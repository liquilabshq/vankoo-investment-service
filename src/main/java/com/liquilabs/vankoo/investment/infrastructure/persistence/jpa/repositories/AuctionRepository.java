package com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.repositories;

import com.liquilabs.vankoo.investment.domain.model.aggregates.Auction;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionId;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.InvoiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, AuctionId> {

    Optional<Auction> findByInvoiceId(InvoiceId invoiceId);
}