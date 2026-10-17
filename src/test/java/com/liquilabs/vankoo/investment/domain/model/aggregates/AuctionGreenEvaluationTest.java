package com.liquilabs.vankoo.investment.domain.model.aggregates;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuctionGreenEvaluationTest {

    private static final Instant NOW = Instant.parse("2026-10-16T17:00:00Z");

    @Test
    void aGreenEvidenceMakesTheAuctionGreenAndKeepsItsCriteria() {
        Auction auction = newAuction(false);

        auction.evaluate("risk-1", ScoreGrade.B, true, NOW, evidence("cert-1"));

        assertThat(auction.isGreenCertified()).isTrue();
        assertThat(auction.greenEvidence()).isEqualTo(evidence("cert-1"));
        assertThat(auction.greenEvidence().criteria())
                .extracting(GreenCriterion::code)
                .containsExactly("ACCEPTED_STANDARD", "IN_FORCE", "NOT_ENVIRONMENTAL_OFFENDER");
    }

    @Test
    void anAuctionIsNotGreenWithoutEvidenceEvenIfItWasCreatedAsGreen() {
        Auction auction = newAuction(true);

        auction.evaluate("risk-1", ScoreGrade.B, true, NOW);

        assertThat(auction.isGreenCertified()).isFalse();
        assertThat(auction.greenEvidence()).isNull();
    }

    @Test
    void aNewAssessmentWithoutEvidenceRemovesThePreviousGreen() {
        Auction auction = newAuction(false);
        auction.evaluate("risk-1", ScoreGrade.B, true, NOW, evidence("cert-1"));

        auction.evaluate("risk-2", ScoreGrade.A, true, NOW.plusSeconds(60));

        assertThat(auction.isGreenCertified()).isFalse();
        assertThat(auction.greenEvidence()).isNull();
        assertThat(auction.getGreenCriteria()).isEmpty();
    }

    @Test
    void repeatingAnAssessmentWithTheSameEvidenceInAnotherOrderIsIgnored() {
        Auction auction = newAuction(false);
        auction.evaluate("risk-1", ScoreGrade.B, true, NOW, evidence("cert-1"));
        GreenEvidence reordered = new GreenEvidence(certification("cert-1"), List.of(
                new GreenCriterion("NOT_ENVIRONMENTAL_OFFENDER", null),
                new GreenCriterion("IN_FORCE", "Vigente hasta el 2028-03-31"),
                new GreenCriterion("ACCEPTED_STANDARD", "ISO 14001:2015")
        ));

        auction.evaluate("risk-1", ScoreGrade.B, true, NOW, reordered);

        assertThat(auction.greenEvidence()).isEqualTo(evidence("cert-1"));
    }

    @Test
    void repeatingAnAssessmentWithOtherEvidenceIsRefused() {
        Auction auction = newAuction(false);
        auction.evaluate("risk-1", ScoreGrade.B, true, NOW, evidence("cert-1"));

        assertThatThrownBy(() -> auction.evaluate("risk-1", ScoreGrade.B, true, NOW))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different data");
        assertThatThrownBy(() -> auction.evaluate("risk-1", ScoreGrade.B, true, NOW, evidence("cert-2")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void evidenceNeedsAtLeastOneCriterionAndNoRepeatedCodes() {
        assertThatThrownBy(() -> new GreenEvidence(certification("cert-1"), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GreenEvidence(certification("cert-1"), List.of(
                new GreenCriterion("IN_FORCE", null), new GreenCriterion("IN_FORCE", "otra vez"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("repeat");
    }

    @Test
    void aCertificateNeedsItsIssuerAndExpiryDate() {
        assertThatThrownBy(() -> new GreenCertification("cert-1", GreenStandard.ISO_14001, " ", LocalDate.of(2028, 3, 31)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GreenCertification("cert-1", GreenStandard.ISO_14001, "SGS del Perú", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static GreenEvidence evidence(String certificationId) {
        return new GreenEvidence(certification(certificationId), List.of(
                new GreenCriterion("IN_FORCE", "Vigente hasta el 2028-03-31"),
                new GreenCriterion("ACCEPTED_STANDARD", "ISO 14001:2015"),
                new GreenCriterion("NOT_ENVIRONMENTAL_OFFENDER", null)
        ));
    }

    private static GreenCertification certification(String certificationId) {
        return new GreenCertification(certificationId, GreenStandard.ISO_14001, "SGS del Perú S.A.C.", LocalDate.of(2028, 3, 31));
    }

    private static Auction newAuction(boolean greenCertified) {
        return new Auction(
                new InvoiceId("invoice-green-1"),
                new UserId("mype-green-1"),
                new Money(new BigDecimal("10000.00"), Currency.PEN),
                greenCertified,
                "20123456789",
                "Pagador S.A.",
                LocalDate.of(2026, 12, 31)
        );
    }
}
