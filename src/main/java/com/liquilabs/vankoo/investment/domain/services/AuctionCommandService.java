package com.liquilabs.vankoo.investment.domain.services;

import com.liquilabs.vankoo.investment.domain.model.commands.AddPartitionCommand;
import com.liquilabs.vankoo.investment.domain.model.commands.CreateAuctionCommand;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionId;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.PartitionId;

import java.util.Optional;

public interface AuctionCommandService {
    Optional<AuctionId> handle(CreateAuctionCommand command);
    Optional<PartitionId> handle(AddPartitionCommand command);
}