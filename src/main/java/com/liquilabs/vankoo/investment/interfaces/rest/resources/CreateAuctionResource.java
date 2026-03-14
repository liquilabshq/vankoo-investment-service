package com.liquilabs.vankoo.investment.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateAuctionResource(
        String invoiceId,
        String mypeId,
        BigDecimal invoiceAmount,
        String currency,
        boolean greenCertified,
        String payerRuc,
        String payerName,
        LocalDateTime dueDate
) {}