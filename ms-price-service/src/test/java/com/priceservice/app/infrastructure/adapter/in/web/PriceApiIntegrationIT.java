package com.priceservice.app.infrastructure.adapter.in.web;

import com.priceservice.openapi.model.PriceResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test: sends requests through the REST endpoint (real
 * port) and traverses every layer down to H2, using data.sql. Covers the five
 * challenge scenarios, an error case, and conditional caching (ETag). The
 * rate limiter is disabled to avoid throttling the request suite.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "ratelimit.enabled=false")
class PriceApiIntegrationIT {

    private static final String URL = "/v1/brands/1/products/35455/price?date={date}";

    @Autowired
    private TestRestTemplate restTemplate;

    @ParameterizedTest(name = "{0} -> {1} EUR")
    @CsvSource({
            "2020-06-14T10:00:00Z, 35.50",
            "2020-06-14T16:00:00Z, 25.45",
            "2020-06-14T21:00:00Z, 35.50",
            "2020-06-15T10:00:00Z, 30.50",
            "2020-06-16T21:00:00Z, 38.95"
    })
    void devuelveElPrecioAplicable(String date, BigDecimal expectedPrice) {
        ResponseEntity<PriceResponseDto> response =
                restTemplate.getForEntity(URL, PriceResponseDto.class, date);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBrandId()).isEqualTo(1);
        assertThat(response.getBody().getProductId()).isEqualTo(35455);
        assertThat(response.getBody().getCurr()).isEqualTo("EUR");
        assertThat(response.getBody().getPrice()).isEqualByComparingTo(expectedPrice);
    }

    @Test
    void devuelve404SiElProductoNoExiste() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/v1/brands/1/products/99999/price?date={date}", String.class,
                "2020-06-14T10:00:00Z");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void devuelveEtagY304ConPeticionCondicional() {
        ResponseEntity<PriceResponseDto> first =
                restTemplate.getForEntity(URL, PriceResponseDto.class, "2020-06-14T10:00:00Z");
        String etag = first.getHeaders().getETag();

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(etag).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.setIfNoneMatch(etag);
        ResponseEntity<String> second = restTemplate.exchange(
                URL, HttpMethod.GET, new HttpEntity<>(headers), String.class, "2020-06-14T10:00:00Z");

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
    }
}
