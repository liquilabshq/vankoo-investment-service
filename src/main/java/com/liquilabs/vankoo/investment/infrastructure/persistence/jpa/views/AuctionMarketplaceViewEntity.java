package com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.views;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class AuctionMarketplaceViewEntity {

    @Id
    private String auctionId;

    private String invoiceId;
    private String mypeId;

    private BigDecimal targetAmount;
    private BigDecimal currentFunding;
    private String currency;
    private String status;
    private boolean greenCertified;
    private LocalDateTime expiresAt;

    private String payerRuc;
    private String payerName;
    private LocalDateTime dueDate;


    // Datos extraídos del Profile Service (en caso se implemente)
    //private String mypeName;
}