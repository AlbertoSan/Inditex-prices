package com.priceservice.app.application.port.in;

import com.priceservice.app.domain.model.Price;

/**
 * Driving (input) port. Defines the application's use case as seen by
 * the adapters that trigger it (e.g. the REST controller).
 */
public interface GetPriceUseCase {

    /**
     * Returns the applicable price for the given product, brand and date,
     * resolving conflicts by highest priority.
     *
     * @throws com.priceservice.app.domain.exception.PriceNotFoundException   no price applies
     * @throws com.priceservice.app.domain.exception.BrandNotFoundException   brand has no prices at all
     * @throws com.priceservice.app.domain.exception.ProductNotFoundException product has no prices at all
     */
    Price getPrice(GetPriceQuery query);
}
