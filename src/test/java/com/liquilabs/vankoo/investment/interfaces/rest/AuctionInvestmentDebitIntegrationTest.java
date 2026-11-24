package com.liquilabs.vankoo.investment.interfaces.rest;

import com.liquilabs.vankoo.investment.application.internal.outboundservices.acl.InvestorWalletService;
import com.liquilabs.vankoo.investment.domain.exceptions.InsufficientBalanceException;
import com.liquilabs.vankoo.investment.domain.exceptions.WalletUnavailableException;
import com.liquilabs.vankoo.investment.domain.model.commands.AcceptFinancialQuoteCommand;
import com.liquilabs.vankoo.investment.domain.model.commands.CreateAuctionCommand;
import com.liquilabs.vankoo.investment.domain.model.commands.CreateFinancialQuoteCommand;
import com.liquilabs.vankoo.investment.domain.model.commands.EvaluateAuctionCommand;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionId;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.Currency;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.InvoiceId;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.Money;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.ScoreGrade;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.UserId;
import com.liquilabs.vankoo.investment.domain.services.AuctionCommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Buying a share pays for it: the wallet is debited first, through
 * {@link InvestorWalletService}, and the share is granted only if that worked.
 */
@SpringBootTest(properties = "spring.cloud.stream.default-binder=test")
@AutoConfigureMockMvc
@Import({AuctionInvestmentDebitIntegrationTest.FixedClockConfiguration.class, TestChannelBinderConfiguration.class})
@ActiveProfiles("test")
class AuctionInvestmentDebitIntegrationTest {

    private static final String ENDPOINT = "/api/v1/auctions/{auctionId}/investments";

    @MockitoBean
    private InvestorWalletService investorWallet;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuctionCommandService commands;

    @Test
    void debitsTheInvestorWithTheTransactionIdAsKeyAndAReplayDoesNotPayAgain() throws Exception {
        var auctionId = publishedAuction("debit-ok");

        invest(auctionId, "investor-a", "investor-a", "500.00", "tx-debit-ok").andExpect(status().isCreated());
        invest(auctionId, "investor-a", "investor-a", "500.00", "tx-debit-ok").andExpect(status().isCreated());

        verify(investorWallet, times(1)).debitForInvestment(
                new UserId("investor-a"), new Money(new BigDecimal("500.00"), Currency.PEN), "tx-debit-ok");
        mockMvc.perform(get("/api/v1/auctions/{auctionId}", auctionId.uuid()))
                .andExpect(jsonPath("$.currentFunding").value(500.00));
    }

    @Test
    void anInsufficientBalanceIsAConflictWithItsCodeAndGrantsNothing() throws Exception {
        var auctionId = publishedAuction("debit-poor");
        doThrow(new InsufficientBalanceException("The wallet's balance does not cover this investment"))
                .when(investorWallet).debitForInvestment(any(), any(), any());

        invest(auctionId, "investor-b", "investor-b", "500.00", "tx-debit-poor")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("insufficient-balance"));

        mockMvc.perform(get("/api/v1/auctions/{auctionId}", auctionId.uuid()))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.currentFunding").value(0.00));
    }

    @Test
    void anUnreachableWalletIsAServiceUnavailableWithItsCodeAndGrantsNothing() throws Exception {
        var auctionId = publishedAuction("debit-down");
        doThrow(new WalletUnavailableException("Finance could not be reached to debit the wallet"))
                .when(investorWallet).debitForInvestment(any(), any(), any());

        invest(auctionId, "investor-c", "investor-c", "500.00", "tx-debit-down")
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("wallet-unavailable"));

        mockMvc.perform(get("/api/v1/auctions/{auctionId}", auctionId.uuid()))
                .andExpect(jsonPath("$.currentFunding").value(0.00));
    }

    @Test
    void anInvestmentTheAuctionWouldRefuseIsNeverPaidFor() throws Exception {
        var auctionId = publishedAuction("debit-refused");

        invest(auctionId, "investor-d", "investor-d", "99999.00", "tx-overfund").andExpect(status().isBadRequest());
        invest(auctionId, "investor-d", "investor-d", "10.00", "tx-below-minimum").andExpect(status().isBadRequest());

        verify(investorWallet, never()).debitForInvestment(any(), any(), any());
    }

    @Test
    void onlyTheInvestorCanInvestWithTheirOwnMoney() throws Exception {
        var auctionId = publishedAuction("debit-impostor");

        invest(auctionId, "someone-else", "investor-e", "500.00", "tx-impostor").andExpect(status().isForbidden());
        mockMvc.perform(post(ENDPOINT, auctionId.uuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("investor-e", "500.00", "tx-anonymous")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(investorWallet);
    }

    private org.springframework.test.web.servlet.ResultActions invest(
            AuctionId auctionId, String caller, String investor, String amount, String transactionId) throws Exception {
        return mockMvc.perform(post(ENDPOINT, auctionId.uuid())
                .header("X-User-Id", caller)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(investor, amount, transactionId)));
    }

    private static String body(String investor, String amount, String transactionId) {
        return """
                {"investorId":"%s","amount":%s,"currency":"PEN","transactionId":"%s"}
                """.formatted(investor, amount, transactionId);
    }

    private AuctionId publishedAuction(String suffix) {
        var id = commands.handle(new CreateAuctionCommand(new InvoiceId("invoice-" + suffix),
                new UserId("mype-" + suffix), new Money(new BigDecimal("10000.00"), Currency.PEN),
                false, "20123456789", "Pagador S.A.", LocalDate.of(2026, 11, 6)));
        commands.handle(new EvaluateAuctionCommand(id, "assessment-" + suffix, ScoreGrade.B,
                true, Instant.parse("2026-09-07T17:00:00Z")));
        var quote = commands.handle(new CreateFinancialQuoteCommand(id, new UserId("mype-" + suffix)));
        commands.handle(new AcceptFinancialQuoteCommand(id, quote.getId(), new UserId("mype-" + suffix)));
        return id;
    }

    @TestConfiguration
    static class FixedClockConfiguration {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-09-07T17:00:00Z"), ZoneOffset.UTC);
        }
    }
}
