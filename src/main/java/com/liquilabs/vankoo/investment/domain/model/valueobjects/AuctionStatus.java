package com.liquilabs.vankoo.investment.domain.model.valueobjects;

import java.util.List;

public enum AuctionStatus {
    PENDING_VERIFICATION_RISK,
    DRAFT,
    PUBLISHED,
    FUNDING,
    FULLY_FUNDED,
    CLOSED,
    EXPIRED,
    CANCELLED;

    public static List<String> marketplaceActiveStatuses() {
        return List.of(
                PUBLISHED.name(),
                FUNDING.name(),
                PENDING_VERIFICATION_RISK.name() // Probar
        );
    }
}