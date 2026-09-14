package com.liquilabs.vankoo.investment.domain.services;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.ScoreGrade;

import java.math.BigDecimal;

public interface AuctionPricingPolicy {

    String version();

    int dayCountBasis();

    BigDecimal platformMonthlyFeeRate();

    BigDecimal platformFeeTaxRate();

    BigDecimal teaFor(ScoreGrade grade);
}
