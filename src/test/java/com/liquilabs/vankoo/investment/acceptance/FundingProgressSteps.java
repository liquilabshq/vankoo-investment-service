package com.liquilabs.vankoo.investment.acceptance;

import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.entities.Partition;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
import com.liquilabs.vankoo.investment.domain.services.AuctionPricingCalculator;
import com.liquilabs.vankoo.investment.domain.services.AuctionPricingCalculatorTest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps de US07 (seguimiento del fondeo) sobre el agregado {@link Auction} real:
 * se publica una subasta aceptando su cotización y se registra una participación.
 */
public class FundingProgressSteps {

    private static final Instant NOW = Instant.parse("2026-09-25T15:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);
    private static final BigDecimal MINIMUM = new BigDecimal("50.00");
    private static final BigDecimal INVESTMENT = new BigDecimal("1500.00");

    private Auction auction;
    private Partition partition;

    @Given("que la operación está publicada en subasta")
    public void publishedAuction() {
        auction = new Auction(new InvoiceId("invoice-us07"), new UserId("mype-us07"),
                new Money(new BigDecimal("10000.00"), Currency.PEN), false,
                "20123456789", "Pagador S.A.", TODAY.plusDays(60));
        auction.evaluate("assessment-us07", ScoreGrade.A, true, NOW);
        var calculation = new AuctionPricingCalculator().calculate(
                AuctionPricingCalculatorTest.pricingParameters(), auction.getFundableAmount(),
                ScoreGrade.A, TODAY, TODAY.plusDays(60));
        var quote = auction.createQuote(calculation, NOW, Duration.ofHours(24));
        auction.acceptQuote(quote.getId(), NOW, Duration.ofDays(7), Duration.ofDays(1), ZoneId.of("America/Lima"));

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.PUBLISHED);
    }

    @Given("todavía tiene un saldo pendiente de fondeo")
    public void hasOutstandingBalance() {
        assertThat(auction.getCurrentFunding().amount()).isEqualByComparingTo("0.00");
        assertThat(auction.getTargetAmount().amount()).isGreaterThan(INVESTMENT);
    }

    @When("se registra una nueva participación de un inversionista")
    public void registersParticipation() {
        partition = auction.addInvestment(new UserId("investor-us07"),
                new Money(INVESTMENT, Currency.PEN), MINIMUM, "tx-us07-1", NOW.plusSeconds(60));
    }

    @Then("el sistema actualiza el monto financiado")
    public void updatesFundedAmount() {
        assertThat(auction.getCurrentFunding().amount()).isEqualByComparingTo(INVESTMENT);
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.FUNDING);
        assertThat(auction.getPartitions()).containsExactly(partition);
    }

    @Then("muestra el porcentaje de avance y el saldo pendiente")
    public void reportsProgressAndBalance() {
        BigDecimal target = auction.getTargetAmount().amount();
        BigDecimal expectedPct = INVESTMENT.multiply(new BigDecimal("100")).divide(target, 8, RoundingMode.HALF_UP);
        BigDecimal remaining = target.subtract(auction.getCurrentFunding().amount());

        assertThat(partition.getPercentage().value()).isEqualByComparingTo(expectedPct);
        assertThat(remaining).isEqualByComparingTo(target.subtract(INVESTMENT));
        assertThat(auction.isFunded()).isFalse();
    }
}
