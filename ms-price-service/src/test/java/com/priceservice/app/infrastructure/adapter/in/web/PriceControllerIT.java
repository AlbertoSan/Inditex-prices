package com.priceservice.app.infrastructure.adapter.in.web;

import com.priceservice.app.application.port.in.GetPriceQuery;
import com.priceservice.app.application.port.in.GetPriceUseCase;
import com.priceservice.app.domain.exception.PriceNotFoundException;
import com.priceservice.app.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer integration test for the REST adapter. Boots only the controller
 * (plus its mapper and the exception handler), mocking the use case, and
 * exercises the 200 / 404 / 400 paths over HTTP.
 */
@WebMvcTest(PriceController.class)
@Import({PriceWebMapper.class, GlobalExceptionHandler.class})
class PriceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetPriceUseCase getPriceUseCase;

    @ParameterizedTest(name = "returns {0}")
    @MethodSource("successfulPrices")
    void returns200WithPriceWhenFound(String scenario, Price price, String requestedAt) throws Exception {
        when(getPriceUseCase.getPrice(any(GetPriceQuery.class))).thenReturn(price);

        mockMvc.perform(get("/v1/brands/{brandId}/products/{productId}/price",
                        price.getBrandId(), price.getProductId())
                        .param("date", requestedAt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandId").value(price.getBrandId()))
                .andExpect(jsonPath("$.productId").value(price.getProductId()))
                .andExpect(jsonPath("$.price").value(price.getAmount().doubleValue()))
                .andExpect(jsonPath("$.curr").value(price.getCurrency()));

        verify(getPriceUseCase).getPrice(new GetPriceQuery(
                price.getProductId(), price.getBrandId(), OffsetDateTime.parse(requestedAt)));
    }

    private static Stream<Arguments> successfulPrices() {
        return Stream.of(
                arguments("the standard EUR price", price(1, 35455, "35.50", "EUR", 0),
                        "2020-06-14T10:00:00Z"),
                arguments("a discounted EUR price", price(1, 35455, "25.45", "EUR", 1),
                        "2020-06-14T16:00:00Z"),
                arguments("a price with another currency and identifiers", price(2, 12345, "0.99", "USD", 2),
                        "2020-06-15T10:00:00+02:00"),
                arguments("the minimum-cent EUR price", price(1, 35455, "0.01", "EUR", 0),
                        "2020-06-14T21:00:00Z"),
                arguments("a higher-value GBP price", price(3, 98765, "999.99", "GBP", 3),
                        "2020-06-16T21:00:00Z")
        );
    }

    private static Price price(int brandId, int productId, String amount, String currency, int priority) {
        return Price.builder()
                .brandId(brandId).productId(productId).priceList(priority + 1)
                .startDate(OffsetDateTime.parse("2020-06-14T00:00:00Z"))
                .endDate(OffsetDateTime.parse("2020-12-31T23:59:59Z"))
                .priority(priority).amount(new BigDecimal(amount)).currency(currency)
                .build();
    }

    @Test
    void returns404WhenPriceNotFound() throws Exception {
        when(getPriceUseCase.getPrice(any(GetPriceQuery.class)))
                .thenThrow(new PriceNotFoundException("Price not found for given parameters"));

        mockMvc.perform(get("/v1/brands/1/products/35455/price")
                        .param("date", "2020-06-14T10:00:00Z"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Price not found"));
    }

    @Test
    void returns400WhenRequiredParamMissing() throws Exception {
        mockMvc.perform(get("/v1/brands/1/products/35455/price"))
                .andExpect(status().isBadRequest());
    }
}
