package com.priceservice.app.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicablePricesTest {

    @Test
    void reportsEmptyAndHasNoHighestPriorityPriceWhenNoPricesApply() {
        ApplicablePrices applicablePrices = new ApplicablePrices(List.of());

        assertTrue(applicablePrices.isEmpty());
        assertTrue(applicablePrices.highestPriority().isEmpty());
    }

    @Test
    void selectsThePriceWithTheHighestPriority() {
        Price lowerPriority = Price.builder().priority(0).build();
        Price higherPriority = Price.builder().priority(1).build();
        ApplicablePrices applicablePrices = new ApplicablePrices(List.of(higherPriority, lowerPriority));

        assertFalse(applicablePrices.isEmpty());
        assertEquals(higherPriority, applicablePrices.highestPriority().orElseThrow());
    }
}
