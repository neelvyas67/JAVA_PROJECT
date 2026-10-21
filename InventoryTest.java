package com.github.inventory;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InventoryTest {
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        inventory.addItem(new Item("bolt-m8", "Bolt M8", 100, 20, 0.15));
        inventory.addItem(new Item("nut-m8", "Nut M8", 10, 20, 0.05));
    }

    @Test
    void skuIsNormalisedAndLookupIsCaseInsensitive() {
        assertTrue(inventory.find("BOLT-M8").isPresent());
        assertTrue(inventory.find(" bolt-m8 ").isPresent());
    }

    @Test
    void duplicateSkuRejected() {
        assertThrows(IllegalArgumentException.class, () -> inventory.addItem(new Item("BOLT-M8", "Dup", 1, 1, 1)));
    }

    @Test
    void receiveAndIssueUpdateQuantity() {
        inventory.receive("bolt-m8", 50);
        inventory.issue("bolt-m8", 30);
        assertEquals(120, inventory.find("bolt-m8").orElseThrow().getQuantity());
    }

    @Test
    void issuingMoreThanAvailableFailsAndLeavesStockUnchanged() {
        assertThrows(InsufficientStockException.class, () -> inventory.issue("nut-m8", 11));
        assertEquals(10, inventory.find("nut-m8").orElseThrow().getQuantity());
    }

    @Test
    void unknownSkuThrows() {
        assertThrows(NoSuchElementException.class, () -> inventory.receive("nope", 1));
    }

    @Test
    void invalidAmountsRejected() {
        assertThrows(IllegalArgumentException.class, () -> inventory.receive("bolt-m8", 0));
        assertThrows(IllegalArgumentException.class, () -> inventory.issue("bolt-m8", -1));
    }

    @Test
    void lowStockReportsItemsAtOrBelowReorderLevel() {
        var low = inventory.lowStock();
        assertEquals(1, low.size());
        assertEquals("NUT-M8", low.get(0).getSku());
    }

    @Test
    void totalValueIsSumOfLines() {
        assertEquals(100 * 0.15 + 10 * 0.05, inventory.totalValue(), 1e-9);
    }

    @Test
    void searchMatchesNameOrSku() {
        assertEquals(2, inventory.search("m8").size());
        assertEquals(1, inventory.search("nut").size());
    }

    @Test
    void csvRoundTrip(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("inv.csv");
        InventoryCsv.save(inventory, file);
        Inventory loaded = InventoryCsv.load(file);
        assertEquals(2, loaded.size());
        assertEquals(10, loaded.find("nut-m8").orElseThrow().getQuantity());
    }

    @Test
    void malformedCsvIsReported(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("bad.csv");
        Files.writeString(file, "sku,name,quantity,reorderLevel,unitPrice\nONLY,two\n");
        assertThrows(IOException.class, () -> InventoryCsv.load(file));
    }
}
