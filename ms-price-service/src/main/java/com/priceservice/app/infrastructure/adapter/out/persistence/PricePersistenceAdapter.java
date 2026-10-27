package com.priceservice.app.infrastructure.adapter.out.persistence;

import com.priceservice.app.application.port.out.LoadPricePort;
import com.priceservice.app.domain.model.Price;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Driven adapter implementing the {@link LoadPricePort} on top of Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class PricePersistenceAdapter implements LoadPricePort {

    private final PriceJpaRepository repository;
    private final PricePersistenceMapper mapper;

    @Override
    @Cacheable(cacheNames = "applicablePrices", key = "#productId + '-' + #brandId + '-' + #date")
    public List<Price> findApplicablePrices(Integer productId, Integer brandId, OffsetDateTime date) {
        return repository
                .findByProductIdAndBrandIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        productId, brandId, date, date)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByBrandId(Integer brandId) {
        return repository.existsByBrandId(brandId);
    }

    @Override
    public boolean existsByProductId(Integer productId) {
        return repository.existsByProductId(productId);
    }
}
