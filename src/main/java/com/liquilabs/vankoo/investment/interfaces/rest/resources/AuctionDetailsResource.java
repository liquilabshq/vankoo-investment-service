package com.liquilabs.vankoo.investment.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.UserId;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionStatus;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.ScoreGrade;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AuctionDetailsResource(
        String auctionId,
        String invoiceId,
        String mypeId,
        AuctionStatus status,
        ScoreGrade riskGrade,
        BigDecimal invoiceAmount,
        BigDecimal fundableAmount,
        BigDecimal targetAmount,
        BigDecimal currentFunding,
        String currency,
        String payerRuc,
        String payerName,
        LocalDate dueDate,
        boolean greenCertified,
        GreenCertificationResource greenCertification,
        boolean fullBalanceOutstandingConfirmed,
        String assessmentId,
        Instant assessedAt,
        Instant publishedAt,
        Instant expiresAt,
        Instant closedAt,
        Instant cancelledAt,
        String cancellationReason,
        Instant rejectedAt,
        String rejectionReasonCode,
        String rejectionReason,
        FinancialQuoteResource acceptedQuote,
        // Solo en GET /auctions/investor/{investorId}: las participaciones de quien
        // pregunta en esta subasta, para que su portafolio sepa cuánto puso y cuánto
        // espera cobrar. En el resto de endpoints no viaja: ahí nadie es «yo».
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<InvestmentResponseResource> myInvestments
) {
    public static AuctionDetailsResource from(Auction auction) {
        return from(auction, null);
    }

    /** La subasta vista por un inversionista: lleva sus propias participaciones. */
    public static AuctionDetailsResource forInvestor(Auction auction, UserId investorId) {
        return from(auction, auction.getPartitions().stream()
                .filter(partition -> partition.getInvestorId().equals(investorId))
                .map(InvestmentResponseResource::from)
                .toList());
    }

    private static AuctionDetailsResource from(Auction auction, List<InvestmentResponseResource> myInvestments) {
        FinancialQuoteResource quote = auction.getAcceptedQuoteId() == null
                ? null
                : FinancialQuoteResource.from(auction.acceptedQuote());
        return new AuctionDetailsResource(
                auction.getId().uuid(),
                auction.getInvoiceId().uuid(),
                auction.getMypeId().uuid(),
                auction.getStatus(),
                auction.getRiskScore().grade(),
                auction.getInvoiceAmount().amount(),
                amountOf(auction.getFundableAmount()),
                amountOf(auction.getTargetAmount()),
                auction.getCurrentFunding().amount(),
                auction.getInvoiceAmount().currency().name(),
                auction.getPayerRuc(),
                auction.getPayerName(),
                auction.getDueDate(),
                auction.isGreenCertified(),
                GreenCertificationResource.from(auction.greenEvidence()),
                auction.isFullBalanceOutstandingConfirmed(),
                auction.getAssessmentId(),
                auction.getAssessedAt(),
                auction.getPublishedAt(),
                auction.getExpiresAt(),
                auction.getClosedAt(),
                auction.getCancelledAt(),
                auction.getCancellationReason(),
                auction.getRejectedAt(),
                auction.getRejectionReasonCode(),
                auction.getRejectionReason(),
                quote,
                myInvestments
        );
    }

    private static BigDecimal amountOf(com.liquilabs.vankoo.investment.domain.model.valueobjects.Money money) {
        return money == null ? null : money.amount();
    }
}
