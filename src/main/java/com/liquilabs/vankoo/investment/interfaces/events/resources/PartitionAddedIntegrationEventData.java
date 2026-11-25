package com.liquilabs.vankoo.investment.interfaces.events.resources;

import java.math.BigDecimal;

public record PartitionAddedIntegrationEventData(
        String partitionId,
        String investorId,
        BigDecimal addedAmount,
        String currency,
        BigDecimal currentFunding
) implements AuctionLifecycleEventData {
}
