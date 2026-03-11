package com.liquilabs.vankoo.investment.domain.model.events;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionCreatedEvent(
        String auctionId,
        String invoiceId,
        String mypeId,

        String payerRuc,
        String payerName,
        LocalDateTime dueDate,

        BigDecimal invoiceAmount,
        String currency,
        AuctionStatus status,
        boolean greenCertified
) {
}