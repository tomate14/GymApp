package com.fitexa.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitexa.entities.BillingPlanEntity;
import com.fitexa.entities.MercadoPagoAccountEntity;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.services.MercadoPagoBillingClient;
import com.fitexa.services.SecretCryptoService;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MercadoPagoBillingClientImpl implements MercadoPagoBillingClient {

    private static final String API_BASE_URL = "https://api.mercadopago.com";

    private final ObjectMapper objectMapper;
    private final SecretCryptoService secretCryptoService;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String createPreapprovalPlan(MercadoPagoAccountEntity account, BillingPlanEntity billingPlan) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", billingPlan.getName());
        payload.put("external_reference", "billing-plan:" + billingPlan.getId());
        payload.put("auto_recurring", autoRecurringPayload(billingPlan));

        JsonNode response = send(account, "POST", "/preapproval_plan", payload);
        return response.path("id").asText(null);
    }

    @Override
    public MercadoPagoPreapproval createPreapproval(
        MercadoPagoAccountEntity account,
        BillingPlanEntity billingPlan,
        String payerEmail,
        Long clientId
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("preapproval_plan_id", billingPlan.getMercadoPagoPlanId());
        payload.put("reason", billingPlan.getName());
        payload.put("external_reference", "client:" + clientId + ":billing-plan:" + billingPlan.getId());
        payload.put("payer_email", payerEmail);
        payload.put("back_url", normalizeUrl(account.getAppBaseUrl()) + "/facturacion");
        payload.put("status", "pending");

        JsonNode response = send(account, "POST", "/preapproval", payload);
        return toPreapproval(response);
    }

    @Override
    public MercadoPagoPreapproval getPreapproval(MercadoPagoAccountEntity account, String preapprovalId) {
        JsonNode response = send(account, "GET", "/preapproval/" + preapprovalId, null);
        return toPreapproval(response);
    }

    @Override
    public MercadoPagoAuthorizedPayment getAuthorizedPayment(MercadoPagoAccountEntity account, String authorizedPaymentId) {
        JsonNode response = send(account, "GET", "/authorized_payments/" + authorizedPaymentId, null);
        return toAuthorizedPayment(response);
    }

    @Override
    public List<MercadoPagoAuthorizedPayment> searchAuthorizedPayments(MercadoPagoAccountEntity account, String preapprovalId) {
        JsonNode response = send(account, "GET", "/authorized_payments/search?preapproval_id=" + preapprovalId, null);
        List<MercadoPagoAuthorizedPayment> result = new ArrayList<>();
        response.path("results").forEach(item -> result.add(toAuthorizedPayment(item)));
        return result;
    }

    private Map<String, Object> autoRecurringPayload(BillingPlanEntity billingPlan) {
        Map<String, Object> autoRecurring = new LinkedHashMap<>();
        autoRecurring.put("frequency", billingPlan.getFrequency());
        autoRecurring.put("frequency_type", billingPlan.getFrequencyType().getMercadoPagoValue());
        autoRecurring.put("transaction_amount", normalizeAmount(billingPlan.getPrice()));
        autoRecurring.put("currency_id", billingPlan.getCurrencyId());
        return autoRecurring;
    }

    private JsonNode send(MercadoPagoAccountEntity account, String method, String path, Map<String, Object> payload) {
        String accessToken = secretCryptoService.decrypt(account.getEncryptedAccessToken());
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessValidationException("La cuenta de Mercado Pago no tiene access token configurado.");
        }
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + path))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");

            if ("GET".equals(method)) {
                builder.GET();
            } else {
                builder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessValidationException("Mercado Pago rechazo la operacion: " + response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (BusinessValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo comunicar con Mercado Pago.", ex);
        }
    }

    private MercadoPagoPreapproval toPreapproval(JsonNode response) {
        JsonNode autoRecurring = response.path("auto_recurring");
        return new MercadoPagoPreapproval(
            response.path("id").asText(null),
            response.path("status").asText(null),
            firstText(response, "init_point", "sandbox_init_point"),
            response.path("preapproval_plan_id").asText(null),
            response.path("payer_email").asText(null),
            parseDate(firstText(autoRecurring, "next_payment_date", "start_date"))
        );
    }

    private MercadoPagoAuthorizedPayment toAuthorizedPayment(JsonNode response) {
        JsonNode payment = response.path("payment");
        return new MercadoPagoAuthorizedPayment(
            response.path("id").asText(null),
            response.path("preapproval_id").asText(null),
            payment.path("id").asText(null),
            response.path("status").asText(null),
            payment.path("status").asText(null),
            payment.path("status_detail").asText(null),
            parseAmount(response.path("transaction_amount")),
            response.path("currency_id").asText(null),
            parseDate(response.path("debit_date").asText(null))
        );
    }

    private String firstText(JsonNode node, String... names) {
        for (String name : names) {
            String value = node.path(name).asText(null);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount.stripTrailingZeros();
    }

    private BigDecimal parseAmount(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        try {
            return new BigDecimal(node.asText()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:4200";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
