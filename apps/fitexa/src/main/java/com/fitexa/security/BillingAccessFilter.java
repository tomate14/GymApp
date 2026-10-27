package com.fitexa.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitexa.dtos.ErrorResponseDto;
import com.fitexa.repository.ClientSubscriptionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class BillingAccessFilter extends OncePerRequestFilter {

    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (isAllowedWithoutActiveSubscription(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof FitexaUserDetails userDetails)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean active = clientSubscriptionRepository.findByClientId(userDetails.getClientId())
            .map(subscription -> subscription.isActive())
            .orElse(false);
        if (!active) {
            response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponseDto payload = ErrorResponseDto.builder()
                .message("La suscripcion del tenant esta vencida o pendiente de pago.")
                .detail("TENANT_SUBSCRIPTION_REQUIRED")
                .build();
            response.getWriter().write(objectMapper.writeValueAsString(payload));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedWithoutActiveSubscription(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        return path.equals("/api/v1/auth/login")
            || path.equals("/api/v1/auth/change-password")
            || path.startsWith("/api/v1/billing/")
            || path.startsWith("/api/v1/public/")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui")
            || path.equals("/swagger-ui.html");
    }
}
