package com.liquilabs.vankoo.investment.domain.services;

import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.queries.AuctionMarketplaceView;
import com.liquilabs.vankoo.investment.domain.model.queries.GetAuctionByIdQuery;
import com.liquilabs.vankoo.investment.domain.model.queries.GetAllActiveAuctionsQuery;
import com.liquilabs.vankoo.investment.domain.model.queries.GetMarketplaceAuctionsQuery;

import java.util.List;
import java.util.Optional;

public interface AuctionQueryService {
    Optional<Auction> handle(GetAuctionByIdQuery query);
    List<Auction> handle(GetAllActiveAuctionsQuery query);
    List<AuctionMarketplaceView> handle(GetMarketplaceAuctionsQuery query);
}