# Transaction Management Documentation

This document describes the transaction management system for ensuring data consistency and integrity across database operations.

## Table of Contents

- [Overview](#overview)
- [TransactionManager](#transactionmanager)
- [Basic Usage](#basic-usage)
- [Nested Transactions](#nested-transactions)
- [Transaction Scope](#transaction-scope)
- [Cache Integration](#cache-integration)
- [Best Practices](#best-practices)
- [Common Patterns](#common-patterns)

---

## Overview

The `TransactionManager` class provides thread-safe transaction management using ThreadLocal storage. It ensures that:

- Each thread has its own transaction context
- Multiple operations can be grouped into atomic units
- Transactions can be nested (savepoint-like behavior)
- First-level cache is scoped to transaction lifetime
- Connections are properly managed and cleaned up

### Key Concepts

- **Transaction**: An atomic unit of work that either succeeds completely or fails completely
- **Thread-Local**: Each thread maintains its own transaction context independently
- **Transaction Depth**: Support for nested transaction-like behavior
- **Auto-commit**: Disabled during transaction, enabled after commit/rollback

---

## TransactionManager

The `TransactionManager` is a static utility class that manages database transactions.

### Core Methods

#### beginTransaction()

Starts a new transaction:

```java
public static void beginTransaction() throws SQLException
```

- Gets a connection from the connection pool
- Disables auto-commit mode
- Initializes first-level cache
- Increments transaction depth counter
- Stores connection in ThreadLocal

**Throws**: `SQLException` if unable to obtain connection

#### commit()

Commits the current transaction:

```java
public static void commit() throws SQLException
```

- Decrements transaction depth counter
- If depth reaches 0, commits the database transaction
- Removes connection and cache from ThreadLocal
- Re-enables auto-commit mode

**Throws**: `SQLException` if no active transaction or commit fails

#### rollback()

Rolls back the current transaction:

```java
public static void rollback() throws SQLException
```

- Decrements transaction depth counter
- If depth reaches 0, rolls back all changes
- Removes connection and cache from ThreadLocal
- Re-enables auto-commit mode

**Throws**: `SQLException` if no active transaction or rollback fails

#### getConnection()

Retrieves the current transaction's connection:

```java
public static Connection getConnection()
```

- Returns the connection associated with current thread
- Used internally by repositories

**Throws**: `IllegalStateException` if no active transaction

#### getCache()

Retrieves the first-level cache for current transaction:

```java
public static EntityCache getCache()
```

- Returns the cache associated with current thread
- Used internally for caching entities

**Throws**: `IllegalStateException` if no active transaction

---

## Basic Usage

### Simple Transaction

```java
try {
    TransactionManager.beginTransaction();

    // Perform database operations
    Product product = new Product();
    product.setName("Laptop");
    product.setPrice(999.99);
    productRepository.save(product);

    // Commit if successful
    TransactionManager.commit();
    System.out.println("Transaction committed successfully");

} catch (SQLException e) {
    // Rollback on error
    try {
        TransactionManager.rollback();
        System.err.println("Transaction rolled back");
    } catch (SQLException rollbackEx) {
        rollbackEx.printStackTrace();
    }
    e.printStackTrace();
}
```

### Multiple Operations

```java
TransactionManager.beginTransaction();
try {
    // Operation 1: Create product
    Product product = new Product();
    product.setName("Wireless Mouse");
    product.setPrice(29.99);
    Product savedProduct = productRepository.save(product);

    // Operation 2: Update inventory
    InventoryItem item = new InventoryItem();
    item.setProductId(savedProduct.getId());
    item.setQuantity(100);
    inventoryRepository.save(item);

    // Operation 3: Log transaction
    ProductTransaction transaction = new ProductTransaction();
    transaction.setProductId(savedProduct.getId());
    transaction.setType("PURCHASE");
    transaction.setQuantity(100);
    transactionRepository.save(transaction);

    // All or nothing
    TransactionManager.commit();

} catch (SQLException e) {
    TransactionManager.rollback();
    throw new RuntimeException("Failed to complete product creation", e);
}
```

---

## Nested Transactions

The framework supports nested transaction-like behavior using depth tracking.

### How It Works

```java
// Depth: 0 → 1
TransactionManager.beginTransaction();

    // Depth: 1 → 2
    TransactionManager.beginTransaction();

        // Some operations
        productRepository.save(product);

    // Depth: 2 → 1 (no actual commit yet)
    TransactionManager.commit();

    // More operations
    categoryRepository.save(category);

// Depth: 1 → 0 (actual database commit)
TransactionManager.commit();
```

### Use Case: Service Calling Service

```java
public class OrderService {
    private ProductService productService = new ProductService();
    private InventoryService inventoryService = new InventoryService();

    public void createOrder(Order order) throws SQLException {
        TransactionManager.beginTransaction();  // Depth: 0 → 1
        try {
            // Save order
            orderRepository.save(order);

            // Call other services (they also use transactions)
            productService.updateProduct(order.getProductId());  // Depth: 1 → 2 → 1
            inventoryService.decreaseStock(order.getProductId(), order.getQuantity());  // Depth: 1 → 2 → 1

            TransactionManager.commit();  // Depth: 1 → 0 (actual commit)
        } catch (SQLException e) {
            TransactionManager.rollback();
            throw e;
        }
    }
}

public class ProductService {
    public void updateProduct(Long productId) throws SQLException {
        TransactionManager.beginTransaction();  // Depth: 1 → 2
        try {
            Product product = productRepository.findById(productId);
            product.setSalesCount(product.getSalesCount() + 1);
            productRepository.update(product);

            TransactionManager.commit();  // Depth: 2 → 1 (no actual commit)
        } catch (SQLException e) {
            TransactionManager.rollback();
            throw e;
        }
    }
}
```

### Important Notes

- Only the outermost `commit()` actually commits to database
- Any `rollback()` at any depth rolls back entire transaction
- Inner "commits" just decrement depth counter
- This simulates nested transactions (savepoints)

---

## Transaction Scope

### Thread Safety

Each thread has its own transaction context:

```java
// Thread 1
new Thread(() -> {
    TransactionManager.beginTransaction();
    productRepository.save(product1);
    TransactionManager.commit();
}).start();

// Thread 2 (independent transaction)
new Thread(() -> {
    TransactionManager.beginTransaction();
    productRepository.save(product2);
    TransactionManager.commit();
}).start();
```

### Transaction Lifetime

```
beginTransaction()
    ↓
Connection acquired
Auto-commit disabled
Cache initialized
    ↓
Repository operations
(All use same connection)
    ↓
commit() or rollback()
    ↓
Connection released
Cache cleared
Auto-commit restored
```

---

## Cache Integration

The transaction manager automatically manages a first-level cache.

### Cache Lifecycle

```java
TransactionManager.beginTransaction();

// First access - database hit
Product p1 = productRepository.findById(1L);

// Second access - cache hit (same instance)
Product p2 = productRepository.findById(1L);

assert p1 == p2;  // true - same object instance

TransactionManager.commit();  // Cache cleared
```

### Benefits

- Reduces database round trips
- Ensures object identity within transaction
- Automatic cache invalidation on commit/rollback

See [cache.md](cache.md) for details.

---

## Best Practices

### 1. Always Use Try-Catch with Rollback

```java
// Good
TransactionManager.beginTransaction();
try {
    // operations
    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
    throw e;
}

// BAD - no rollback handling
TransactionManager.beginTransaction();
productRepository.save(product);
TransactionManager.commit();
```

### 2. Keep Transactions Short

```java
// Good - quick transaction
TransactionManager.beginTransaction();
try {
    productRepository.save(product);
    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}

// BAD - long-running transaction
TransactionManager.beginTransaction();
try {
    for (int i = 0; i < 10000; i++) {
        // Heavy processing
        Thread.sleep(100);
        productRepository.save(products.get(i));
    }
    TransactionManager.commit();
} catch (Exception e) {
    TransactionManager.rollback();
}
```

### 3. Use Service Layer for Transaction Management

```java
// Good - transaction in service layer
public class ProductService {
    private ProductRepository productRepo = new ProductRepository();

    public Product createProduct(Product product) throws SQLException {
        TransactionManager.beginTransaction();
        try {
            Product saved = productRepo.save(product);
            // Business logic
            TransactionManager.commit();
            return saved;
        } catch (SQLException e) {
            TransactionManager.rollback();
            throw e;
        }
    }
}

// BAD - transaction in controller/presentation layer
public class ProductController {
    public void handleCreateProduct(ProductRequest request) {
        TransactionManager.beginTransaction();  // Too high level
        // ...
    }
}
```

### 4. Don't Mix Transactional and Non-Transactional Code

```java
// Good - all operations in transaction
TransactionManager.beginTransaction();
try {
    productRepo.save(product);
    categoryRepo.save(category);
    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}

// BAD - mixing transactional contexts
productRepo.save(product);  // No transaction!
TransactionManager.beginTransaction();
categoryRepo.save(category);
TransactionManager.commit();
```

### 5. Handle Nested Transactions Carefully

```java
// Good - aware of nesting
public void outerMethod() throws SQLException {
    TransactionManager.beginTransaction();
    try {
        // operations
        innerMethod();  // Also uses transactions
        // more operations
        TransactionManager.commit();
    } catch (SQLException e) {
        TransactionManager.rollback();
        throw e;
    }
}

public void innerMethod() throws SQLException {
    TransactionManager.beginTransaction();
    try {
        // operations
        TransactionManager.commit();
    } catch (SQLException e) {
        TransactionManager.rollback();
        throw e;  // Propagate to outer transaction
    }
}
```

---

## Common Patterns

### Pattern 1: Read-Write Transaction

```java
public Product updateProductPrice(Long productId, Double newPrice) throws SQLException {
    TransactionManager.beginTransaction();
    try {
        Product product = productRepository.findById(productId);

        if (product == null) {
            throw new EntityNotFoundException("Product not found");
        }

        product.setPrice(newPrice);
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.update(product);
        TransactionManager.commit();

        return updated;
    } catch (SQLException e) {
        TransactionManager.rollback();
        throw e;
    }
}
```

### Pattern 2: Batch Operations

```java
public void importProducts(List<Product> products) throws SQLException {
    TransactionManager.beginTransaction();
    try {
        for (Product product : products) {
            productRepository.save(product);
        }

        System.out.println("Imported " + products.size() + " products");
        TransactionManager.commit();

    } catch (SQLException e) {
        System.err.println("Import failed, rolling back all changes");
        TransactionManager.rollback();
        throw e;
    }
}
```

### Pattern 3: Conditional Rollback

```java
public void processOrder(Order order) throws SQLException {
    TransactionManager.beginTransaction();
    try {
        orderRepository.save(order);

        // Check business rule
        if (!inventoryService.hasStock(order.getProductId(), order.getQuantity())) {
            TransactionManager.rollback();
            throw new BusinessException("Insufficient stock");
        }

        inventoryService.decreaseStock(order.getProductId(), order.getQuantity());
        TransactionManager.commit();

    } catch (SQLException e) {
        TransactionManager.rollback();
        throw e;
    }
}
```

### Pattern 4: Transaction Template

```java
public class TransactionTemplate {

    public static <T> T execute(TransactionCallback<T> callback) throws SQLException {
        TransactionManager.beginTransaction();
        try {
            T result = callback.doInTransaction();
            TransactionManager.commit();
            return result;
        } catch (SQLException e) {
            TransactionManager.rollback();
            throw e;
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction() throws SQLException;
    }
}

// Usage
Product product = TransactionTemplate.execute(() -> {
    Product p = new Product();
    p.setName("Laptop");
    return productRepository.save(p);
});
```

---

## Summary

The transaction management system provides:

- ✅ Thread-safe transaction contexts
- ✅ ACID transaction guarantees
- ✅ Nested transaction support
- ✅ Automatic cache management
- ✅ Clean error handling with rollback
- ✅ Connection pooling integration

For repository usage, see [repository.md](repository.md).  
For cache behavior, see [cache.md](cache.md).  
For database configuration, see `DBcontext` and `RepositoryConfig`.
