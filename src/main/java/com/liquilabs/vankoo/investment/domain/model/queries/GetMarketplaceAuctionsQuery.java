package com.liquilabs.vankoo.investment.domain.model.queries;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.Currency;

import java.util.Optional;

public record GetMarketplaceAuctionsQuery(
        Optional<Currency> currencyFilter,
        Optional<Boolean> onlyGreenCertified
) {}