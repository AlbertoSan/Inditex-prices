package com.priceservice.app.infrastructure.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * JPA persistence model. Lives in infrastructure and is kept separate from
 * the domain {@link com.priceservice.app.domain.model.Price}, so persistence
 * concerns never leak into the core.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PRICE_ENTITY")
public class PriceJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer brandId;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Integer priceList;
    private Integer productId;
    private Integer priority;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;
    private String curr;
}
