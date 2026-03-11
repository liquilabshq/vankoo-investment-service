package com.liquilabs.vankoo.investment.domain.services;

import com.liquilabs.vankoo.investment.domain.model.commands.CreateAuctionCommand;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionId;

import java.util.Optional;

public interface AuctionCommandService {
    Optional<AuctionId> handle(CreateAuctionCommand command);
}