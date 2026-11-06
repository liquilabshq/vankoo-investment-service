package com.liquilabs.vankoo.investment.interfaces.events.resources;

public record AuctionEvaluatedIntegrationEventData(
        String invoiceId,
        String mypeId,
        String assessmentId,
        String riskGrade,
        boolean greenCertified,
        String status
) implements AuctionLifecycleEventData {
}
