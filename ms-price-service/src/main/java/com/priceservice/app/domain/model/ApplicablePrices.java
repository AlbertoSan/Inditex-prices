package com.priceservice.app.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Value object that encapsulates the set of prices that apply to a query and
 * the core business rule for resolving conflicts: when several prices overlap,
 * the one with the highest {@code priority} wins.
 * <p>
 * Pure domain: no framework dependencies.
 */
public class ApplicablePrices {

    private final List<Price> prices;

    public ApplicablePrices(List<Price> prices) {
        this.prices = List.copyOf(prices);
    }

    public boolean isEmpty() {
        return prices.isEmpty();
    }

    /**
     * Returns the applicable price (highest priority), or empty if none applies.
     */
    public Optional<Price> highestPriority() {
        return prices.stream().max(Comparator.comparingInt(Price::getPriority));
    }
}
