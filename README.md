# Izaak King NY CREATES Interview Cellphone Inventory Application - https://cellphone-inventory.onrender.com/

## Overview

This project is a Java and Spring Boot cellphone inventory application with a web interface and command-line interface. It can load, display, filter, add, edit, and delete phones stored in a comma-separated inventory file.

## Features

- View all phones in an inventory table
- Search phones by brand, model, storage, color, and price
- Sort inventory rows by table column
- Add new phones
- Edit existing phones
- Delete existing phones
- Validate phone fields and search filters
- Use a command-line interface for inventory filtering

## Prerequisites

Install:

- Java 21 or later

## Setup

Clone the repository and enter the project directory:

```powershell
git clone https://github.com/iking919/CellphoneInventory.git
cd CellphoneInventory
```

The default inventory file is:

```text
data/cellphonedata.txt
```

## Run the Web Application

From the project directory, run:

```powershell
.\mvnw spring-boot:run
```

```bash
./mvnw.cmd spring-boot:run
```

Open:

```text
http://localhost:8080
```

The available pages are:

- `/index.html` - Full inventory
- `/inventory-search.html` - Inventory search
- `/add-inventory.html` - Add a phone
- `/manage-inventory.html` - Edit or delete phones

Stop the application with `Ctrl+C`.

## Run the Command-Line Interface

The CLI is implemented in `Main.java`. Run it through an IDE or Maven configuration that executes the `main` method in `CellphoneInventory.Main`.

Available commands:

- `Brand => Google`
- `Model => Pixel`
- `Storage => 128GB`
- `Color => Black, Gray`
- `Price => < $900`
- `confirm` - Apply and display the current filters
- `clear` - Clear all filters
- `exit` - Quit the application

Only one filter can be applied to each supported field at a time.

From the project directory, run:

```powershell
javac -d target/classes `
  src/main/java/CellphoneInventory/Phone.java `
  src/main/java/CellphoneInventory/InventoryParser.java `
  src/main/java/CellphoneInventory/InventoryService.java `
  src/main/java/CellphoneInventory/Main.java

java -cp target/classes CellphoneInventory.Main
```

```bash
javac -d target/classes \
  src/main/java/CellphoneInventory/Phone.java \
  src/main/java/CellphoneInventory/InventoryParser.java \
  src/main/java/CellphoneInventory/InventoryService.java \
  src/main/java/CellphoneInventory/Main.java

java -cp target/classes CellphoneInventory.Main
```

## REST API

### Get all phones

```http
GET /api/phones
```

Example response:

```json
[
  {
    "brand": "Google",
    "model": "Pixel 8",
    "storage": "128 GB",
    "color": "Gray",
    "price": 699.99
  }
]
```

### Add a phone

```http
POST /api/phones
Content-Type: application/json
```

Example body:

```json
{
  "brand": "Google",
  "model": "Pixel 8",
  "storage": "128 GB",
  "color": "Gray",
  "price": 699.99
}
```

### Update a phone

```http
PUT /api/phones
Content-Type: application/json
```

Example body:

```json
{
  "original": {
    "brand": "Google",
    "model": "Pixel 8",
    "storage": "128 GB",
    "color": "Gray",
    "price": 699.99
  },
  "updated": {
    "brand": "Google",
    "model": "Pixel 8 Pro",
    "storage": "256 GB",
    "color": "Black",
    "price": 999.99
  }
}
```

### Delete a phone

```http
DELETE /api/phones
Content-Type: application/json
```

Example body:

```json
{
  "brand": "Google",
  "model": "Pixel 8",
  "storage": "128 GB",
  "color": "Gray",
  "price": 699.99
}
```

### Search phones

```http
POST /api/phones/search
Content-Type: application/json
```

Example body:

```json
[
  "brand => Samsung",
  "price => < $900"
]
```

## Run Tests

Run the complete test suite:

```powershell
.\mvnw test
```

```bash
./mvnw.cmd test
```

The tests cover:

- Phone validation
- Inventory parsing
- Filter validation and sorting
- Add, delete, and update service operations
- Command-line integration behavior
- Spring application startup
- REST controller POST, PUT, DELETE, and search endpoints
