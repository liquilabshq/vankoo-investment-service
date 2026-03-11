package com.liquilabs.vankoo.investment.domain.model.events;


public record AuctionFullyFundedEvent(
        String auctionId
) {}