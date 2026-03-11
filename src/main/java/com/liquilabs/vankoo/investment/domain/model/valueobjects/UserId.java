package com.liquilabs.vankoo.investment.domain.model.valueobjects;

import java.util.UUID;

public record UserId(String uuid) {
    public UserId() {
        this(UUID.randomUUID().toString());
    }
    public UserId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("UserId cannot be null or blank");
        }
    }
}