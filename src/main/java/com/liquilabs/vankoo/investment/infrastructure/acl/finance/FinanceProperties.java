package com.liquilabs.vankoo.investment.infrastructure.acl.finance;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Where Finance is and how long to wait for it.
 *
 * @param baseUrl Finance itself, not the gateway: the call is service to service
 *                and the gateway would ask for a JWT Investment does not have.
 *                {@code FINANCE_SERVICE_URL} overrides it per environment.
 * @param timeout connect and read timeout of one debit
 */
@ConfigurationProperties(prefix = "vankoo.investment.finance")
public record FinanceProperties(String baseUrl, Duration timeout) {

    public FinanceProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("vankoo.investment.finance.base-url is required");
        }
        if (timeout == null) {
            timeout = Duration.ofSeconds(5);
        }
    }
}
