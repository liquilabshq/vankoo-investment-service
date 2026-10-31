package com.liquilabs.vankoo.investment.interfaces.events.resources;

public record AuctionRejectedIntegrationEventData(
        String invoiceId,
        String mypeId,
        String assessmentId,
        String reasonCode,
        String reason,
        String status
) implements AuctionLifecycleEventData {
}
