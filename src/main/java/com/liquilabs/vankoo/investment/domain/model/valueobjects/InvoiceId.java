package com.liquilabs.vankoo.investment.domain.model.valueobjects;

import java.util.UUID;

public record InvoiceId(String uuid) {
    public InvoiceId() {
        this(UUID.randomUUID().toString());
    }
    public InvoiceId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("InvoiceId cannot be null or blank");
        }
    }
}