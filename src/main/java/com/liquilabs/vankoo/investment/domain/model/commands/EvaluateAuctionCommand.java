package com.liquilabs.vankoo.investment.domain.model.commands;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionId;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.GreenEvidence;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.ScoreGrade;

import java.time.Instant;

/**
 * @param green why the auction is green, or {@code null} if it is not
 */
public record EvaluateAuctionCommand(
        AuctionId auctionId,
        String assessmentId,
        ScoreGrade riskGrade,
        boolean fullBalanceOutstanding,
        Instant assessedAt,
        GreenEvidence green
) {
    public EvaluateAuctionCommand(
            AuctionId auctionId,
            String assessmentId,
            ScoreGrade riskGrade,
            boolean fullBalanceOutstanding,
            Instant assessedAt
    ) {
        this(auctionId, assessmentId, riskGrade, fullBalanceOutstanding, assessedAt, null);
    }
}
