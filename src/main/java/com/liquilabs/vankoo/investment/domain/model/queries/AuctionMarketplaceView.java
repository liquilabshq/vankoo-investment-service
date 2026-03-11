package com.liquilabs.vankoo.investment.domain.model.queries;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionStatus;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.ScoreGrade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionMarketplaceView(
        String auctionId,
        String invoiceId,

        // Datos de la subasta
        BigDecimal targetAmount,
        BigDecimal currentFunding,
        String currency,
        BigDecimal discountRate,
        AuctionStatus status,
        LocalDateTime expiresAt,
        boolean greenCertified,

        // Datos proyectados
        String mypeId,
        String mypeName,
        String mypeSector,
        ScoreGrade riskGrade
) {}