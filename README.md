# Inventory Manager (Java)

A console inventory/stock-control application with CSV persistence and a JUnit 5 test suite.

## Features
- Add items with SKU, name, quantity, reorder level and unit price
- Receive and issue stock (with validation and insufficient-stock protection)
- Search by name or SKU
- Low-stock report (items at or below their reorder level)
- Total inventory value
- Data saved to / loaded from a CSV file

## Requirements
- JDK 17+
- Maven 3.8+

## Build, test, run
```bash
mvn verify                                  # compile + run tests
java -jar target/inventory-manager-1.0.0.jar          # uses ./inventory.csv
java -jar target/inventory-manager-1.0.0.jar my.csv   # custom data file
```

## Design
| Class | Responsibility |
|-------|----------------|
| `Item` | Validated stock item; quantity changes go through package-private methods |
| `Inventory` | In-memory store keyed by SKU, search, low-stock and valuation queries |
| `InventoryCsv` | Load/save |
| `Main` | Interactive console menu |

## License
MIT
