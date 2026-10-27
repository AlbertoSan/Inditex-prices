package com.priceservice.app.infrastructure.adapter.in.web;

import com.priceservice.app.application.port.in.GetPriceQuery;
import com.priceservice.app.application.port.in.GetPriceUseCase;
import com.priceservice.app.domain.exception.PriceNotFoundException;
import com.priceservice.app.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

    @Test
    void returns200WithPriceWhenFound() throws Exception {
        Price price = Price.builder()
                .brandId(1).productId(35455).priceList(1)
                .startDate(OffsetDateTime.parse("2020-06-14T00:00:00Z"))
                .endDate(OffsetDateTime.parse("2020-12-31T23:59:59Z"))
                .priority(0).amount(new BigDecimal("35.50")).currency("EUR")
                .build();
        when(getPriceUseCase.getPrice(any(GetPriceQuery.class))).thenReturn(price);

        mockMvc.perform(get("/v1/brands/1/products/35455/price")
                        .param("date", "2020-06-14T10:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.price").value(35.50))
                .andExpect(jsonPath("$.curr").value("EUR"));
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
