# Repository Layer Documentation

This document describes the repository pattern implementation in the persistence framework, including the `CrudRepository` interface, `AbstractRepository` base class, and `SimpleRepository` implementation.

## Table of Contents

- [Overview](#overview)
- [CrudRepository Interface](#crudrepository-interface)
- [AbstractRepository](#abstractrepository)
- [SimpleRepository](#simplerepository)
- [Creating Custom Repositories](#creating-custom-repositories)
- [Advanced Features](#advanced-features)
- [Best Practices](#best-practices)

---

## Overview

The repository layer provides a clean abstraction for data access operations, following the Repository pattern. It handles CRUD operations, query building, relationship loading, and caching automatically based on entity annotations.

### Key Features

- Generic CRUD operations (Create, Read, Update, Delete)
- Automatic SQL generation from entity metadata
- Lazy and eager relationship loading
- Built-in pagination support
- Transaction-aware operations
- First-level caching integration
- Flexible query building with `ClauseBuilder`

---

## CrudRepository Interface

The `CrudRepository<E, K>` interface defines standard CRUD operations for entities of type `E` with primary key type `K`.

### Methods

#### Create Operations

```java
E save(E entity) throws SQLException;
```

Inserts a new entity or updates an existing one based on the primary key.

**Returns**: The saved entity (may include generated keys)

**Throws**: `SQLException` if the operation fails

```java
Iterable<E> saveAll(Iterable<E> entities) throws SQLException;
```

Saves multiple entities in batch.

**Returns**: All saved entities

#### Read Operations

```java
E findById(K key);
```

Retrieves an entity by its primary key.

**Returns**: The entity or `null` if not found

```java
Iterable<E> findAll();
```

Retrieves all entities of this type.

**Returns**: All entities in the table

```java
Iterable<E> findWithCondition(ClauseBuilder clause);
```

Retrieves entities matching the specified conditions.

**Parameters**:

- `clause`: A `ClauseBuilder` with WHERE conditions

**Returns**: Matching entities

```java
boolean isExist(K key);
```

Checks if an entity exists by primary key.

**Returns**: `true` if exists, `false` otherwise

```java
int count();
```

Counts all entities in the table.

**Returns**: Total number of entities

#### Update Operations

```java
E update(E entity) throws SQLException;
```

Updates an existing entity identified by its primary key.

**Returns**: The updated entity

**Throws**: `SQLException` if the operation fails

#### Delete Operations

```java
void deleteById(K key) throws SQLException;
```

Deletes an entity by its primary key.

**Throws**: `SQLException` if the operation fails

```java
void deleteWithCondition(ClauseBuilder clause) throws SQLException;
```

Deletes all entities matching the specified conditions.

**Parameters**:

- `clause`: A `ClauseBuilder` with WHERE conditions

**Throws**: `SQLException` if the operation fails

---

## AbstractRepository

`AbstractRepository<E, K>` is an abstract base class that implements `CrudRepository<E, K>` and provides:

- Automatic SQL generation based on `@Entity`, `@Column`, and `@Key` annotations
- Relationship loading (OneToOne, OneToMany, ManyToOne)
- ResultSet to entity mapping
- Pagination support
- Cache integration

### Constructor

```java
protected AbstractRepository(Class<E> cls)
```

Initializes the repository with entity metadata by scanning annotations on the provided class.

### Additional Methods

#### Pagination

```java
Page<E> findAll(PageRequest pageRequest);
```

Retrieves entities with pagination.

**Parameters**:

- `pageRequest`: Contains page number, page size, and optional sorting

**Returns**: A `Page<E>` object containing:

- Current page entities
- Total elements
- Total pages
- Page metadata

**Example**:

```java
PageRequest pageRequest = new PageRequest(0, 10); // page 0, size 10
Page<Product> page = productRepository.findAll(pageRequest);
System.out.println("Total: " + page.getTotalElements());
for (Product product : page.getContent()) {
    System.out.println(product.getName());
}
```

#### Custom Queries

Subclasses can add custom query methods by using query builders:

```java
public List<Product> findExpensiveProducts(double minPrice) {
    ClauseBuilder clause = new ClauseBuilder()
        .where("price >= ?", minPrice)
        .orderBy("price DESC");
    return (List<Product>) findWithCondition(clause);
}
```

---

## SimpleRepository

`SimpleRepository<E, K>` is a concrete implementation of `AbstractRepository` that can be instantiated directly for simple entities without custom logic.

### Usage

```java
// Direct instantiation
CrudRepository<Product, Long> productRepo = new SimpleRepository<>(Product.class);

// Use CRUD operations
Product product = new Product();
product.setName("Laptop");
product.setPrice(999.99);
productRepo.save(product);

Product found = productRepo.findById(1L);
```

---

## Creating Custom Repositories

For entities requiring custom business logic, extend `AbstractRepository`:

### Example: ProductRepository

```java
package com.example.repository;

import com.example.model.Product;
import com.example.persistence.repository.AbstractRepository;
import com.example.persistence.query.clause.ClauseBuilder;
import java.util.List;

public class ProductRepository extends AbstractRepository<Product, Long> {

    public ProductRepository() {
        super(Product.class);
    }

    // Custom query methods
    public List<Product> findByCategory(String category) {
        ClauseBuilder clause = new ClauseBuilder()
            .where("category = ?", category)
            .orderBy("name ASC");
        return (List<Product>) findWithCondition(clause);
    }

    public List<Product> findInPriceRange(double minPrice, double maxPrice) {
        ClauseBuilder clause = new ClauseBuilder()
            .where("price BETWEEN ? AND ?", minPrice, maxPrice);
        return (List<Product>) findWithCondition(clause);
    }

    public List<Product> searchByName(String keyword) {
        ClauseBuilder clause = new ClauseBuilder()
            .where("name LIKE ?", "%" + keyword + "%");
        return (List<Product>) findWithCondition(clause);
    }

    public int countByCategory(String category) throws SQLException {
        // Custom count logic
        SelectBuilder builder = SelectBuilder.builder(getTableName())
            .columns("COUNT(*) as total")
            .where("category = ?", category);
        // Execute and return count
        // ... implementation details
    }
}
```

### Using Custom Repository

```java
ProductRepository productRepo = new ProductRepository();

// Use standard CRUD
Product product = productRepo.findById(1L);

// Use custom methods
List<Product> electronics = productRepo.findByCategory("Electronics");
List<Product> affordable = productRepo.findInPriceRange(10.0, 100.0);
List<Product> laptops = productRepo.searchByName("laptop");
```

---

## Advanced Features

### Relationship Loading

The repository automatically handles entity relationships based on `@OneToOne`, `@OneToMany`, and `@ManyToOne` annotations:

```java
// Entity with relationships
@Entity(tableName = "authors")
public class Author {
    @Key
    private Long id;

    @OneToMany(mappedBy = "author", fetch = FetchMode.LAZY)
    private List<Book> books;
}

// Repository usage
AuthorRepository authorRepo = new AuthorRepository();
Author author = authorRepo.findById(1L);

// Books are loaded lazily when accessed
List<Book> books = author.getBooks(); // Triggers database query
```

### Fetch Strategies

- **LAZY**: Relationships loaded on first access (default for collections)
- **EAGER**: Relationships loaded immediately with the entity

### Transaction Integration

Repositories automatically participate in transactions managed by `TransactionManager`:

```java
try {
    TransactionManager.beginTransaction();

    Product product = new Product();
    product.setName("New Product");
    productRepo.save(product);

    Category category = new Category();
    category.setName("New Category");
    categoryRepo.save(category);

    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
    throw e;
}
```

### Cache Integration

First-level cache is automatically enabled within transactions:

```java
TransactionManager.beginTransaction();
Product p1 = productRepo.findById(1L); // Database hit
Product p2 = productRepo.findById(1L); // Cache hit (same instance)
assert p1 == p2; // true
TransactionManager.commit();
```

---

## Best Practices

### 1. Use Transactions for Multiple Operations

```java
// Good
TransactionManager.beginTransaction();
try {
    productRepo.save(product);
    inventoryRepo.update(inventory);
    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}
```

### 2. Prefer Custom Methods Over Complex Inline Queries

```java
// Good
List<Product> products = productRepo.findActiveProductsByCategory("Electronics");

// Less maintainable
ClauseBuilder clause = new ClauseBuilder()
    .where("category = ? AND active = ?", "Electronics", true);
List<Product> products = productRepo.findWithCondition(clause);
```

### 3. Use Pagination for Large Result Sets

```java
// Good
PageRequest pageRequest = new PageRequest(0, 20);
Page<Product> page = productRepo.findAll(pageRequest);

// Avoid loading all records
Iterable<Product> allProducts = productRepo.findAll(); // Could be millions!
```

### 4. Choose Appropriate Fetch Strategies

```java
// For frequently accessed associations
@OneToOne(joinColumn = "profile_id", fetch = FetchMode.EAGER)
private Profile profile;

// For large collections
@OneToMany(mappedBy = "author", fetch = FetchMode.LAZY)
private List<Book> books;
```

### 5. Handle Null Results

```java
Product product = productRepo.findById(id);
if (product == null) {
    throw new EntityNotFoundException("Product not found: " + id);
}
```

### 6. Close Resources in Service Layer

```java
public class ProductService {
    private final ProductRepository productRepo = new ProductRepository();

    public Product createProduct(Product product) throws SQLException {
        TransactionManager.beginTransaction();
        try {
            Product saved = productRepo.save(product);
            TransactionManager.commit();
            return saved;
        } catch (SQLException e) {
            TransactionManager.rollback();
            throw e;
        }
    }
}
```

---

## Summary

The repository layer provides:

- ✅ Clean separation of data access logic
- ✅ Automatic SQL generation
- ✅ Type-safe CRUD operations
- ✅ Relationship management
- ✅ Transaction support
- ✅ Caching integration
- ✅ Extensibility for custom queries

For query building details, see [query-builder.md](query-builder.md).  
For transaction management, see [transaction.md](transaction.md).  
For caching behavior, see [cache.md](cache.md).
