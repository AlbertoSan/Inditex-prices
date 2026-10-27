package com.priceservice.app.application.service;

import com.priceservice.app.application.port.in.GetPriceQuery;
import com.priceservice.app.application.port.in.GetPriceUseCase;
import com.priceservice.app.application.port.out.LoadPricePort;
import com.priceservice.app.domain.exception.BrandNotFoundException;
import com.priceservice.app.domain.exception.PriceNotFoundException;
import com.priceservice.app.domain.exception.ProductNotFoundException;
import com.priceservice.app.domain.model.ApplicablePrices;
import com.priceservice.app.domain.model.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service implementing the use case. Orchestrates the call to the
 * output port and delegates the conflict-resolution rule to the domain
 * ({@link ApplicablePrices}). Framework-free: it is wired as a bean in the
 * infrastructure layer ({@code BeanConfiguration}), so the application core
 * stays decoupled from Spring.
 */
@Slf4j
@RequiredArgsConstructor
public class GetPriceService implements GetPriceUseCase {

    private final LoadPricePort loadPricePort;

    @Override
    public Price getPrice(GetPriceQuery query) {
        log.debug("getPrice: productId={}, brandId={}, date={}",
                query.productId(), query.brandId(), query.date());

        ApplicablePrices applicable = new ApplicablePrices(
                loadPricePort.findApplicablePrices(query.productId(), query.brandId(), query.date()));

        return applicable.highestPriority().orElseGet(() -> {
            throw priceNotFound(query);
        });
    }

    private RuntimeException priceNotFound(GetPriceQuery query) {
        if (!loadPricePort.existsByBrandId(query.brandId())) {
            log.error("Brand not found");
            return new BrandNotFoundException("Brand not found");
        }
        if (!loadPricePort.existsByProductId(query.productId())) {
            log.error("Product not found");
            return new ProductNotFoundException("Product not found");
        }
        log.error("Price not found for given parameters");
        return new PriceNotFoundException("Price not found for given parameters");
    }
}
