package com.priceservice.app.domain.exception;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(String message) {
        super(message);
    }
}
