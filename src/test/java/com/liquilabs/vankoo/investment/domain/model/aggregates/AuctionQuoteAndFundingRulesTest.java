package com.liquilabs.vankoo.investment.domain.model.aggregates;

import com.liquilabs.vankoo.investment.domain.model.entities.AuctionFinancialQuote;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
import com.liquilabs.vankoo.investment.domain.services.AuctionPricingCalculator;
import com.liquilabs.vankoo.investment.domain.services.AuctionPricingCalculatorTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuctionQuoteAndFundingRulesTest {
    private static final Instant NOW = Instant.parse("2026-09-25T17:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);
    private static final LocalDate DUE_DATE = TODAY.plusDays(60);
    private final AuctionPricingCalculator calculator = new AuctionPricingCalculator();

    @ParameterizedTest
    @EnumSource(value = ScoreGrade.class, names = {"A", "B", "C"})
    void quoteConservesFaceValueForEveryRiskGrade(ScoreGrade grade) {
        var result = calculator.calculate(AuctionPricingCalculatorTest.pricingParameters(),
                money("10000.00"), grade, TODAY, DUE_DATE);
        assertThat(result.fundingTarget().add(result.investorGrossProfit())).isEqualByComparingTo("10000.00");
        assertThat(result.mypeAdvance().add(result.mypeTotalCost())).isEqualByComparingTo("10000.00");
        assertThat(result.platformFeeBase().add(result.platformFeeTax()))
                .isEqualByComparingTo(result.platformFeeTotal());
        assertThat(result.mypeAdvance()).isPositive();
    }

    @Test
    void rejectsMissingRiskGradeAndInvalidInvoiceInputs() {
        assertThatThrownBy(() -> calculator.calculate(AuctionPricingCalculatorTest.pricingParameters(),
                money("10000.00"), null, TODAY, DUE_DATE))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("risk grade");
        assertThatThrownBy(() -> new Auction(new InvoiceId("invalid-amount"), new UserId("mype-1"),
                money("0.00"), false, "20123456789", "Pagador S.A.", DUE_DATE))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("positive");
        assertThatThrownBy(() -> calculator.calculate(AuctionPricingCalculatorTest.pricingParameters(),
                money("10000.00"), ScoreGrade.B, TODAY, TODAY))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("due date");
    }

    @Test
    void acceptingQuotePublishesAuctionAndSetsFundingWindow() {
        Auction auction = evaluatedAuction();
        AuctionFinancialQuote quote = quote(auction);
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.DRAFT);
        assertThat(auction.getTargetAmount()).isNull();
        auction.acceptQuote(quote.getId(), NOW, Duration.ofDays(7), Duration.ofDays(1), ZoneId.of("America/Lima"));
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.PUBLISHED);
        assertThat(auction.getTargetAmount().amount()).isEqualByComparingTo(quote.getFundingTarget());
        assertThat(auction.getCurrentFunding().amount()).isEqualByComparingTo("0.00");
        assertThat(auction.getPublishedAt()).isEqualTo(NOW);
        assertThat(auction.getExpiresAt()).isAfter(NOW).isBeforeOrEqualTo(NOW.plus(Duration.ofDays(7)));
    }

    @Test
    void partialFundingPreservesTheRemainingBalance() {
        Auction auction = publishedAuction();
        BigDecimal target = auction.getTargetAmount().amount();
        auction.addInvestment(new UserId("investor-1"), money("500.00"),
                new BigDecimal("50.00"), "tx-partial", NOW.plusSeconds(1));
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.FUNDING);
        assertThat(auction.getCurrentFunding().amount()).isEqualByComparingTo("500.00");
        assertThat(target.subtract(auction.getCurrentFunding().amount()))
                .isEqualByComparingTo(target.subtract(new BigDecimal("500.00")));
        assertThat(auction.isFunded()).isFalse();
    }

    @Test
    void overfundingIsRejectedWithoutChangingTheConfirmedBalance() {
        Auction auction = publishedAuction();
        BigDecimal target = auction.getTargetAmount().amount();
        auction.addInvestment(new UserId("investor-1"), money("500.00"),
                new BigDecimal("50.00"), "tx-first", NOW.plusSeconds(1));
        assertThatThrownBy(() -> auction.addInvestment(new UserId("investor-2"),
                new Money(target, Currency.PEN), new BigDecimal("50.00"), "tx-overfund", NOW.plusSeconds(2)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("remaining funding target");
        assertThat(auction.getCurrentFunding().amount()).isEqualByComparingTo("500.00");
        assertThat(auction.getPartitions()).hasSize(1);
    }

    @Test
    void exactFinalParticipationCompletesFundingAndBlocksFurtherInvestments() {
        Auction auction = publishedAuction();
        BigDecimal target = auction.getTargetAmount().amount();
        auction.addInvestment(new UserId("investor-1"), money("500.00"),
                new BigDecimal("50.00"), "tx-first", NOW.plusSeconds(1));
        auction.addInvestment(new UserId("investor-2"),
                new Money(target.subtract(new BigDecimal("500.00")), Currency.PEN),
                new BigDecimal("50.00"), "tx-final", NOW.plusSeconds(2));
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.FULLY_FUNDED);
        assertThat(auction.getCurrentFunding().amount()).isEqualByComparingTo(target);
        assertThat(auction.isFunded()).isTrue();
        assertThatThrownBy(() -> auction.addInvestment(new UserId("investor-3"), money("50.00"),
                new BigDecimal("50.00"), "tx-late", NOW.plusSeconds(3)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("not accepting");
    }

    @Test
    void reusedTransactionIdWithDifferentDataIsRejected() {
        Auction auction = publishedAuction();
        auction.addInvestment(new UserId("investor-1"), money("500.00"),
                new BigDecimal("50.00"), "tx-idempotent", NOW.plusSeconds(1));
        assertThatThrownBy(() -> auction.addInvestment(new UserId("investor-2"), money("500.00"),
                new BigDecimal("50.00"), "tx-idempotent", NOW.plusSeconds(2)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already used");
        assertThat(auction.getPartitions()).hasSize(1);
    }

    @Test
    void investmentAtFundingDeadlineIsRejected() {
        Auction auction = publishedAuction();
        assertThatThrownBy(() -> auction.addInvestment(new UserId("investor-1"), money("500.00"),
                new BigDecimal("50.00"), "tx-expired", auction.getExpiresAt()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("expired");
        assertThat(auction.getCurrentFunding().amount()).isEqualByComparingTo("0.00");
    }

    private Auction evaluatedAuction() {
        Auction auction = new Auction(new InvoiceId("invoice-1"), new UserId("mype-1"),
                money("10000.00"), false, "20123456789", "Pagador S.A.", DUE_DATE);
        auction.evaluate("assessment-1", ScoreGrade.B, true, NOW);
        return auction;
    }

    private Auction publishedAuction() {
        Auction auction = evaluatedAuction();
        AuctionFinancialQuote quote = quote(auction);
        auction.acceptQuote(quote.getId(), NOW, Duration.ofDays(7), Duration.ofDays(1), ZoneId.of("America/Lima"));
        return auction;
    }

    private AuctionFinancialQuote quote(Auction auction) {
        return auction.createQuote(calculator.calculate(AuctionPricingCalculatorTest.pricingParameters(),
                auction.getFundableAmount(), ScoreGrade.B, TODAY, DUE_DATE), NOW, Duration.ofHours(24));
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), Currency.PEN);
    }
}
