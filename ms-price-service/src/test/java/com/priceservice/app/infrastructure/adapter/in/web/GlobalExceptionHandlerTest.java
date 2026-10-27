package com.priceservice.app.infrastructure.adapter.in.web;

import com.priceservice.app.domain.exception.BrandNotFoundException;
import com.priceservice.app.domain.exception.PriceNotFoundException;
import com.priceservice.app.domain.exception.ProductNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsBrandNotFoundToNotFoundProblem() {
        assertProblem(handler.handleBrandNotFound(new BrandNotFoundException("Unknown brand")),
                HttpStatus.NOT_FOUND, "Brand not found", "Unknown brand");
    }

    @Test
    void mapsProductNotFoundToNotFoundProblem() {
        assertProblem(handler.handleProductNotFound(new ProductNotFoundException("Unknown product")),
                HttpStatus.NOT_FOUND, "Product not found", "Unknown product");
    }

    @Test
    void mapsPriceNotFoundToNotFoundProblem() {
        assertProblem(handler.handlePriceNotFound(new PriceNotFoundException("No applicable price")),
                HttpStatus.NOT_FOUND, "Price not found", "No applicable price");
    }

    @Test
    void mapsIllegalArgumentToBadRequestProblem() {
        assertProblem(handler.handleIllegalArgument(new IllegalArgumentException("Invalid value")),
                HttpStatus.BAD_REQUEST, "Invalid request", "Invalid value");
    }

    @Test
    void mapsConstraintViolationToBadRequestProblem() {
        assertProblem(handler.handleConstraintViolation(new ConstraintViolationException("Invalid value", Set.of())),
                HttpStatus.BAD_REQUEST, "Validation failed", "Invalid value");
    }

    @Test
    void mapsTypeMismatchToBadRequestProblem() throws NoSuchMethodException {
        Method endpoint = TestEndpoint.class.getDeclaredMethod("get", Integer.class);
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "not-a-number", Integer.class, "brandId", new MethodParameter(endpoint, 0),
                new NumberFormatException("not-a-number"));

        assertProblem(handler.handleMethodArgumentTypeMismatch(exception),
                HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage());
    }

    private void assertProblem(ProblemDetail problem, HttpStatus status, String title, String detail) {
        assertEquals(status.value(), problem.getStatus());
        assertEquals(title, problem.getTitle());
        assertEquals(detail, problem.getDetail());
        assertNotNull(problem.getProperties());
        assertNotNull(problem.getProperties().get("timestamp"));
    }

    private static class TestEndpoint {
        void get(Integer brandId) {
        }
    }
}
