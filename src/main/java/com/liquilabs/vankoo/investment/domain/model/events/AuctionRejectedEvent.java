package com.liquilabs.vankoo.investment.domain.model.events;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionStatus;

import java.time.Instant;

public record AuctionRejectedEvent(
        String auctionId,
        String invoiceId,
        String mypeId,
        String assessmentId,
        String reasonCode,
        String reason,
        AuctionStatus status,
        Instant occurredAt
) {
}
