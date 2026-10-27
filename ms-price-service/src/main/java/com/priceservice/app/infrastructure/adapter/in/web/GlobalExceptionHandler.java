package com.priceservice.app.infrastructure.adapter.in.web;

import com.priceservice.app.domain.exception.BrandNotFoundException;
import com.priceservice.app.domain.exception.PriceNotFoundException;
import com.priceservice.app.domain.exception.ProductNotFoundException;
import com.priceservice.app.infrastructure.adapter.in.web.filter.CorrelationIdFilter;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Centralized error handling using the RFC 7807 standard ({@link ProblemDetail},
 * built into Spring 6 and serialized as application/problem+json). Business
 * exceptions are mapped to problems with their corresponding status; Spring MVC
 * handles other errors through the base class, which already produces ProblemDetail.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setProperty("timestamp", OffsetDateTime.now(ZoneOffset.UTC));
        pd.setProperty("correlationId", CorrelationIdFilter.getCorrelationId());
        return pd;
    }

    @ExceptionHandler(PriceNotFoundException.class)
    public ProblemDetail handlePriceNotFound(PriceNotFoundException ex) {
        log.error("Price not found for given parameters");
        return problem(HttpStatus.NOT_FOUND, "Price not found", ex.getMessage());
    }

    @ExceptionHandler(BrandNotFoundException.class)
    public ProblemDetail handleBrandNotFound(BrandNotFoundException ex) {
        log.error("Brand not found");
        return problem(HttpStatus.NOT_FOUND, "Brand not found", ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
        log.error("Product not found");
        return problem(HttpStatus.NOT_FOUND, "Product not found", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument");
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Method argument type mismatch");
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
    }
}
