package com.liquilabs.vankoo.investment.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;

/**
 * The environmental certificate that made an auction green, as the risk service validated it.
 *
 * It only exists for a green auction: an auction without one is not green.
 */
@Embeddable
public record GreenCertification(
        String certificationId,
        @Enumerated(EnumType.STRING) GreenStandard standard,
        String issuer,
        LocalDate validUntil
) {
    public GreenCertification {
        certificationId = requireText(certificationId, "Green certification ID", 100);
        if (standard == null) {
            throw new IllegalArgumentException("Green certification standard is required");
        }
        issuer = requireText(issuer, "Green certification issuer", 200);
        if (validUntil == null) {
            throw new IllegalArgumentException("Green certification expiry date is required");
        }
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(field + " cannot exceed " + maxLength + " characters");
        }
        return normalized;
    }
}
