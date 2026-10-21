package com.github.inventory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/** Interactive console menu. Usage: java -jar inventory-manager.jar [data.csv] */
public class Main {
    public static void main(String[] args) throws IOException {
        Path file = Path.of(args.length > 0 ? args[0] : "inventory.csv");
        Inventory inventory = InventoryCsv.load(file);
        System.out.println("Inventory Manager - " + inventory.size() + " item(s) loaded from " + file);

        try (Scanner in = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = in.hasNextLine() ? in.nextLine().trim() : "0";
                try {
                    switch (choice) {
                        case "1" -> add(in, inventory);
                        case "2" -> { inventory.receive(ask(in, "SKU"), askInt(in, "Amount")); System.out.println("Stock received."); }
                        case "3" -> { inventory.issue(ask(in, "SKU"), askInt(in, "Amount")); System.out.println("Stock issued."); }
                        case "4" -> inventory.all().forEach(System.out::println);
                        case "5" -> inventory.search(ask(in, "Search text")).forEach(System.out::println);
                        case "6" -> {
                            var low = inventory.lowStock();
                            if (low.isEmpty()) System.out.println("Nothing below reorder level.");
                            low.forEach(System.out::println);
                        }
                        case "7" -> System.out.printf("Total inventory value: %.2f%n", inventory.totalValue());
                        case "8" -> System.out.println(inventory.remove(ask(in, "SKU")) ? "Removed." : "SKU not found.");
                        case "0" -> running = false;
                        default -> System.out.println("Unknown option.");
                    }
                } catch (RuntimeException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
        InventoryCsv.save(inventory, file);
        System.out.println("Saved to " + file + ". Goodbye!");
    }

    private static void printMenu() {
        System.out.println("""

                1) Add item      2) Receive stock   3) Issue stock   4) List all
                5) Search        6) Low stock       7) Total value   8) Remove item
                0) Save & exit""");
        System.out.print("> ");
    }

    private static void add(Scanner in, Inventory inventory) {
        inventory.addItem(new Item(ask(in, "SKU"), ask(in, "Name"), askInt(in, "Quantity"),
                askInt(in, "Reorder level"), askDouble(in, "Unit price")));
        System.out.println("Item added.");
    }

    private static String ask(Scanner in, String label) {
        System.out.print(label + ": ");
        return in.nextLine().trim();
    }

    private static int askInt(Scanner in, String label) {
        try { return Integer.parseInt(ask(in, label)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(label + " must be a whole number"); }
    }

    private static double askDouble(Scanner in, String label) {
        try { return Double.parseDouble(ask(in, label)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(label + " must be a number"); }
    }
}
