package com.liquilabs.vankoo.investment.infrastructure.persistence;

import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
import com.liquilabs.vankoo.investment.domain.services.AuctionPricingCalculator;
import com.liquilabs.vankoo.investment.domain.services.AuctionPricingCalculatorTest;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.repositories.AuctionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class AuctionRepositoryTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAnAcceptedQuoteAndItsPartitions() {
        Instant now = Instant.parse("2026-09-07T17:00:00Z");
        LocalDate dueDate = LocalDate.of(2026, 11, 6);
        Auction auction = new Auction(
                new InvoiceId("invoice-persistence"),
                new UserId("mype-persistence"),
                new Money(new BigDecimal("10000.00"), Currency.PEN),
                true,
                "20123456789",
                "Pagador Persistente S.A.",
                dueDate
        );
        auction.evaluate("assessment-persistence", ScoreGrade.B, true, now);
        var calculator = new AuctionPricingCalculator();
        var quote = auction.createQuote(
                calculator.calculate(
                        AuctionPricingCalculatorTest.pricingParameters(),
                        auction.getFundableAmount(), ScoreGrade.B, LocalDate.of(2026, 9, 7), dueDate),
                now,
                Duration.ofHours(24)
        );
        auction.acceptQuote(quote.getId(), now, Duration.ofDays(7), Duration.ofDays(1), ZoneId.of("America/Lima"));
        auction.addInvestment(
                new UserId("investor-persistence"),
                new Money(new BigDecimal("500.00"), Currency.PEN),
                new BigDecimal("500.00"),
                "transaction-persistence",
                now.plusSeconds(1)
        );

        Auction saved = auctionRepository.saveAndFlush(auction);
        entityManager.clear();

        Auction reloaded = auctionRepository.findDetailedById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(AuctionStatus.FUNDING);
        assertThat(reloaded.acceptedQuote().getMypeAdvance()).isEqualByComparingTo("9698.96");
        assertThat(reloaded.getPartitions()).singleElement()
                .satisfies(partition -> assertThat(partition.getExpectedReturn().amount()).isGreaterThan(new BigDecimal("500.00")));
    }
}
