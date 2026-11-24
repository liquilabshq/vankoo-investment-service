package com.liquilabs.vankoo.investment.domain.model.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PartitionAddedEvent(
        String auctionId,
        String partitionId,
        // Quién compró la participación: Notification le avisa la «inversión realizada».
        String investorId,
        BigDecimal addedAmount,
        String currency,
        BigDecimal newCurrentFunding,
        Instant occurredAt
) {}
