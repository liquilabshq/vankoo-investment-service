package com.liquilabs.vankoo.investment.application.internal.commandservices;

import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.commands.CreateAuctionCommand;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionId;
import com.liquilabs.vankoo.investment.domain.services.AuctionCommandService;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.repositories.AuctionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuctionCommandServiceImpl implements AuctionCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionCommandServiceImpl.class);

    private final AuctionRepository auctionRepository;

    public AuctionCommandServiceImpl(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    @Override
    @Transactional
    public Optional<AuctionId> handle(CreateAuctionCommand command) {
        var existing = auctionRepository.findByInvoiceId(command.invoiceId());

        if (existing.isPresent()) {
            LOGGER.info("Subasta ya existente para la factura {}, se reutiliza. ID: {}",
                    command.invoiceId().uuid(), existing.get().getId().uuid());
            return Optional.of(existing.get().getId());
        }

        var auction = new Auction(
                command.invoiceId(),
                command.mypeId(),
                command.invoiceAmount(),
                command.riskScore(),
                command.greenCertified(),
                command.dueDate()
        );

        auction.registerAuctionCreatedEvent(command.payerRuc(), command.payerName());

        auctionRepository.save(auction);

        return Optional.of(auction.getId());
    }
}