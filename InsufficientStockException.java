package com.github.inventory;

/** Thrown when an issue request exceeds the quantity on hand. */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String sku, int available, int requested) {
        super("Insufficient stock for " + sku + ": requested " + requested + ", available " + available);
    }
}
