package com.priceservice.app.application.service;

import com.priceservice.app.application.port.in.GetPriceQuery;
import com.priceservice.app.application.port.out.LoadPricePort;
import com.priceservice.app.domain.exception.BrandNotFoundException;
import com.priceservice.app.domain.exception.PriceNotFoundException;
import com.priceservice.app.domain.exception.ProductNotFoundException;
import com.priceservice.app.domain.model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPriceServiceTest {

    private static final OffsetDateTime DATE = OffsetDateTime.parse("2020-06-14T10:00:00Z");
    private static final OffsetDateTime START = OffsetDateTime.parse("2020-06-14T00:00:00Z");
    private static final OffsetDateTime END = OffsetDateTime.parse("2020-12-31T23:59:59Z");

    @Mock
    private LoadPricePort loadPricePort;

    @InjectMocks
    private GetPriceService service;

    private Price lowPriority;
    private Price highPriority;

    @BeforeEach
    void setUp() {
        lowPriority = Price.builder()
                .brandId(1).productId(35455).priceList(1)
                .startDate(START).endDate(END)
                .priority(0).amount(new BigDecimal("35.50")).currency("EUR").build();

        highPriority = Price.builder()
                .brandId(1).productId(35455).priceList(2)
                .startDate(START).endDate(END)
                .priority(1).amount(new BigDecimal("25.45")).currency("EUR").build();
    }

    @Test
    void returnsHighestPriorityPriceWhenSeveralApply() {
        when(loadPricePort.findApplicablePrices(35455, 1, DATE))
                .thenReturn(List.of(lowPriority, highPriority));

        Price result = service.getPrice(new GetPriceQuery(35455, 1, DATE));

        assertEquals(0, new BigDecimal("25.45").compareTo(result.getAmount()));
        assertEquals(1, result.getPriority());
    }

    @Test
    void returnsPriceWhenExactlyOnePriceApplies() {
        when(loadPricePort.findApplicablePrices(35455, 1, DATE))
                .thenReturn(List.of(lowPriority));

        Price result = service.getPrice(new GetPriceQuery(35455, 1, DATE));

        assertEquals(0, new BigDecimal("35.50").compareTo(result.getAmount()));
        assertEquals(0, result.getPriority());
        verify(loadPricePort, never()).existsByBrandId(1);
        verify(loadPricePort, never()).existsByProductId(35455);
    }

    @Test
    void throwsBrandNotFoundWhenBrandHasNoPrices() {
        when(loadPricePort.findApplicablePrices(35455, 1, DATE)).thenReturn(List.of());
        when(loadPricePort.existsByBrandId(1)).thenReturn(false);

        GetPriceQuery query = new GetPriceQuery(35455, 1, DATE);
        assertThrows(BrandNotFoundException.class, () -> service.getPrice(query));
    }

    @Test
    void throwsProductNotFoundWhenProductHasNoPrices() {
        when(loadPricePort.findApplicablePrices(35455, 1, DATE)).thenReturn(List.of());
        when(loadPricePort.existsByBrandId(1)).thenReturn(true);
        when(loadPricePort.existsByProductId(35455)).thenReturn(false);

        GetPriceQuery query = new GetPriceQuery(35455, 1, DATE);
        assertThrows(ProductNotFoundException.class, () -> service.getPrice(query));
    }

    @Test
    void throwsPriceNotFoundWhenBrandAndProductExistButNoPriceApplies() {
        when(loadPricePort.findApplicablePrices(35455, 1, DATE)).thenReturn(List.of());
        when(loadPricePort.existsByBrandId(1)).thenReturn(true);
        when(loadPricePort.existsByProductId(35455)).thenReturn(true);

        GetPriceQuery query = new GetPriceQuery(35455, 1, DATE);
        assertThrows(PriceNotFoundException.class, () -> service.getPrice(query));
    }
}
