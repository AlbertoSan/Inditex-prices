package com.priceservice.app.infrastructure.adapter.in.web;

import com.priceservice.app.domain.model.Price;
import com.priceservice.openapi.model.PriceResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceWebMapperTest {

    private final PriceWebMapper mapper = new PriceWebMapper();

    private Price price;

    @BeforeEach
    void setUp() {
        price = Price.builder()
                .id(1L)
                .brandId(1)
                .productId(35455)
                .priceList(1)
                .startDate(OffsetDateTime.parse("2020-06-14T00:00:00Z"))
                .endDate(OffsetDateTime.parse("2020-12-31T23:59:59Z"))
                .priority(1)
                .amount(new BigDecimal("123.45"))
                .currency("EUR")
                .build();
    }

    @Test
    void mapsDomainPriceToResponseDto() {
        PriceResponseDto result = mapper.toResponseDto(price);

        assertEquals(0, new BigDecimal("123.45").compareTo(result.getPrice()));
        assertEquals(1, result.getBrandId());
        assertEquals(35455, result.getProductId());
        assertEquals("EUR", result.getCurr());
    }
}
