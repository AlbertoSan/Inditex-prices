package com.priceservice.app.domain.exception;

public class BrandNotFoundException extends RuntimeException {

    public BrandNotFoundException(String message) {
        super(message);
    }
}
