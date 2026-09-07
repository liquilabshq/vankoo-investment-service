package com.liquilabs.vankoo.investment.domain.model.commands;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionId;

public record AcceptFinancialQuoteCommand(AuctionId auctionId, String quoteId) {
}
