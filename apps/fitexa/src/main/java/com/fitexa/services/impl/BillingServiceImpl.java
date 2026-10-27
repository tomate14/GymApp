package com.fitexa.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitexa.dtos.billing.BillingCheckoutRequestDto;
import com.fitexa.dtos.billing.BillingCheckoutResponseDto;
import com.fitexa.dtos.billing.BillingPlanResponseDto;
import com.fitexa.dtos.billing.ClientSubscriptionResponseDto;
import com.fitexa.entities.BillingEventEntity;
import com.fitexa.entities.BillingPlanEntity;
import com.fitexa.entities.ClientEntity;
import com.fitexa.entities.ClientSubscriptionEntity;
import com.fitexa.entities.ClientSubscriptionPaymentEntity;
import com.fitexa.entities.ClientSubscriptionStatus;
import com.fitexa.entities.MercadoPagoAccountEntity;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.BillingEventRepository;
import com.fitexa.repository.BillingPlanRepository;
import com.fitexa.repository.ClientSubscriptionRepository;
import com.fitexa.repository.ClientSubscriptionPaymentRepository;
import com.fitexa.repository.MercadoPagoAccountRepository;
import com.fitexa.services.AuthenticatedUserService;
import com.fitexa.services.BillingService;
import com.fitexa.services.MercadoPagoBillingClient;
import com.fitexa.services.SecretCryptoService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private static final String PROVIDER = "mercado_pago";

    private final BillingPlanRepository billingPlanRepository;
    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final ClientSubscriptionPaymentRepository clientSubscriptionPaymentRepository;
    private final MercadoPagoAccountRepository mercadoPagoAccountRepository;
    private final BillingEventRepository billingEventRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final MercadoPagoBillingClient mercadoPagoBillingClient;
    private final SecretCryptoService secretCryptoService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BillingPlanResponseDto> findActivePlans() {
        return billingPlanRepository.findByActiveTrueOrderByPriceAsc().stream().map(this::toPlanDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientSubscriptionResponseDto getCurrentSubscription() {
        ClientEntity client = authenticatedUserService.getCurrentClient();
        return clientSubscriptionRepository.findByClientId(client.getId())
            .map(this::toSubscriptionDto)
            .orElseGet(() -> emptySubscription(client));
    }

    @Override
    @Transactional
    public BillingCheckoutResponseDto createCheckout(BillingCheckoutRequestDto request) {
        ClientEntity client = authenticatedUserService.getCurrentClient();
        BillingPlanEntity plan = billingPlanRepository.findByIdAndActiveTrue(request.getBillingPlanId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan de facturacion no encontrado: " + request.getBillingPlanId()));
        MercadoPagoAccountEntity account = activeAccount();

        ensureMercadoPagoPlan(account, plan);

        ClientSubscriptionEntity subscription = clientSubscriptionRepository.findByClientId(client.getId())
            .orElseGet(() -> ClientSubscriptionEntity.builder()
                .client(client)
                .billingPlan(plan)
                .status(ClientSubscriptionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        String payerEmail = clean(request.getPayerEmail());
        if (payerEmail != null) {
            subscription.setPayerEmail(payerEmail);
        }
        if (payerEmail == null) {
            payerEmail = clean(subscription.getPayerEmail());
        }
        if (payerEmail == null) {
            payerEmail = authenticatedUserService.getCurrentUser().getEmail();
        }

        if (subscription.isActive() && plan.getId().equals(subscription.getBillingPlan().getId())) {
            return toCheckoutDto(subscription);
        }

        MercadoPagoPreapproval preapproval = mercadoPagoBillingClient.createPreapproval(account, plan, payerEmail, client.getId());
        subscription.setBillingPlan(plan);
        subscription.setStatus(ClientSubscriptionStatus.fromMercadoPagoStatus(preapproval.status()));
        subscription.setMercadoPagoPreapprovalId(preapproval.id());
        subscription.setMercadoPagoPlanId(preapproval.preapprovalPlanId() == null ? plan.getMercadoPagoPlanId() : preapproval.preapprovalPlanId());
        subscription.setPayerEmail(preapproval.payerEmail() == null ? payerEmail : preapproval.payerEmail());
        subscription.setCheckoutUrl(preapproval.initPoint());
        subscription.setNextPaymentAt(preapproval.nextPaymentAt());
        subscription.setUpdatedAt(LocalDateTime.now());
        markLifecycleDates(subscription);
        return toCheckoutDto(clientSubscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public void handleMercadoPagoWebhook(String payload, Map<String, String> headers, Map<String, String> queryParams) {
        MercadoPagoAccountEntity account = activeAccount();
        validateWebhookSecret(account, headers);

        JsonNode root = parsePayload(payload);
        String resourceId = firstValue(
            queryParams.get("data.id"),
            queryParams.get("id"),
            root.path("data").path("id").asText(null),
            root.path("id").asText(null)
        );
        String eventType = firstValue(queryParams.get("type"), root.path("type").asText(null), root.path("action").asText(null), "unknown");
        String eventKey = PROVIDER + ":" + eventType + ":" + resourceId;

        if (resourceId == null || resourceId.isBlank()) {
            eventKey = PROVIDER + ":" + eventType + ":" + Integer.toHexString(payload.hashCode());
        }
        if (billingEventRepository.existsByEventKey(eventKey)) {
            return;
        }

        ClientSubscriptionEntity subscription = null;
        ClientSubscriptionStatus resultingStatus = null;
        if (resourceId != null && !resourceId.isBlank()) {
            if (isAuthorizedPaymentEvent(eventType)) {
                subscription = syncAuthorizedPayment(account, resourceId);
            } else {
                subscription = syncPreapproval(account, resourceId);
            }
            resultingStatus = subscription == null ? null : subscription.getStatus();
        }

        billingEventRepository.save(BillingEventEntity.builder()
            .eventKey(eventKey)
            .provider(PROVIDER)
            .eventType(eventType)
            .providerResourceId(resourceId)
            .clientId(subscription == null ? null : subscription.getClient().getId())
            .resultingStatus(resultingStatus)
            .payload(payload == null || payload.isBlank() ? "{}" : payload)
            .receivedAt(LocalDateTime.now())
            .build());
    }

    @Scheduled(cron = "${fitexa.billing.reconciliation-cron:0 15 3 * * *}")
    @Transactional
    public void reconcileMercadoPagoSubscriptions() {
        MercadoPagoAccountEntity account = mercadoPagoAccountRepository.findFirstByActiveTrueOrderByIdAsc().orElse(null);
        if (account == null || clean(account.getEncryptedAccessToken()) == null) {
            return;
        }
        List<ClientSubscriptionStatus> statuses = List.of(
            ClientSubscriptionStatus.ACTIVE,
            ClientSubscriptionStatus.PENDING,
            ClientSubscriptionStatus.PAUSED,
            ClientSubscriptionStatus.PAYMENT_REQUIRED
        );
        for (ClientSubscriptionEntity subscription : clientSubscriptionRepository.findByStatusInAndMercadoPagoPreapprovalIdIsNotNull(statuses)) {
            syncPreapproval(account, subscription.getMercadoPagoPreapprovalId());
            mercadoPagoBillingClient.searchAuthorizedPayments(account, subscription.getMercadoPagoPreapprovalId()).stream()
                .sorted(Comparator.comparing(payment -> payment.debitDate() == null ? LocalDateTime.MIN : payment.debitDate()))
                .forEach(this::recordAuthorizedPayment);
        }
    }

    private ClientSubscriptionEntity syncPreapproval(MercadoPagoAccountEntity account, String preapprovalId) {
        return clientSubscriptionRepository.findByMercadoPagoPreapprovalId(preapprovalId)
            .map(subscription -> {
                MercadoPagoPreapproval preapproval = mercadoPagoBillingClient.getPreapproval(account, preapprovalId);
                subscription.setStatus(ClientSubscriptionStatus.fromMercadoPagoStatus(preapproval.status()));
                subscription.setPayerEmail(preapproval.payerEmail() == null ? subscription.getPayerEmail() : preapproval.payerEmail());
                subscription.setNextPaymentAt(preapproval.nextPaymentAt());
                subscription.setUpdatedAt(LocalDateTime.now());
                markLifecycleDates(subscription);
                return clientSubscriptionRepository.save(subscription);
            })
            .orElse(null);
    }

    private ClientSubscriptionEntity syncAuthorizedPayment(MercadoPagoAccountEntity account, String authorizedPaymentId) {
        MercadoPagoAuthorizedPayment authorizedPayment = mercadoPagoBillingClient.getAuthorizedPayment(account, authorizedPaymentId);
        return recordAuthorizedPayment(authorizedPayment);
    }

    private ClientSubscriptionEntity recordAuthorizedPayment(MercadoPagoAuthorizedPayment authorizedPayment) {
        if (authorizedPayment == null || clean(authorizedPayment.preapprovalId()) == null) {
            return null;
        }
        ClientSubscriptionEntity subscription = clientSubscriptionRepository.findByMercadoPagoPreapprovalId(authorizedPayment.preapprovalId())
            .orElse(null);
        if (subscription == null) {
            return null;
        }

        if (!authorizedPayment.isApproved()) {
            subscription.setStatus(ClientSubscriptionStatus.PAYMENT_REQUIRED);
            subscription.setUpdatedAt(LocalDateTime.now());
            return clientSubscriptionRepository.save(subscription);
        }

        boolean alreadyRecorded = clean(authorizedPayment.id()) != null &&
            clientSubscriptionPaymentRepository.findByMercadoPagoAuthorizedPaymentId(authorizedPayment.id()).isPresent();
        if (!alreadyRecorded && clean(authorizedPayment.paymentId()) != null) {
            alreadyRecorded = clientSubscriptionPaymentRepository.findByMercadoPagoPaymentId(authorizedPayment.paymentId()).isPresent();
        }

        LocalDateTime paidAt = authorizedPayment.debitDate() == null ? LocalDateTime.now() : authorizedPayment.debitDate();
        LocalDateTime periodStart = resolvePeriodStart(subscription, paidAt);
        LocalDateTime periodEnd = addBillingFrequency(periodStart, subscription.getBillingPlan());

        if (!alreadyRecorded) {
            clientSubscriptionPaymentRepository.save(ClientSubscriptionPaymentEntity.builder()
                .subscription(subscription)
                .client(subscription.getClient())
                .mercadoPagoAuthorizedPaymentId(authorizedPayment.id())
                .mercadoPagoPaymentId(authorizedPayment.paymentId())
                .status(authorizedPayment.paymentStatus())
                .statusDetail(authorizedPayment.paymentStatusDetail())
                .amount(authorizedPayment.amount())
                .currencyId(authorizedPayment.currencyId() == null ? subscription.getBillingPlan().getCurrencyId() : authorizedPayment.currencyId())
                .paidAt(paidAt)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        }

        if (subscription.getCurrentPeriodEnd() == null || periodEnd.isAfter(subscription.getCurrentPeriodEnd())) {
            subscription.setCurrentPeriodStart(periodStart);
            subscription.setCurrentPeriodEnd(periodEnd);
        }
        subscription.setStatus(ClientSubscriptionStatus.ACTIVE);
        subscription.setActivatedAt(subscription.getActivatedAt() == null ? paidAt : subscription.getActivatedAt());
        subscription.setCancelledAt(null);
        subscription.setUpdatedAt(LocalDateTime.now());
        return clientSubscriptionRepository.save(subscription);
    }

    private void ensureMercadoPagoPlan(MercadoPagoAccountEntity account, BillingPlanEntity plan) {
        if (plan.getMercadoPagoPlanId() != null && !plan.getMercadoPagoPlanId().isBlank()) {
            return;
        }
        String mercadoPagoPlanId = mercadoPagoBillingClient.createPreapprovalPlan(account, plan);
        if (mercadoPagoPlanId == null || mercadoPagoPlanId.isBlank()) {
            throw new BusinessValidationException("Mercado Pago no devolvio el ID del plan de suscripcion.");
        }
        plan.setMercadoPagoPlanId(mercadoPagoPlanId);
        plan.setUpdatedAt(LocalDateTime.now());
        billingPlanRepository.save(plan);
    }

    private MercadoPagoAccountEntity activeAccount() {
        return mercadoPagoAccountRepository.findFirstByActiveTrueOrderByIdAsc()
            .orElseThrow(() -> new BusinessValidationException("No hay cuenta activa de Mercado Pago configurada."));
    }

    private void validateWebhookSecret(MercadoPagoAccountEntity account, Map<String, String> headers) {
        String expectedSecret = secretCryptoService.decrypt(account.getEncryptedWebhookSecret());
        if (expectedSecret == null || expectedSecret.isBlank()) {
            return;
        }
        String providedSecret = firstValue(headers.get("x-webhook-secret"), headers.get("X-Webhook-Secret"));
        if (!expectedSecret.equals(providedSecret)) {
            throw new BusinessValidationException("Webhook de Mercado Pago no autorizado.");
        }
    }

    private JsonNode parsePayload(String payload) {
        try {
            if (payload == null || payload.isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new BusinessValidationException("Payload de Mercado Pago invalido.");
        }
    }

    private void markLifecycleDates(ClientSubscriptionEntity subscription) {
        LocalDateTime now = LocalDateTime.now();
        if (subscription.getStatus() == ClientSubscriptionStatus.ACTIVE && subscription.getActivatedAt() == null) {
            subscription.setActivatedAt(now);
            subscription.setCancelledAt(null);
        }
        if (
            subscription.getStatus() == ClientSubscriptionStatus.CANCELLED ||
            subscription.getStatus() == ClientSubscriptionStatus.EXPIRED ||
            subscription.getStatus() == ClientSubscriptionStatus.PAYMENT_REQUIRED
        ) {
            subscription.setCancelledAt(subscription.getCancelledAt() == null ? now : subscription.getCancelledAt());
        }
    }

    private LocalDateTime resolvePeriodStart(ClientSubscriptionEntity subscription, LocalDateTime paidAt) {
        if (subscription.getCurrentPeriodEnd() != null && subscription.getCurrentPeriodEnd().isAfter(paidAt)) {
            return subscription.getCurrentPeriodEnd();
        }
        return paidAt;
    }

    private LocalDateTime addBillingFrequency(LocalDateTime start, BillingPlanEntity plan) {
        return switch (plan.getFrequencyType()) {
            case DAYS -> start.plusDays(plan.getFrequency());
            case MONTHS -> start.plusMonths(plan.getFrequency());
            case YEARS -> start.plusYears(plan.getFrequency());
        };
    }

    private boolean isAuthorizedPaymentEvent(String eventType) {
        return eventType != null && eventType.toLowerCase().contains("authorized_payment");
    }

    private BillingPlanResponseDto toPlanDto(BillingPlanEntity entity) {
        return BillingPlanResponseDto.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .name(entity.getName())
            .description(entity.getDescription())
            .price(entity.getPrice())
            .currencyId(entity.getCurrencyId())
            .frequency(entity.getFrequency())
            .frequencyType(entity.getFrequencyType())
            .build();
    }

    private ClientSubscriptionResponseDto toSubscriptionDto(ClientSubscriptionEntity entity) {
        BillingPlanEntity plan = entity.getBillingPlan();
        return ClientSubscriptionResponseDto.builder()
            .clientId(entity.getClient().getId())
            .clientName(entity.getClient().getName())
            .status(entity.getStatus())
            .active(entity.isActive())
            .billingPlanId(plan.getId())
            .billingPlanName(plan.getName())
            .price(plan.getPrice())
            .currencyId(plan.getCurrencyId())
            .nextPaymentAt(entity.getNextPaymentAt())
            .currentPeriodStart(entity.getCurrentPeriodStart())
            .currentPeriodEnd(entity.getCurrentPeriodEnd())
            .activatedAt(entity.getActivatedAt())
            .cancelledAt(entity.getCancelledAt())
            .checkoutUrl(entity.getCheckoutUrl())
            .build();
    }

    private ClientSubscriptionResponseDto emptySubscription(ClientEntity client) {
        return ClientSubscriptionResponseDto.builder()
            .clientId(client.getId())
            .clientName(client.getName())
            .status(ClientSubscriptionStatus.NONE)
            .active(false)
            .build();
    }

    private BillingCheckoutResponseDto toCheckoutDto(ClientSubscriptionEntity entity) {
        return BillingCheckoutResponseDto.builder()
            .clientId(entity.getClient().getId())
            .billingPlanId(entity.getBillingPlan().getId())
            .status(entity.getStatus())
            .checkoutUrl(entity.getCheckoutUrl())
            .mercadoPagoPreapprovalId(entity.getMercadoPagoPreapprovalId())
            .build();
    }

    private String firstValue(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
