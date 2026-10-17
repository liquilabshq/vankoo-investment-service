package com.liquilabs.vankoo.investment.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * One of the criteria the risk service checked to call an auction green, e.g. {@code IN_FORCE}.
 *
 * @param code   the criterion, as the risk service names it
 * @param detail what it found, for the investor to read; optional
 */
@Embeddable
public record GreenCriterion(String code, String detail) {
    public GreenCriterion {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Green criterion code is required");
        }
        code = code.trim();
        if (code.length() > 40) {
            throw new IllegalArgumentException("Green criterion code cannot exceed 40 characters");
        }
        detail = detail == null || detail.isBlank() ? null : detail.trim();
        if (detail != null && detail.length() > 300) {
            throw new IllegalArgumentException("Green criterion detail cannot exceed 300 characters");
        }
    }
}
