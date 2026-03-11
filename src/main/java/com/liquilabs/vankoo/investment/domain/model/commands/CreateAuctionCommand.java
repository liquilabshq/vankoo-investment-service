package com.liquilabs.vankoo.investment.domain.model.commands;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.InvoiceId;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.Money;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.RiskScore;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.UserId;

import java.time.LocalDateTime;

public record CreateAuctionCommand(
        InvoiceId invoiceId,
        UserId mypeId,
        Money invoiceAmount,
        RiskScore riskScore,
        boolean greenCertified,

        String payerRuc,
        String payerName,
        LocalDateTime dueDate
) {}