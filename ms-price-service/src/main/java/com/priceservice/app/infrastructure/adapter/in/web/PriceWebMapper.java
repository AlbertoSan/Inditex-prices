package com.priceservice.app.infrastructure.adapter.in.web;

import com.priceservice.app.domain.model.Price;
import com.priceservice.openapi.model.PriceResponseDto;
import org.springframework.stereotype.Component;

/**
 * Maps the domain {@link Price} to the OpenAPI-generated response DTO.
 */
@Component
public class PriceWebMapper {

    public PriceResponseDto toResponseDto(Price price) {
        PriceResponseDto dto = new PriceResponseDto();
        dto.setBrandId(price.getBrandId());
        dto.setProductId(price.getProductId());
        dto.setPriceList(price.getPriceList());
        dto.setStartDate(price.getStartDate());
        dto.setEndDate(price.getEndDate());
        dto.setPrice(price.getAmount());
        dto.setCurr(price.getCurrency());
        return dto;
    }
}
