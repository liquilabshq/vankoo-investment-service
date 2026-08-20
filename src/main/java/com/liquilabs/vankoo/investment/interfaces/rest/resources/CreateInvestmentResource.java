package com.liquilabs.vankoo.investment.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateInvestmentResource(
        @NotBlank String investorId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        @NotNull @Positive BigDecimal returnRate,
        String transactionId
) {}