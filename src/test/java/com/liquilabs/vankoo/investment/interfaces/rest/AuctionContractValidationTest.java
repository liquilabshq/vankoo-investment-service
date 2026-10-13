package com.liquilabs.vankoo.investment.interfaces.rest;

import com.liquilabs.vankoo.investment.domain.model.commands.*;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.cloud.stream.default-binder=test")
@AutoConfigureMockMvc
@Import({AuctionContractValidationTest.FixedClockConfiguration.class, TestChannelBinderConfiguration.class})
@ActiveProfiles("test")
class AuctionContractValidationTest {
    @Autowired MockMvc mockMvc;
    @Autowired AuctionCommandService commands;

    @Test
    void rejectsInvalidAuctionAndInvestmentRequestsWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"invoiceId":"invalid-amount","mypeId":"mype-test","invoiceAmount":0,
                                 "currency":"PEN","greenCertified":false,
                                 "payerRuc":"20123456789","payerName":"Pagador S.A.",
                                 "dueDate":"2026-11-06"}
                                """))
                .andExpect(status().isBadRequest());

        var auctionId = publishedAuction("invalid-investment");
        mockMvc.perform(post("/api/v1/auctions/{auctionId}/investments", auctionId.uuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"investorId":"investor-1","amount":0,"currency":"PEN",
                                 "transactionId":"tx-invalid"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsOverfundingAndConflictingIdempotencyKeysWithoutChangingAuction() throws Exception {
        var auctionId = publishedAuction("funding-conflicts");
        var endpoint = "/api/v1/auctions/{auctionId}/investments";
        mockMvc.perform(post(endpoint, auctionId.uuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(investment("investor-1", "500.00", "tx-unique")))
                .andExpect(status().isCreated());

        mockMvc.perform(post(endpoint, auctionId.uuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(investment("investor-2", "500.00", "tx-unique")))
                .andExpect(status().isConflict());
        mockMvc.perform(post(endpoint, auctionId.uuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(investment("investor-2", "9769.76", "tx-overfund")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/auctions/{auctionId}", auctionId.uuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FUNDING"))
                .andExpect(jsonPath("$.currentFunding").value(500.00));
    }

    @Test
    void rejectsCancellationAfterFundingHasStarted() throws Exception {
        var auctionId = publishedAuction("cancel-after-funding");
        mockMvc.perform(post("/api/v1/auctions/{auctionId}/investments", auctionId.uuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(investment("investor-1", "500.00", "tx-before-cancel")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/auctions/{auctionId}/cancel", auctionId.uuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"MYPE request\"}"))
                .andExpect(status().isConflict());
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

    private String investment(String investor, String amount, String transaction) {
        return """
                {"investorId":"%s","amount":%s,"currency":"PEN","transactionId":"%s"}
                """.formatted(investor, amount, transaction);
    }

    @TestConfiguration
    static class FixedClockConfiguration {
        @Bean @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-09-07T17:00:00Z"), ZoneOffset.UTC);
        }
    }
}
