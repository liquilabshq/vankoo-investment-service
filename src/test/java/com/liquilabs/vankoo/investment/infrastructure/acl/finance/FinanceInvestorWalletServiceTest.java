package com.liquilabs.vankoo.investment.infrastructure.acl.finance;

import com.liquilabs.vankoo.investment.domain.exceptions.InsufficientBalanceException;
import com.liquilabs.vankoo.investment.domain.exceptions.WalletUnavailableException;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.Currency;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.Money;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/** The wire to Finance's debit endpoint, request and every answer Investment distinguishes. */
class FinanceInvestorWalletServiceTest {

    private static final String INVESTOR = "018f8f2e-0000-7000-8000-000000000001";
    private static final String URL = "http://finance.test/api/v1/accounts/" + INVESTOR + "/wallets/PEN/debits";

    private MockRestServiceServer server;
    private FinanceInvestorWalletService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://finance.test");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new FinanceInvestorWalletService(builder);
    }

    @Test
    void debitsInMinorUnitsAsTheInvestorWithTheGivenKey() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-User-Id", INVESTOR))
                .andExpect(header("Idempotency-Key", "tx-1"))
                .andExpect(content().json("{\"amountMinor\":123450,\"reason\":\"INVERSION\"}"))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"debitId\":\"d-1\"}"));

        assertThatCode(() -> service.debitForInvestment(new UserId(INVESTOR), pen("1234.50"), "tx-1"))
                .doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void insufficientBalanceAndAMissingWalletAreBothNoMoney() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body("{\"status\":409,\"code\":\"insufficient-balance\"}"));
        server.expect(requestTo(URL)).andRespond(withResourceNotFound()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body("{\"status\":404,\"code\":\"wallet-not-found\"}"));

        assertThatThrownBy(() -> service.debitForInvestment(new UserId(INVESTOR), pen("50.00"), "tx-2"))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThatThrownBy(() -> service.debitForInvestment(new UserId(INVESTOR), pen("50.00"), "tx-3"))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void aReusedKeyWithAnotherAmountIsTheSameConflictAsAReusedTransactionId() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body("{\"status\":409,\"code\":\"idempotency-key-conflict\"}"));

        assertThatThrownBy(() -> service.debitForInvestment(new UserId(INVESTOR), pen("60.00"), "tx-4"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transaction ID was already used");
    }

    @Test
    void anythingElseIsUnavailable() {
        server.expect(requestTo(URL)).andRespond(withServerError());
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.FORBIDDEN).body("not json"));

        assertThatThrownBy(() -> service.debitForInvestment(new UserId(INVESTOR), pen("50.00"), "tx-5"))
                .isInstanceOf(WalletUnavailableException.class);
        assertThatThrownBy(() -> service.debitForInvestment(new UserId(INVESTOR), pen("50.00"), "tx-6"))
                .isInstanceOf(WalletUnavailableException.class);
    }

    private static Money pen(String amount) {
        return new Money(new BigDecimal(amount), Currency.PEN);
    }
}
