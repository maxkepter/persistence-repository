# Query Builder Documentation

This document describes the query building system for constructing type-safe SQL queries programmatically.

## Table of Contents

- [Overview](#overview)
- [SelectBuilder](#selectbuilder)
- [InsertBuilder](#insertbuilder)
- [UpdateBuilder](#updatebuilder)
- [DeleteBuilder](#deletebuilder)
- [ClauseBuilder](#clausebuilder)
- [Pagination Classes](#pagination-classes)
- [Best Practices](#best-practices)

---

## Overview

The query builder system provides fluent APIs for constructing SQL queries without writing raw SQL strings. Benefits include:

- **Type safety**: Compile-time checking of query structure
- **Readability**: Chainable methods for clear query intent
- **Maintainability**: Easy to modify and test
- **SQL injection prevention**: Automatic parameterization

### Query Builder Hierarchy

```
AbstractQueryBuilder (base)
├── SelectBuilder (SELECT queries)
├── InsertBuilder (INSERT queries)
├── UpdateBuilder (UPDATE queries)
└── DeleteBuilder (DELETE queries)

ClauseBuilder (WHERE conditions, reusable)
```

---

## SelectBuilder

Constructs SELECT queries with WHERE, JOIN, ORDER BY, LIMIT, and OFFSET clauses.

### Basic Usage

```java
SelectBuilder<Product> builder = SelectBuilder.builder("products")
    .columns("id", "name", "price")
    .where("price > ?", 100.0)
    .orderBy(List.of(new Order("name", true)))
    .limit(10);

String sql = builder.build();
// SELECT id, name, price FROM products WHERE price > ? ORDER BY name ASC LIMIT 10
```

### Methods

#### columns()

Select specific columns:

```java
// Multiple columns
builder.columns("id", "name", "email");

// All columns (default)
builder.columns("*");

// With list
builder.columns(List.of("id", "name", "price"));
```

#### distinct()

Remove duplicate rows:

```java
builder.distinct(true).columns("category");
// SELECT DISTINCT category FROM products
```

#### where()

Add WHERE clause with parameters:

```java
// Single condition
builder.where("status = ?", "ACTIVE");

// Multiple conditions (AND)
builder.where("price >= ? AND price <= ?", 100.0, 500.0);

// Using ClauseBuilder (recommended for complex conditions)
ClauseBuilder clause = new ClauseBuilder()
    .where("price > ?", 100)
    .where("category = ?", "Electronics");
builder.where(clause.build(), clause.getParameters().toArray());
```

#### orderBy()

Sort results:

```java
// Single column ascending
builder.orderBy(List.of(new Order("name", true)));

// Multiple columns
builder.orderBy(List.of(
    new Order("category", true),
    new Order("price", false)  // descending
));

// ORDER BY category ASC, price DESC
```

#### limit() and offset()

Pagination:

```java
// First 10 results
builder.limit(10);

// Skip first 20, take next 10
builder.limit(10).offset(20);
```

#### alias()

Table alias for joins:

```java
builder.alias("p")
    .columns("p.id", "p.name");
// SELECT p.id, p.name FROM products p
```

#### innerJoin() and leftJoin()

Join tables:

```java
SelectBuilder<Product> builder = SelectBuilder.builder("products")
    .alias("p")
    .columns("p.id", "p.name", "c.name as category_name")
    .innerJoin("categories c", "p.category_id = c.id")
    .where("p.price > ?", 100);

// SELECT p.id, p.name, c.name as category_name
// FROM products p
// INNER JOIN categories c ON p.category_id = c.id
// WHERE p.price > ?
```

#### Custom join()

```java
builder.join("LEFT OUTER JOIN orders o ON p.id = o.product_id");
```

### Complete Example

```java
SelectBuilder<Product> builder = SelectBuilder.builder("products")
    .alias("p")
    .columns("p.id", "p.name", "p.price", "c.name as category")
    .innerJoin("categories c", "p.category_id = c.id")
    .leftJoin("reviews r", "p.id = r.product_id")
    .where("p.price BETWEEN ? AND ?", 50.0, 200.0)
    .where("p.status = ?", "ACTIVE")
    .orderBy(List.of(
        new Order("c.name", true),
        new Order("p.price", false)
    ))
    .limit(20)
    .offset(0);

String sql = builder.build();
List<Object> params = builder.getParameters();

// Execute query
try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    int index = 1;
    for (Object param : params) {
        stmt.setObject(index++, param);
    }
    ResultSet rs = stmt.executeQuery();
    // Process results...
}
```

---

## InsertBuilder

Constructs INSERT statements for adding new records.

### Basic Usage

```java
InsertBuilder builder = InsertBuilder.builder("products")
    .value("name", "Laptop")
    .value("price", 999.99)
    .value("category_id", 5);

String sql = builder.build();
// INSERT INTO products (name, price, category_id) VALUES (?, ?, ?)
```

### Methods

#### value()

Add column-value pairs:

```java
builder.value("name", "Product Name")
       .value("price", 49.99)
       .value("stock", 100);
```

#### Batch Inserts

For multiple rows, use `saveAll()` in repository which handles batching.

### Complete Example

```java
InsertBuilder builder = InsertBuilder.builder("products")
    .value("name", "Wireless Mouse")
    .value("price", 29.99)
    .value("category_id", 3)
    .value("stock_quantity", 150)
    .value("status", "ACTIVE");

String sql = builder.build();
List<Object> params = builder.getParameters();

try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    int index = 1;
    for (Object param : params) {
        stmt.setObject(index++, param);
    }
    stmt.executeUpdate();

    // Get generated ID
    ResultSet keys = stmt.getGeneratedKeys();
    if (keys.next()) {
        long id = keys.getLong(1);
        System.out.println("Inserted with ID: " + id);
    }
}
```

---

## UpdateBuilder

Constructs UPDATE statements for modifying existing records.

### Basic Usage

```java
UpdateBuilder builder = UpdateBuilder.builder("products")
    .set("price", 899.99)
    .set("stock_quantity", 50)
    .where("id = ?", 10);

String sql = builder.build();
// UPDATE products SET price = ?, stock_quantity = ? WHERE id = ?
```

### Methods

#### set()

Specify columns to update:

```java
builder.set("name", "Updated Name")
       .set("price", 79.99)
       .set("updated_at", LocalDateTime.now());
```

#### where()

Add WHERE clause (REQUIRED for safety):

```java
// Update single record
builder.set("status", "INACTIVE")
       .where("id = ?", 5);

// Update multiple records
builder.set("discount", 0.10)
       .where("category = ? AND price > ?", "Electronics", 100);
```

### Complete Example

```java
UpdateBuilder builder = UpdateBuilder.builder("products")
    .set("name", "Premium Laptop")
    .set("price", 1299.99)
    .set("description", "High-performance laptop")
    .set("updated_at", Timestamp.valueOf(LocalDateTime.now()))
    .where("id = ?", 42);

String sql = builder.build();
List<Object> params = builder.getParameters();

try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    int index = 1;
    for (Object param : params) {
        stmt.setObject(index++, param);
    }
    int rowsUpdated = stmt.executeUpdate();
    System.out.println("Updated " + rowsUpdated + " rows");
}
```

---

## DeleteBuilder

Constructs DELETE statements for removing records.

### Basic Usage

```java
DeleteBuilder builder = DeleteBuilder.builder("products")
    .where("id = ?", 10);

String sql = builder.build();
// DELETE FROM products WHERE id = ?
```

### Methods

#### where()

Specify which records to delete (REQUIRED):

```java
// Delete single record
builder.where("id = ?", 5);

// Delete multiple records
builder.where("status = ? AND created_at < ?", "INACTIVE", someDate);

// Delete with complex condition
ClauseBuilder clause = new ClauseBuilder()
    .where("stock_quantity = ?", 0)
    .where("status = ?", "DISCONTINUED");
builder.where(clause.build(), clause.getParameters().toArray());
```

### Complete Example

```java
DeleteBuilder builder = DeleteBuilder.builder("products")
    .where("status = ? AND last_updated < ?",
           "INACTIVE",
           Timestamp.valueOf(LocalDateTime.now().minusYears(1)));

String sql = builder.build();
List<Object> params = builder.getParameters();

try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    int index = 1;
    for (Object param : params) {
        stmt.setObject(index++, param);
    }
    int rowsDeleted = stmt.executeUpdate();
    System.out.println("Deleted " + rowsDeleted + " rows");
}
```

---

## ClauseBuilder

A fluent helper for composing `WHERE` clauses with parameter binding. It works alongside the CRUD builders and repositories to produce safe, reusable predicates.

### Purpose

- Build complex conditional logic without hand-writing SQL fragments
- Share condition blocks across queries and repositories
- Automatically collect bound parameters in the order conditions were added

### Basic Usage

```java
ClauseBuilder clause = ClauseBuilder.builder()
    .greaterOrEqual("price", 100)
    .and()
    .lessOrEqual("price", 500)
    .and()
    .equal("status", "ACTIVE");

String whereClause = clause.build();
List<Object> parameters = clause.getParameters();
// whereClause -> "price >= ? AND price <= ? AND status = ?"
// parameters  -> [100, 500, "ACTIVE"]
```

### Condition Helpers

```java
ClauseBuilder.builder()
    .equal("category", "Electronics")
    .and()
    .like("name", "%Pro%")
    .and()
    .in("brand", List.of("Acme", "Globex"))
    .and()
    .group(group -> group
        .greater("stock", 0)
        .or()
        .isNull("stock")
    );
```

- `equal`, `notEqual`, `greater`, `less`, `greaterOrEqual`, `lessOrEqual`, `like`
- `in` / `notIn` accept collections and skip empty inputs
- `isNull` injects a `IS NULL` check
- `and()` / `or()` switch logical connectors between conditions
- `group(Consumer<ClauseBuilder>)` nests grouped expressions, keeping parameters aligned

### Using with Repositories

```java
ClauseBuilder activeProducts = ClauseBuilder.builder()
    .equal("status", "ACTIVE")
    .and()
    .greater("stock_quantity", 0);

Iterable<Product> products = productRepository.findWithCondition(activeProducts);

ClauseBuilder premiumActiveProducts = ClauseBuilder.builder()
    .group(group -> group
        .equal("status", "ACTIVE")
        .and()
        .greater("stock_quantity", 0)
    )
    .and()
    .greater("price", 1_000);

Iterable<Product> premiumProducts = productRepository.findWithCondition(premiumActiveProducts);
```

Because every condition automatically appends its parameter, you can safely pass `clause.getParameters()` straight to a prepared statement or builder.

### Dynamic Queries

```java
ClauseBuilder clause = ClauseBuilder.builder();

if (minPrice != null) {
    clause.greaterOrEqual("price", minPrice);
}

if (maxPrice != null) {
    clause.lessOrEqual("price", maxPrice);
}

if (category != null) {
    clause.equal("category", category);
}

Iterable<Product> results = productRepo.findWithCondition(clause);
```

> ℹ️ `ClauseBuilder` focuses purely on conditions; sorting and pagination are handled by `SelectBuilder`, `Order`, `Sort`, and `PageRequest`.

---

## Pagination Classes

### Order

Represents column sorting:

```java
public class Order {
    private String column;
    private boolean ascending;

    public Order(String column, boolean ascending) {
        this.column = column;
        this.ascending = ascending;
    }
}

// Usage
Order ascendingByName = new Order("name", true);
Order descendingByPrice = new Order("price", false);
```

### PageRequest

Specifies pagination parameters:

```java
public class PageRequest {
    private int page;        // Zero-based page number
    private int size;        // Items per page
    private Sort sort;       // Optional sorting

    // Simple pagination
    PageRequest pageRequest = new PageRequest(0, 20);

    // With sorting
    Sort sort = new Sort(List.of(new Order("name", true)));
    PageRequest pageRequest = new PageRequest(0, 20, sort);
}
```

### Page

Contains paginated results:

```java
public class Page<T> {
    private List<T> content;      // Current page items
    private int totalElements;    // Total items across all pages
    private int totalPages;       // Total number of pages
    private int currentPage;      // Current page number
    private int pageSize;         // Items per page

    // Usage
    Page<Product> page = productRepo.findAll(new PageRequest(0, 10));
    List<Product> products = page.getContent();
    int total = page.getTotalElements();
    int totalPages = page.getTotalPages();
    boolean hasNext = page.hasNext();
    boolean hasPrevious = page.hasPrevious();
}
```

### Sort

Wraps multiple Order instances:

```java
Sort sort = new Sort(List.of(
    new Order("category", true),
    new Order("price", false),
    new Order("name", true)
));

PageRequest pageRequest = new PageRequest(0, 20, sort);
Page<Product> page = productRepo.findAll(pageRequest);
```

### Pagination Example

```java
// Page 1 (items 0-19)
PageRequest page1 = new PageRequest(0, 20);
Page<Product> results1 = productRepo.findAll(page1);
System.out.println("Page 1 of " + results1.getTotalPages());

// Page 2 (items 20-39)
PageRequest page2 = new PageRequest(1, 20);
Page<Product> results2 = productRepo.findAll(page2);

// With sorting
Sort sort = new Sort(List.of(new Order("price", false)));
PageRequest pageRequest = new PageRequest(0, 20, sort);
Page<Product> results = productRepo.findAll(pageRequest);

// Iterate through all pages
int currentPage = 0;
Page<Product> page;
do {
    page = productRepo.findAll(new PageRequest(currentPage++, 50));
    for (Product product : page.getContent()) {
        System.out.println(product.getName());
    }
} while (page.hasNext());
```

---

## Best Practices

### 1. Always Parameterize Values

```java
// Good - prevents SQL injection
builder.where("name = ?", userInput);

// BAD - vulnerable to SQL injection
builder.where("name = '" + userInput + "'");
```

### 2. Use ClauseBuilder for Complex Conditions

```java
// Good - clean and maintainable
ClauseBuilder clause = new ClauseBuilder()
    .where("category = ?", category)
    .where("price BETWEEN ? AND ?", minPrice, maxPrice)
    .where("status = ?", "ACTIVE")
    .orderBy("price DESC");
List<Product> products = (List<Product>) repo.findWithCondition(clause);

// Less maintainable - inline conditions
List<Product> products = (List<Product>) repo.findWithCondition(
    new ClauseBuilder().where("category = ? AND price BETWEEN ? AND ? AND status = ?",
        category, minPrice, maxPrice, "ACTIVE")
);
```

### 3. Always Include WHERE in UPDATE and DELETE

```java
// Good
builder.set("status", "INACTIVE").where("id = ?", productId);

// Dangerous - updates ALL rows!
builder.set("status", "INACTIVE");
```

### 4. Use Aliases for Joins

```java
// Good - clear table references
SelectBuilder.builder("products")
    .alias("p")
    .innerJoin("categories c", "p.category_id = c.id")
    .columns("p.name", "c.name as category_name");

// Confusing without aliases
SelectBuilder.builder("products")
    .innerJoin("categories", "products.category_id = categories.id")
    .columns("products.name", "categories.name");
```

### 5. Prefer Repository Methods Over Direct Query Building

```java
// Good - use repository abstraction
List<Product> products = productRepo.findByCategory("Electronics");

// Less ideal - bypassing repository layer
ClauseBuilder clause = new ClauseBuilder().where("category = ?", "Electronics");
List<Product> products = (List<Product>) productRepo.findWithCondition(clause);
```

### 6. Use Pagination for Large Result Sets

```java
// Good - paginated
PageRequest pageRequest = new PageRequest(0, 50);
Page<Product> page = productRepo.findAll(pageRequest);

// Bad - loads everything into memory
Iterable<Product> allProducts = productRepo.findAll();
```

---

## Summary

The query builder system provides:

- ✅ Type-safe SQL generation
- ✅ Fluent, chainable API
- ✅ Automatic parameterization
- ✅ Support for complex queries
- ✅ Pagination support
- ✅ Reusable query components

For repository usage, see [repository.md](repository.md).  
For entity mapping, see [entity.md](entity.md).
