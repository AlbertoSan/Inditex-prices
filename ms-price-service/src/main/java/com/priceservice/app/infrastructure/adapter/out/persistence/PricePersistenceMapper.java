package com.priceservice.app.infrastructure.adapter.out.persistence;

import com.priceservice.app.domain.model.Price;
import org.springframework.stereotype.Component;

/**
 * Translates between the JPA entity and the domain model.
 */
@Component
public class PricePersistenceMapper {

    public Price toDomain(PriceJpaEntity entity) {
        return Price.builder()
                .id(entity.getId())
                .brandId(entity.getBrandId())
                .productId(entity.getProductId())
                .priceList(entity.getPriceList())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .priority(entity.getPriority())
                .amount(entity.getPrice())
                .currency(entity.getCurr())
                .build();
    }
}
