package com.liquilabs.vankoo.investment.application.internal.queryservices;

import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.queries.AuctionMarketplaceView;
import com.liquilabs.vankoo.investment.domain.model.queries.GetAllActiveAuctionsQuery;
import com.liquilabs.vankoo.investment.domain.model.queries.GetAuctionByIdQuery;
import com.liquilabs.vankoo.investment.domain.model.queries.GetMarketplaceAuctionsQuery;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionStatus;
import com.liquilabs.vankoo.investment.domain.services.AuctionQueryService;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.repositories.AuctionMarketplaceViewRepository;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.repositories.AuctionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionQueryServiceImpl implements AuctionQueryService {

    private final AuctionRepository auctionRepository;
    private final AuctionMarketplaceViewRepository viewRepository;

    public AuctionQueryServiceImpl(AuctionRepository auctionRepository, AuctionMarketplaceViewRepository viewRepository) {
        this.auctionRepository = auctionRepository;
        this.viewRepository = viewRepository;
    }

    @Override
    public Optional<Auction> handle(GetAuctionByIdQuery query) {
        return auctionRepository.findById(query.auctionId());
    }

    @Override
    public List<Auction> handle(GetAllActiveAuctionsQuery query) {
        return auctionRepository.findAll();
    }

    @Override
    public List<AuctionMarketplaceView> handle(GetMarketplaceAuctionsQuery query) {
        var activeStatuses = List.of(
                AuctionStatus.PUBLISHED.name(),
                AuctionStatus.FUNDING.name()
        );
        var infraViews = viewRepository.findByStatusIn(activeStatuses);

        return infraViews.stream().map(infraView -> new AuctionMarketplaceView(
                infraView.getAuctionId(),
                infraView.getInvoiceId(),
                infraView.getTargetAmount(),
                infraView.getCurrentFunding(),
                infraView.getCurrency(),
                BigDecimal.ZERO,
                AuctionStatus.valueOf(infraView.getStatus()),
                infraView.getExpiresAt(),
                infraView.isGreenCertified(),
                infraView.getMypeId(),
                //infraView.getMypeName(),
                null      // RiskGrade se implementará en el futuro
        )).toList();
    }
}