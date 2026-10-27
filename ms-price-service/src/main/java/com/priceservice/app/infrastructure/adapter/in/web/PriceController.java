package com.priceservice.app.infrastructure.adapter.in.web;

import com.priceservice.app.application.port.in.GetPriceQuery;
import com.priceservice.app.application.port.in.GetPriceUseCase;
import com.priceservice.app.domain.model.Price;
import com.priceservice.openapi.api.PricesApi;
import com.priceservice.openapi.model.PriceResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Driving (input) adapter. Translates HTTP into a use-case call and the
 * resulting domain model into the API DTO. It depends on the input port,
 * not on the concrete service.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PriceController implements PricesApi {

    private final GetPriceUseCase getPriceUseCase;
    private final PriceWebMapper priceWebMapper;

    @Override
    public ResponseEntity<PriceResponseDto> getApplicablePrice(
            Integer brandId,
            Integer productId,
            OffsetDateTime date) {
        log.debug("getApplicablePrice: brandId={}, productId={}, date={}", brandId, productId, date);
        Price price = getPriceUseCase.getPrice(new GetPriceQuery(productId, brandId, date));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .body(priceWebMapper.toResponseDto(price));
    }
}
