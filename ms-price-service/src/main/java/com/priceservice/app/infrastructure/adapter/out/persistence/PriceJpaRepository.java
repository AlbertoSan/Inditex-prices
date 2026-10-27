package com.priceservice.app.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PriceJpaRepository extends JpaRepository<PriceJpaEntity, Long> {

    List<PriceJpaEntity> findByProductIdAndBrandIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Integer productId, Integer brandId, OffsetDateTime date1, OffsetDateTime date2);

    boolean existsByBrandId(Integer brandId);

    boolean existsByProductId(Integer productId);
}
