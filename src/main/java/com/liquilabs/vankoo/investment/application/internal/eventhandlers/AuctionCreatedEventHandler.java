package com.liquilabs.vankoo.investment.application.internal.eventhandlers;

import com.liquilabs.vankoo.investment.domain.model.events.AuctionCreatedEvent;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.repositories.AuctionMarketplaceViewRepository;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.views.AuctionMarketplaceViewEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AuctionCreatedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionCreatedEventHandler.class);

    private final AuctionMarketplaceViewRepository viewRepository;

    public AuctionCreatedEventHandler(AuctionMarketplaceViewRepository viewRepository) {
        this.viewRepository = viewRepository;
    }

    @EventListener
    public void on(AuctionCreatedEvent event) {
        LOGGER.info("Sincronizando Auction {} con la tabla de lectura del Marketplace", event.auctionId());

        var view = new AuctionMarketplaceViewEntity();
        view.setAuctionId(event.auctionId());
        view.setInvoiceId(event.invoiceId());
        view.setMypeId(event.mypeId());
        view.setPayerRuc(event.payerRuc());
        view.setPayerName(event.payerName());
        view.setDueDate(event.dueDate());
        view.setTargetAmount(event.invoiceAmount());
        view.setCurrency(event.currency());
        view.setStatus(event.status().name());
        view.setGreenCertified(event.greenCertified());
        view.setCurrentFunding(BigDecimal.ZERO);

        viewRepository.save(view);
    }
}