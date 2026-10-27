package com.priceservice.app.application.port.in;

import java.time.OffsetDateTime;

/**
 * Input query for the "get applicable price" use case.
 * Decouples the use case signature from the web/HTTP layer.
 */
public record GetPriceQuery(Integer productId, Integer brandId, OffsetDateTime date) {
}
