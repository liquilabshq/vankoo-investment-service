package com.liquilabs.vankoo.investment.acceptance;

import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.entities.AuctionFinancialQuote;
import com.liquilabs.vankoo.investment.domain.model.events.AuctionPublishedEvent;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
import com.liquilabs.vankoo.investment.domain.services.AuctionPricingCalculator;
import com.liquilabs.vankoo.investment.domain.services.AuctionPricingCalculatorTest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps de US06 (solicitud de subasta) sobre el agregado {@link Auction} real. La MYPE publica
 * la operación al aceptar la cotización del adelanto; antes, Risk tiene que haberla evaluado.
 * Los plazos son los de producción: cotización de 24 h, ventana de 7 días y 1 día de margen.
 */
public class AuctionRequestSteps {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);
    private static final LocalDate DUE_DATE = TODAY.plusDays(60);
    private static final Instant NOW = Instant.parse("2026-09-25T15:00:00Z");
    private static final Duration QUOTE_VALIDITY = Duration.ofHours(24);
    private static final Duration FUNDING_WINDOW = Duration.ofDays(7);
    private static final Duration SETTLEMENT_BUFFER = Duration.ofDays(1);

    private Auction auction;
    private AuctionFinancialQuote quote;
    private Instant acceptedAt = NOW;
    private String missingRequirement;
    private RuntimeException rejection;

    @Given("que la factura fue evaluada con riesgo {string} y su saldo sigue pendiente de cobro")
    public void evaluatedInvoice(String grade) {
        auction = newAuction();
        auction.evaluate("assessment-us06", ScoreGrade.valueOf(grade), true, NOW);
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.DRAFT);
    }

    @Given("la MYPE tiene una cotización vigente del adelanto")
    public void activeQuote() {
        quote = quoteAt(NOW);
    }

    @Given("^que la factura es elegible pero falta (.*)$")
    public void eligibleInvoiceMissing(String requirement) {
        missingRequirement = requirement;
        auction = newAuction();
        switch (requirement) {
            case "la evaluación de riesgo" -> { }
            case "una cotización vigente" -> {
                auction.evaluate("assessment-us06", ScoreGrade.B, true, NOW);
                quote = quoteAt(NOW);
                acceptedAt = NOW.plus(QUOTE_VALIDITY).plusSeconds(1);
            }
            case "un plazo suficiente antes del vencimiento" -> {
                auction.evaluate("assessment-us06", ScoreGrade.B, true, NOW);
                acceptedAt = DUE_DATE.atStartOfDay(LIMA).toInstant().minus(Duration.ofHours(12));
                quote = quoteAt(acceptedAt);
            }
            default -> throw new IllegalArgumentException("Unknown requirement: " + requirement);
        }
    }

    @When("el empresario acepta la cotización para publicar la operación")
    public void acceptsQuote() {
        try {
            if (quote == null) {
                quote = quoteAt(acceptedAt);
            }
            auction.acceptQuote(quote.getId(), acceptedAt, FUNDING_WINDOW, SETTLEMENT_BUFFER, LIMA);
        } catch (RuntimeException exception) {
            rejection = exception;
        }
    }

    @Then("el sistema cambia su estado a {string}")
    public void changesStatusTo(String label) {
        assertThat(rejection).isNull();
        assertThat(label).isEqualTo("En subasta");
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.PUBLISHED);
        assertThat(auction.getPublishedAt()).isEqualTo(NOW);
    }

    @Then("abre la ventana de fondeo antes del vencimiento de la factura")
    public void opensFundingWindow() {
        Instant dueDateLimit = DUE_DATE.atStartOfDay(LIMA).toInstant().minus(SETTLEMENT_BUFFER);
        assertThat(auction.getExpiresAt()).isEqualTo(NOW.plus(FUNDING_WINDOW)).isBefore(dueDateLimit);
        assertThat(auction.getTargetAmount().amount()).isEqualByComparingTo(quote.getFundingTarget());
    }

    @Then("la incorpora al marketplace de oportunidades")
    public void announcesPublication() {
        // El marketplace es una proyección que se alimenta de AuctionPublished.
        assertThat(publishedEvents()).hasSize(1);
    }

    @Then("el sistema rechaza la solicitud e informa el requisito pendiente")
    public void rejectsRequest() {
        String expected = switch (missingRequirement) {
            case "la evaluación de riesgo" -> "must be evaluated";
            case "una cotización vigente" -> "quote is not active";
            case "un plazo suficiente antes del vencimiento" -> "too close to its due date";
            default -> throw new IllegalArgumentException("Unknown requirement: " + missingRequirement);
        };
        assertThat(rejection).isInstanceOf(IllegalStateException.class).hasMessageContaining(expected);
    }

    @Then("la operación no se publica en el marketplace")
    public void notPublished() {
        assertThat(auction.getStatus()).isNotEqualTo(AuctionStatus.PUBLISHED);
        assertThat(auction.getPublishedAt()).isNull();
        assertThat(publishedEvents()).isEmpty();
    }

    private static Auction newAuction() {
        return new Auction(new InvoiceId("invoice-us06"), new UserId("mype-us06"),
                new Money(new BigDecimal("10000.00"), Currency.PEN), false,
                "20123456789", "Pagador S.A.", DUE_DATE);
    }

    private AuctionFinancialQuote quoteAt(Instant at) {
        // Sin evaluación no hay monto financiable ni grado: se cotiza con los datos de la factura
        // para que sea el agregado quien rechace la cotización.
        boolean evaluated = auction.getStatus() != AuctionStatus.PENDING_VERIFICATION_RISK;
        Money amount = evaluated ? auction.getFundableAmount() : auction.getInvoiceAmount();
        ScoreGrade grade = evaluated ? auction.getRiskScore().grade() : ScoreGrade.B;
        var calculation = new AuctionPricingCalculator().calculate(
                AuctionPricingCalculatorTest.pricingParameters(), amount, grade, TODAY, DUE_DATE);
        return auction.createQuote(calculation, at, QUOTE_VALIDITY);
    }

    private Collection<?> publishedEvents() {
        Collection<?> events = ReflectionTestUtils.invokeMethod(auction, "domainEvents");
        return events.stream().filter(AuctionPublishedEvent.class::isInstance).toList();
    }
}
