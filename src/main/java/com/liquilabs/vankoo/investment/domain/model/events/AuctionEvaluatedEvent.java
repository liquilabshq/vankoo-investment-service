package com.liquilabs.vankoo.investment.domain.model.events;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionStatus;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.ScoreGrade;

import java.time.Instant;

public record AuctionEvaluatedEvent(
        String auctionId,
        String invoiceId,
        String mypeId,
        String assessmentId,
        ScoreGrade riskGrade,
        boolean greenCertified,
        AuctionStatus status,
        Instant occurredAt
) {
}
