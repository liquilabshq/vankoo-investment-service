package com.liquilabs.vankoo.investment.domain.model.valueobjects;

import java.util.UUID;

public record AuctionId(String uuid) {
    public AuctionId() {
        this(UUID.randomUUID().toString());
    }

    public AuctionId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("AuctionId cannot be null or blank");
        }
    }
}