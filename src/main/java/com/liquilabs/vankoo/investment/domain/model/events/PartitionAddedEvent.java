package com.liquilabs.vankoo.investment.domain.model.events;

import java.math.BigDecimal;

public record PartitionAddedEvent(
        String auctionId,
        String partitionId,
        BigDecimal addedAmount,
        BigDecimal newCurrentFunding
) {}