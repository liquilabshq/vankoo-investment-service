package com.liquilabs.vankoo.investment.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;

@Embeddable
public record RiskScore(
        @Enumerated(EnumType.STRING) ScoreGrade grade,
        int score,
        LocalDateTime calculatedAt
) {
    public RiskScore {
        if (grade == null) throw new IllegalArgumentException("Grade cannot be null");
        if (score < 0 || score > 100) throw new IllegalArgumentException("Score must be between 0 and 100");
        if (calculatedAt == null) throw new IllegalArgumentException("Calculation date cannot be null");
    }

    public boolean isLowRisk() { return grade == ScoreGrade.A; }
    public boolean isMediumRisk() { return grade == ScoreGrade.B; }
    public boolean isHighRisk() { return grade == ScoreGrade.C; }
}