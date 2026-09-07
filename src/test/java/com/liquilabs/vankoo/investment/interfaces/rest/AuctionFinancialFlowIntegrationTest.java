package com.liquilabs.vankoo.investment.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liquilabs.vankoo.investment.domain.model.commands.*;
import com.liquilabs.vankoo.investment.domain.model.queries.GetAuctionByIdQuery;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
import com.liquilabs.vankoo.investment.domain.services.AuctionCommandService;
import com.liquilabs.vankoo.investment.domain.services.AuctionQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.cloud.stream.default-binder=test")
@AutoConfigureMockMvc
@Import({AuctionFinancialFlowIntegrationTest.FixedClockConfiguration.class, TestChannelBinderConfiguration.class})
@ActiveProfiles("test")
class AuctionFinancialFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuctionCommandService auctionCommandService;

    @Autowired
    private AuctionQueryService auctionQueryService;

    @Test
    void runsTheEvaluationQuoteAcceptanceAndInvestmentFlow() throws Exception {
        String auctionId = mockMvc.perform(post("/api/v1/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceId": "invoice-http-flow",
                                  "mypeId": "mype-http-flow",
                                  "invoiceAmount": 10000.00,
                                  "currency": "PEN",
                                  "greenCertified": false,
                                  "payerRuc": "20123456789",
                                  "payerName": "Pagador HTTP S.A.",
                                  "dueDate": "2026-11-06"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(put("/api/v1/internal/auctions/{auctionId}/evaluation", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assessmentId": "assessment-http-flow",
                                  "riskGrade": "B",
                                  "fullBalanceOutstanding": true,
                                  "assessedAt": "2026-09-07T17:00:00Z"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.fullBalanceOutstandingConfirmed").value(true));

        String quoteJson = mockMvc.perform(post("/api/v1/auctions/{auctionId}/quotes", auctionId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.investorTeaPct").value(15.000000))
                .andExpect(jsonPath("$.fundingTarget").value(9769.76))
                .andExpect(jsonPath("$.platformFeeBase").value(60.00))
                .andExpect(jsonPath("$.platformFeeTax").value(10.80))
                .andExpect(jsonPath("$.mypeAdvance").value(9698.96))
                .andReturn().getResponse().getContentAsString();
        JsonNode quote = objectMapper.readTree(quoteJson);
        String quoteId = quote.get("quoteId").asText();

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/quotes/{quoteId}/accept", auctionId, quoteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.acceptedQuote.quoteId").value(quoteId));

        String investmentJson = mockMvc.perform(post("/api/v1/auctions/{auctionId}/investments", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "investorId": "investor-http-flow",
                                  "amount": 500.00,
                                  "currency": "PEN",
                                  "transactionId": "transaction-http-flow"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.expectedGrossProfit").isNumber())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(investmentJson).has("returnRate")).isFalse();

        String marketplaceJson = mockMvc.perform(get("/api/v1/auctions/marketplace"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode marketplaceAuction = StreamSupport.stream(objectMapper.readTree(marketplaceJson).spliterator(), false)
                .filter(node -> auctionId.equals(node.path("auctionId").asText()))
                .findFirst()
                .orElseThrow();
        assertThat(marketplaceAuction.path("status").asText()).isEqualTo("FUNDING");
        assertThat(marketplaceAuction.path("investorTeaPct").decimalValue()).isEqualByComparingTo("15.000000");
    }

    @Test
    void serializesConcurrentInvestmentsSoTheAuctionCannotBeOverfunded() throws Exception {
        var auctionId = auctionCommandService.handle(new CreateAuctionCommand(
                new InvoiceId("invoice-concurrency"),
                new UserId("mype-concurrency"),
                new Money(new BigDecimal("10000.00"), Currency.PEN),
                false,
                "20123456789",
                "Pagador Concurrente S.A.",
                LocalDate.of(2026, 11, 6)
        ));
        auctionCommandService.handle(new EvaluateAuctionCommand(
                auctionId, "assessment-concurrency", ScoreGrade.B, true, Instant.parse("2026-09-07T17:00:00Z")
        ));
        var quote = auctionCommandService.handle(new CreateFinancialQuoteCommand(auctionId));
        auctionCommandService.handle(new AcceptFinancialQuoteCommand(auctionId, quote.getId()));

        try (var executor = Executors.newFixedThreadPool(2)) {
            List<Callable<Boolean>> investments = List.of(
                    () -> attemptInvestment(auctionId, "investor-concurrency-1", "transaction-concurrency-1"),
                    () -> attemptInvestment(auctionId, "investor-concurrency-2", "transaction-concurrency-2")
            );
            long successes = executor.invokeAll(investments).stream()
                    .filter(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();
            assertThat(successes).isEqualTo(1);
        }

        var auction = auctionQueryService.handle(new GetAuctionByIdQuery(auctionId)).orElseThrow();
        assertThat(auction.getCurrentFunding().amount()).isEqualByComparingTo("6000.00");
        assertThat(auction.getPartitions()).hasSize(1);
    }

    @Test
    void publishesTheFixedRateContractInOpenApi() throws Exception {
        String specification = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode api = objectMapper.readTree(specification);
        JsonNode investmentProperties = api.path("components")
                .path("schemas")
                .path("CreateInvestmentResource")
                .path("properties");

        assertThat(investmentProperties.has("amount")).isTrue();
        assertThat(investmentProperties.has("transactionId")).isTrue();
        assertThat(investmentProperties.has("returnRate")).isFalse();
        assertThat(api.path("paths").has("/api/v1/auctions/{auctionId}/quotes/{quoteId}/accept")).isTrue();
    }

    private boolean attemptInvestment(AuctionId auctionId, String investorId, String transactionId) {
        try {
            auctionCommandService.handle(new AddPartitionCommand(
                    auctionId,
                    new UserId(investorId),
                    new Money(new BigDecimal("6000.00"), Currency.PEN),
                    transactionId
            ));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
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
