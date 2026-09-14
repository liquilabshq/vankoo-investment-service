package com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.views;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AuctionMarketplaceViewRepository extends JpaRepository<AuctionMarketplaceViewEntity, String> {

    List<AuctionMarketplaceViewEntity> findByStatusInOrderByExpiresAtAscAuctionIdAsc(
            Collection<AuctionStatus> statuses
    );
}
