package com.fulfilment.application.monolith.warehouses.domain;

public class WarehouseDomainException extends RuntimeException {

    public WarehouseDomainException(String message) {
        super(message);
    }

    public WarehouseDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}