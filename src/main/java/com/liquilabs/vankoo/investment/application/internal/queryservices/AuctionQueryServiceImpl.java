package com.liquilabs.vankoo.investment.application.internal.queryservices;

import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.queries.AuctionMarketplaceView;
import com.liquilabs.vankoo.investment.domain.model.queries.GetAllActiveAuctionsQuery;
import com.liquilabs.vankoo.investment.domain.model.queries.GetAuctionByIdQuery;
import com.liquilabs.vankoo.investment.domain.model.queries.GetMarketplaceAuctionsQuery;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionStatus;
import com.liquilabs.vankoo.investment.domain.services.AuctionQueryService;
import com.liquilabs.vankoo.investment.infrastructure.configuration.PricingProperties;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.repositories.AuctionRepository;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.views.AuctionMarketplaceViewEntity;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.views.AuctionMarketplaceViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuctionQueryServiceImpl implements AuctionQueryService {

    private final AuctionRepository auctionRepository;
    private final AuctionMarketplaceViewRepository marketplaceViewRepository;
    private final PricingProperties pricingProperties;
    private final Clock clock;

    public AuctionQueryServiceImpl(
            AuctionRepository auctionRepository,
            AuctionMarketplaceViewRepository marketplaceViewRepository,
            PricingProperties pricingProperties,
            Clock clock
    ) {
        this.auctionRepository = auctionRepository;
        this.marketplaceViewRepository = marketplaceViewRepository;
        this.pricingProperties = pricingProperties;
        this.clock = clock;
    }

    @Override
    public Optional<Auction> handle(GetAuctionByIdQuery query) {
        return auctionRepository.findDetailedById(query.auctionId());
    }

    @Override
    public List<Auction> handle(GetAllActiveAuctionsQuery query) {
        return auctionRepository.findByStatusIn(List.of(AuctionStatus.PUBLISHED, AuctionStatus.FUNDING));
    }

    @Override
    public List<AuctionMarketplaceView> handle(GetMarketplaceAuctionsQuery query) {
        LocalDate today = LocalDate.now(clock.withZone(pricingProperties.pricingZone()));
        return marketplaceViewRepository
                .findByStatusInOrderByExpiresAtAscAuctionIdAsc(
                        List.of(AuctionStatus.PUBLISHED, AuctionStatus.FUNDING)
                )
                .stream()
                .filter(view -> query.currencyFilter().map(value -> value == view.getCurrency()).orElse(true))
                .filter(view -> query.onlyGreenCertified().map(value -> !value || view.isGreenCertified()).orElse(true))
                .map(view -> toMarketplaceView(view, today))
                .toList();
    }

    private AuctionMarketplaceView toMarketplaceView(AuctionMarketplaceViewEntity auction, LocalDate today) {
        BigDecimal available = auction.getTargetAmount().subtract(auction.getCurrentFunding());
        BigDecimal progress = auction.getCurrentFunding()
                .multiply(new BigDecimal("100"))
                .divide(auction.getTargetAmount(), 4, RoundingMode.HALF_UP);

        return new AuctionMarketplaceView(
                auction.getAuctionId(),
                auction.getInvoiceId(),
                auction.getMypeId(),
                auction.getPayerRuc(),
                auction.getPayerName(),
                auction.getTargetAmount(),
                auction.getCurrentFunding(),
                available,
                progress,
                auction.getCurrency().name(),
                toPercentagePoints(auction.getInvestorTea()),
                toPercentagePoints(auction.getInvestorTermRate()),
                auction.getQuotedTermDays(),
                Math.max(0, ChronoUnit.DAYS.between(today, auction.getDueDate())),
                auction.getRiskGrade(),
                auction.getStatus(),
                auction.getDueDate(),
                auction.getExpiresAt(),
                auction.isGreenCertified()
        );
    }

    private static BigDecimal toPercentagePoints(BigDecimal fraction) {
        return fraction.multiply(new BigDecimal("100")).setScale(6, RoundingMode.HALF_UP);
    }
}
