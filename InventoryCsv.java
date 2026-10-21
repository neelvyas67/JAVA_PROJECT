package com.github.inventory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** CSV persistence. Format: sku,name,quantity,reorderLevel,unitPrice (names may not contain commas). */
public final class InventoryCsv {
    private static final String HEADER = "sku,name,quantity,reorderLevel,unitPrice";

    private InventoryCsv() {}

    public static void save(Inventory inventory, Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Item i : inventory.all()) {
            lines.add(String.join(",", i.getSku(), i.getName().replace(",", " "),
                    Integer.toString(i.getQuantity()), Integer.toString(i.getReorderLevel()),
                    Double.toString(i.getUnitPrice())));
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public static Inventory load(Path path) throws IOException {
        Inventory inventory = new Inventory();
        if (!Files.exists(path)) return inventory;
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (int n = 1; n < lines.size(); n++) {
            String line = lines.get(n).trim();
            if (line.isEmpty()) continue;
            String[] p = line.split(",", -1);
            if (p.length != 5) throw new IOException("Malformed CSV at line " + (n + 1) + ": " + line);
            try {
                inventory.addItem(new Item(p[0], p[1], Integer.parseInt(p[2]),
                        Integer.parseInt(p[3]), Double.parseDouble(p[4])));
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid data at line " + (n + 1) + ": " + e.getMessage(), e);
            }
        }
        return inventory;
    }
}
