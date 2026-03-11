package com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.repositories;

import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.views.AuctionMarketplaceView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionMarketplaceViewRepository extends JpaRepository<AuctionMarketplaceView, String> {

    List<AuctionMarketplaceView> findByStatusIn(List<String> statuses);
}