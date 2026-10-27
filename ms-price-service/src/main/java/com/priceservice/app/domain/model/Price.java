package com.priceservice.app.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Domain model representing an applicable price.
 * Pure POJO: no JPA, web or framework annotations. The only allowed
 * dependency is Lombok, which is a compile-time helper and leaves no
 * runtime coupling.
 */
@Getter
@Builder
@ToString
public class Price {

    private final Long id;
    private final Integer brandId;
    private final Integer productId;
    private final Integer priceList;
    private final OffsetDateTime startDate;
    private final OffsetDateTime endDate;
    private final Integer priority;
    private final BigDecimal amount;
    private final String currency;
}
