package com.priceservice.app.application.port.out;

import com.priceservice.app.domain.model.Price;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Driven (output) port. The application defines what it needs from the
 * persistence side; infrastructure provides the implementation.
 */
public interface LoadPricePort {

    /**
     * Prices whose date range contains {@code date} for the given product and brand.
     */
    List<Price> findApplicablePrices(Integer productId, Integer brandId, OffsetDateTime date);

    boolean existsByBrandId(Integer brandId);

    boolean existsByProductId(Integer productId);
}
