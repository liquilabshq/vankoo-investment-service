package com.liquilabs.vankoo.investment.interfaces.events.resources;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Optional on the wire (schema v1 allowed {@code AuctionFullyFunded} to be empty),
 * so records published before these fields existed still validate. Investment always
 * fills them now; Finance reads {@code mypeId}, {@code currency} and
 * {@code mypeAdvance} to pay the MYPE.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuctionFullyFundedIntegrationEventData(
        String invoiceId,
        String mypeId,
        String currency,
        BigDecimal fundedAmount,
        BigDecimal mypeAdvance
) implements AuctionLifecycleEventData {
}
