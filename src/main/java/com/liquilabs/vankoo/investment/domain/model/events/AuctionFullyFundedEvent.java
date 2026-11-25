package com.liquilabs.vankoo.investment.domain.model.events;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The auction reached its funding target.
 *
 * <p>It carries who is owed what, so that Finance can pay the MYPE from this
 * event alone: {@code fundedAmount} is what investors put in (the target) and
 * {@code mypeAdvance} is what the MYPE receives, the accepted quote's
 * {@code mypeAdvance} (target minus the platform fee and its tax).
 */
public record AuctionFullyFundedEvent(
        String auctionId,
        String invoiceId,
        String mypeId,
        String currency,
        BigDecimal fundedAmount,
        BigDecimal mypeAdvance,
        Instant occurredAt
) {}
