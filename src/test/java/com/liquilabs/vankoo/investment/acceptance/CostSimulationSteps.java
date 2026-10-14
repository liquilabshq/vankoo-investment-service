package com.liquilabs.vankoo.investment.acceptance;

import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
import com.liquilabs.vankoo.investment.domain.services.AuctionPricingCalculator;
import com.liquilabs.vankoo.investment.domain.services.AuctionPricingCalculatorTest;
import com.liquilabs.vankoo.investment.domain.services.FinancialCalculation;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class CostSimulationSteps {
    private final AuctionPricingCalculator calculator = new AuctionPricingCalculator();
    private final LocalDate today = LocalDate.of(2026, 9, 25);
    private ScoreGrade grade = ScoreGrade.B;
    private BigDecimal amount = new BigDecimal("10000.00");
    private LocalDate dueDate = today.plusDays(60);
    private FinancialCalculation calculation;
    private RuntimeException rejection;
    private Auction auction;

    @Given("^que la factura tiene (.*)$")
    public void invoiceWith(String condition) {
        switch (condition) {
            case "monto, moneda, plazo y clasificación de riesgo válidos" -> { }
            case "una clasificación de riesgo ausente" -> grade = null;
            case "un monto fuera del rango permitido" -> amount = BigDecimal.ZERO;
            case "un plazo fuera del rango permitido" -> dueDate = today;
            default -> throw new IllegalArgumentException("Unknown example: " + condition);
        }
    }

    @When("el empresario solicita la simulación del adelanto")
    public void requestSimulation() {
        try {
            auction = new Auction(new InvoiceId("invoice-bdd"), new UserId("mype-bdd"),
                    new Money(amount, Currency.PEN), false,
                    "20123456789", "Pagador S.A.", dueDate);
            if (grade != null) {
                auction.evaluate("assessment-bdd", grade, true, today.atStartOfDay(ZoneOffset.UTC).toInstant());
            }
            calculation = calculator.calculate(AuctionPricingCalculatorTest.pricingParameters(),
                    auction.getInvoiceAmount(), grade, today, dueDate);
        } catch (RuntimeException exception) {
            rejection = exception;
        }
    }

    @Then("el sistema informa el monto bruto, el descuento y la comisión")
    public void reportsPricingComponents() {
        assertThat(rejection).isNull();
        assertThat(calculation.fundableAmount()).isEqualByComparingTo("10000.00");
        assertThat(calculation.investorGrossProfit()).isPositive();
        assertThat(calculation.platformFeeTotal()).isPositive();
    }

    @Then("muestra el monto neto que recibiría el empresario")
    public void reportsNetAdvance() {
        assertThat(calculation.mypeAdvance()).isPositive();
        assertThat(calculation.mypeAdvance().add(calculation.mypeTotalCost()))
                .isEqualByComparingTo(calculation.fundableAmount());
    }

    @Then("muestra el importe estimado para el inversionista")
    public void reportsInvestorReturn() {
        assertThat(calculation.fundingTarget().add(calculation.investorGrossProfit()))
                .isEqualByComparingTo(calculation.fundableAmount());
    }

    @Then("el sistema rechaza la simulación e informa el dato que debe corregirse")
    public void rejectsInvalidInput() {
        assertThat(rejection).isInstanceOf(IllegalArgumentException.class);
        assertThat(rejection.getMessage()).isNotBlank();
        assertThat(calculation).isNull();
    }

    @Then("no permite crear una subasta con esos datos")
    public void cannotPublishInvalidCandidate() {
        assertThat(auction == null || auction.getStatus() != AuctionStatus.PUBLISHED).isTrue();
    }
}
