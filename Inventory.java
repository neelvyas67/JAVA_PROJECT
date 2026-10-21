package com.github.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/** In-memory inventory keyed by SKU. */
public class Inventory {
    private final Map<String, Item> items = new LinkedHashMap<>();

    public void addItem(Item item) {
        if (items.putIfAbsent(item.getSku(), item) != null) {
            throw new IllegalArgumentException("SKU already exists: " + item.getSku());
        }
    }

    public Optional<Item> find(String sku) {
        return Optional.ofNullable(items.get(normalise(sku)));
    }

    public void receive(String sku, int amount) {
        require(sku).receive(amount);
    }

    public void issue(String sku, int amount) {
        require(sku).issue(amount);
    }

    public boolean remove(String sku) {
        return items.remove(normalise(sku)) != null;
    }

    public Collection<Item> all() {
        return items.values();
    }

    public List<Item> search(String text) {
        String needle = text.toLowerCase();
        return items.values().stream()
                .filter(i -> i.getName().toLowerCase().contains(needle) || i.getSku().toLowerCase().contains(needle))
                .toList();
    }

    /** Items at or below their reorder level, lowest quantity first. */
    public List<Item> lowStock() {
        List<Item> result = new ArrayList<>(items.values().stream().filter(Item::isLowStock).toList());
        result.sort(Comparator.comparingInt(Item::getQuantity));
        return result;
    }

    public double totalValue() {
        return items.values().stream().mapToDouble(Item::getTotalValue).sum();
    }

    public int size() {
        return items.size();
    }

    private Item require(String sku) {
        Item item = items.get(normalise(sku));
        if (item == null) throw new NoSuchElementException("Unknown SKU: " + sku);
        return item;
    }

    private static String normalise(String sku) {
        return sku == null ? "" : sku.trim().toUpperCase();
    }
}
