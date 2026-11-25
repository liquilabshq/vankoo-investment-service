package com.liquilabs.vankoo.investment.infrastructure.acl.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liquilabs.vankoo.investment.application.internal.outboundservices.acl.InvestorWalletService;
import com.liquilabs.vankoo.investment.domain.exceptions.InsufficientBalanceException;
import com.liquilabs.vankoo.investment.domain.exceptions.WalletUnavailableException;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.Money;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Map;

/**
 * {@link InvestorWalletService} over Finance's
 * {@code POST /api/v1/accounts/{accountId}/wallets/{currency}/debits}.
 *
 * <ul>
 *   <li>{@code accountId} and {@code X-User-Id} are both the investor's id —
 *       the IAM user id the gateway already put in {@code X-User-Id} and the
 *       controller checked. Finance's ownership check passes because it is the
 *       investor's own account, not because Investment is trusted.</li>
 *   <li>{@code Idempotency-Key} is the investment's {@code transactionId}.</li>
 *   <li>Finance answers RFC 9457 with a {@code code}; the ones that mean
 *       "no money" ({@code insufficient-balance}, {@code wallet-not-found})
 *       become {@link InsufficientBalanceException}, a reused key with a
 *       different amount becomes the same {@link IllegalStateException} the
 *       aggregate raises for a reused {@code transactionId}, and anything else
 *       is {@link WalletUnavailableException}.</li>
 * </ul>
 */
@Service
public class FinanceInvestorWalletService implements InvestorWalletService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceInvestorWalletService.class);
    private static final String DEBITS_PATH = "/api/v1/accounts/{accountId}/wallets/{currency}/debits";

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FinanceInvestorWalletService(@Qualifier("financeRestClientBuilder") RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public void debitForInvestment(UserId investorId, Money amount, String idempotencyKey) {
        long amountMinor = amount.amount().movePointRight(2).longValueExact();
        try {
            restClient.post()
                    .uri(DEBITS_PATH, investorId.uuid(), amount.currency().name())
                    .header("X-User-Id", investorId.uuid())
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("amountMinor", amountMinor, "reason", "INVERSION"))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw translate(response.getStatusCode(), readCode(response.getBody().readAllBytes()));
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException exception) {
            throw new WalletUnavailableException("Finance could not be reached to debit the wallet", exception);
        }
    }

    private RuntimeException translate(HttpStatusCode status, String code) {
        if ("insufficient-balance".equals(code) || "wallet-not-found".equals(code)) {
            return new InsufficientBalanceException("The wallet's balance does not cover this investment");
        }
        if ("idempotency-key-conflict".equals(code)) {
            return new IllegalStateException("Transaction ID was already used with different investment data");
        }
        LOGGER.error("Finance refused the debit with {} and code {}", status.value(), code);
        return new WalletUnavailableException("Finance refused the debit (" + status.value() + ")");
    }

    private String readCode(byte[] body) {
        try {
            JsonNode node = objectMapper.readTree(body);
            return node == null ? null : node.path("code").asText(null);
        } catch (IOException exception) {
            return null;
        }
    }
}
