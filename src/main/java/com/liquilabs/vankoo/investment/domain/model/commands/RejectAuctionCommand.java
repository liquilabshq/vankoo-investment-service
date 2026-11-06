package com.liquilabs.vankoo.investment.domain.model.commands;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionId;

import java.time.Instant;

public record RejectAuctionCommand(
        AuctionId auctionId,
        String assessmentId,
        String reasonCode,
        String reason,
        Instant assessedAt
) {
}
