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
    private final PricingProperties pricingProperties;
    private final Clock clock;

    public AuctionQueryServiceImpl(AuctionRepository auctionRepository, PricingProperties pricingProperties, Clock clock) {
        this.auctionRepository = auctionRepository;
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
        return auctionRepository.findByStatusInOrderByExpiresAtAsc(List.of(AuctionStatus.PUBLISHED, AuctionStatus.FUNDING))
                .stream()
                .map(auction -> toMarketplaceView(auction, today))
                .toList();
    }

    private AuctionMarketplaceView toMarketplaceView(Auction auction, LocalDate today) {
        var quote = auction.acceptedQuote();
        BigDecimal available = auction.getTargetAmount().amount().subtract(auction.getCurrentFunding().amount());
        BigDecimal progress = auction.getCurrentFunding().amount()
                .multiply(new BigDecimal("100"))
                .divide(auction.getTargetAmount().amount(), 4, RoundingMode.HALF_UP);

        return new AuctionMarketplaceView(
                auction.getId().uuid(),
                auction.getInvoiceId().uuid(),
                auction.getMypeId().uuid(),
                auction.getPayerRuc(),
                auction.getPayerName(),
                auction.getTargetAmount().amount(),
                auction.getCurrentFunding().amount(),
                available,
                progress,
                auction.getTargetAmount().currency().name(),
                toPercentagePoints(quote.getInvestorTea()),
                toPercentagePoints(quote.getInvestorTermRate()),
                quote.getTermDays(),
                Math.max(0, ChronoUnit.DAYS.between(today, auction.getDueDate())),
                auction.getRiskScore().grade(),
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
