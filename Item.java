package com.github.inventory;

import java.util.Objects;

/** A stock item. Quantity is mutable; everything else is fixed after creation. */
public final class Item {
    private final String sku;
    private final String name;
    private final double unitPrice;
    private final int reorderLevel;
    private int quantity;

    public Item(String sku, String name, int quantity, int reorderLevel, double unitPrice) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU must not be blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be blank");
        if (quantity < 0) throw new IllegalArgumentException("Quantity must not be negative");
        if (reorderLevel < 0) throw new IllegalArgumentException("Reorder level must not be negative");
        if (unitPrice < 0) throw new IllegalArgumentException("Unit price must not be negative");
        this.sku = sku.trim().toUpperCase();
        this.name = name.trim();
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.unitPrice = unitPrice;
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public int getReorderLevel() { return reorderLevel; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalValue() { return quantity * unitPrice; }
    public boolean isLowStock() { return quantity <= reorderLevel; }

    void receive(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        quantity = Math.addExact(quantity, amount);
    }

    void issue(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (amount > quantity) throw new InsufficientStockException(sku, quantity, amount);
        quantity -= amount;
    }

    @Override public boolean equals(Object o) {
        return o instanceof Item other && sku.equals(other.sku);
    }

    @Override public int hashCode() { return Objects.hash(sku); }

    @Override public String toString() {
        return String.format("%-10s %-24s qty=%-6d reorder=%-5d price=%9.2f value=%11.2f%s",
                sku, name, quantity, reorderLevel, unitPrice, getTotalValue(), isLowStock() ? "  [LOW]" : "");
    }
}
