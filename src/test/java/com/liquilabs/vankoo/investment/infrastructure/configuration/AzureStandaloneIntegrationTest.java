package com.liquilabs.vankoo.investment.infrastructure.configuration;

import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.Currency;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.InvoiceId;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.Money;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.UserId;
import com.liquilabs.vankoo.investment.infrastructure.messaging.outbox.OutboxEventRepository;
import com.liquilabs.vankoo.investment.infrastructure.messaging.outbox.OutboxPublisher;
import com.liquilabs.vankoo.investment.infrastructure.messaging.outbox.OutboxStatus;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.repositories.AuctionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "INVESTMENT_DB_URL=jdbc:h2:mem:azureStandalone;MODE=Oracle;DB_CLOSE_DELAY=-1",
        "INVESTMENT_DB_USER=sa", "INVESTMENT_DB_PASSWORD=", "JWT_SECRET=test-only-not-a-real-secret",
        "INVESTMENT_PUBLIC_URL=http://localhost:8082",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false"
})
@ActiveProfiles("azure")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AzureStandaloneIntegrationTest {
    @Autowired ApplicationContext context;
    @Autowired AuctionRepository auctions;
    @Autowired OutboxEventRepository outbox;
    @Autowired TransactionTemplate transactions;

    @Test
    void azureStartsWithoutKafkaAndKeepsCommittedEventsPending() {
        var bindings = context.getBean(BindingService.class);
        assertThat(bindings.getConsumerBindings("processInvoiceEligibleForFunding-in-0")).isEmpty();
        assertThat(bindings.getConsumerBindings("projectAuctionMarketplace-in-0")).isEmpty();
        assertThat(context.containsBean("processInvoiceEligibleForFunding")).isFalse();
        assertThat(context.containsBean("projectAuctionMarketplace")).isFalse();
        assertThat(context.getBeansOfType(OutboxPublisher.class)).isEmpty();
        var auction = new Auction(new InvoiceId("standalone-invoice"), new UserId("standalone-mype"),
                new Money(new BigDecimal("10000.00"), Currency.PEN), false,
                "20123456789", "Academic demo", LocalDate.now().plusMonths(3));
        auction.registerAuctionCreatedEvent(Instant.now());
        transactions.executeWithoutResult(status -> auctions.save(auction));

        assertThat(auctions.findById(auction.getId())).isPresent();
        assertThat(outbox.findByAggregateIdOrderBySequence(auction.getId().uuid()))
                .singleElement().satisfies(event -> {
                    assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
                    assertThat(event.getPayload()).contains("AuctionCreated");
                });
    }
}
