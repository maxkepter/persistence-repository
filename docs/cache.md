# Caching System Documentation

This document describes the first-level caching system that improves performance by reducing redundant database queries within a transaction.

## Table of Contents

- [Overview](#overview)
- [PersistenceCache Interface](#persistencecache-interface)
- [EntityCache](#entitycache)
- [EntityKey](#entitykey)
- [CacheBuilder](#cachebuilder)
- [Cache Lifecycle](#cache-lifecycle)
- [Usage Examples](#usage-examples)
- [Best Practices](#best-practices)

---

## Overview

The caching system provides a first-level cache (session cache) that:

- Stores entities in memory during a transaction
- Ensures object identity within a transaction
- Reduces redundant database queries
- Automatically cleared on transaction commit/rollback
- Thread-safe using ThreadLocal storage

### Key Features

- **Identity Guarantee**: Multiple loads of same entity return same instance
- **Transparent**: Works automatically without explicit code
- **Transaction-Scoped**: Cache lifetime tied to transaction
- **Configurable**: Adjustable size and expiration settings
- **Thread-Safe**: Each thread has its own cache instance

---

## PersistenceCache Interface

The base interface for cache implementations.

### Interface Definition

```java
public interface PersistenceCache<K, V> {
    V get(K key);
    void put(K key, V value);
    void put(Iterable<K> keys, Iterable<V> values);
    boolean contains(K key);
    void remove(K key);
    void clear();
}
```

### Methods

| Method                          | Description                    |
| ------------------------------- | ------------------------------ |
| `get(K key)`                    | Retrieve cached value by key   |
| `put(K key, V value)`           | Store single key-value pair    |
| `put(Iterable<K>, Iterable<V>)` | Store multiple key-value pairs |
| `contains(K key)`               | Check if key exists in cache   |
| `remove(K key)`                 | Remove single entry from cache |
| `clear()`                       | Remove all entries from cache  |

---

## EntityCache

`EntityCache` is the main implementation of `PersistenceCache` for entity caching.

### Structure

```java
public class EntityCache implements PersistenceCache<EntityKey, Object> {
    private final int maxSize;
    private final long expirationTimeMillis;
    private final Map<EntityKey, Object> cache;

    public static final int DEFAULT_MAX_SIZE = 1000;
}
```

### Configuration

#### Default Cache

```java
// Create with default settings (1000 max size, 10 min expiration)
EntityCache cache = EntityCache.defaultCache();
```

#### Custom Configuration

```java
// Custom max size and expiration
EntityCache cache = new EntityCache(5000, 30 * 60 * 1000); // 5000 items, 30 min
```

#### Using CacheBuilder

```java
EntityCache cache = EntityCache.builder()
    .maxSize(2000)
    .expirationTimeMillis(15 * 60 * 1000)  // 15 minutes
    .build();
```

### Methods

#### Basic Operations

```java
// Store entity
EntityKey key = EntityKey.of(Product.class, 1L);
cache.put(key, productInstance);

// Retrieve entity
Product product = (Product) cache.get(key);

// Check existence
boolean exists = cache.contains(key);

// Remove entity
cache.remove(key);

// Clear all
cache.clear();
```

#### Batch Operations

```java
// Store multiple entities
List<Product> products = Arrays.asList(p1, p2, p3);
cache.put(products);  // Automatically creates keys

// Store with explicit keys
List<EntityKey> keys = Arrays.asList(key1, key2, key3);
List<Product> values = Arrays.asList(p1, p2, p3);
cache.put(keys, values);

// Remove multiple
cache.remove(keys);
```

### Eviction Policy

When cache reaches `maxSize`:

```java
if (cache.size() >= maxSize) {
    cache.clear();  // Simple eviction: clear all
}
cache.put(key, value);
```

**Note**: Current implementation uses a simple "clear all" eviction policy. Consider implementing LRU (Least Recently Used) for production use.

---

## EntityKey

`EntityKey` is a composite key combining entity class and primary key value.

### Structure

```java
public final class EntityKey {
    private final Class<?> entityClass;
    private final Object id;

    public EntityKey(Class<?> entityClass, Object id) {
        this.entityClass = entityClass;
        this.id = id;
    }
}
```

### Creating EntityKeys

#### From Class and ID

```java
// Explicit key creation
EntityKey key = EntityKey.of(Product.class, 1L);
EntityKey userKey = EntityKey.of(User.class, "user123");
```

#### From Entity Instance

```java
// Automatic key extraction (requires @Key annotation)
Product product = new Product();
product.setId(42L);

EntityKey key = EntityKey.of(product);
// Result: EntityKey(Product.class, 42L)
```

#### Batch Creation

```java
List<Product> products = productRepository.findAll();
Iterable<EntityKey> keys = EntityKey.of(products);
```

### Key Properties

```java
EntityKey key = EntityKey.of(Product.class, 1L);

Class<?> entityClass = key.getEntityClass();  // Product.class
Object id = key.getId();                       // 1L
String stringRep = key.toString();             // "com.example.model.Product:1"
```

### Equality and Hashing

Keys are equal if both class and ID match:

```java
EntityKey key1 = EntityKey.of(Product.class, 1L);
EntityKey key2 = EntityKey.of(Product.class, 1L);
EntityKey key3 = EntityKey.of(Product.class, 2L);
EntityKey key4 = EntityKey.of(Category.class, 1L);

assert key1.equals(key2);    // true - same class and ID
assert !key1.equals(key3);   // false - different ID
assert !key1.equals(key4);   // false - different class
```

---

## CacheBuilder

Fluent API for constructing `EntityCache` instances.

### Usage

```java
EntityCache cache = EntityCache.builder()
    .maxSize(5000)
    .expirationTimeMillis(20 * 60 * 1000)  // 20 minutes
    .build();
```

### Methods

| Method                       | Default         | Description                       |
| ---------------------------- | --------------- | --------------------------------- |
| `maxSize(int)`               | 1000            | Maximum number of cached entities |
| `expirationTimeMillis(long)` | 600000 (10 min) | Time before cache entries expire  |
| `build()`                    | -               | Creates EntityCache instance      |

### Examples

```java
// Small cache for testing
EntityCache testCache = EntityCache.builder()
    .maxSize(100)
    .expirationTimeMillis(60 * 1000)  // 1 minute
    .build();

// Large cache for production
EntityCache prodCache = EntityCache.builder()
    .maxSize(10000)
    .expirationTimeMillis(60 * 60 * 1000)  // 1 hour
    .build();

// Default settings
EntityCache defaultCache = EntityCache.builder().build();
```

---

## Cache Lifecycle

### Automatic Management

The cache is automatically managed by `TransactionManager`:

```
TransactionManager.beginTransaction()
    ↓
EntityCache created
    ↓
Repository operations
(Entities cached automatically)
    ↓
TransactionManager.commit()
    ↓
Cache cleared and released
```

### Cache Scope

```java
// Transaction 1
TransactionManager.beginTransaction();

Product p1 = productRepo.findById(1L);  // DB query
Product p2 = productRepo.findById(1L);  // Cache hit
assert p1 == p2;  // Same instance

TransactionManager.commit();  // Cache cleared

// Transaction 2 (new cache)
TransactionManager.beginTransaction();

Product p3 = productRepo.findById(1L);  // DB query (cache was cleared)
assert p1 != p3;  // Different instance

TransactionManager.commit();
```

---

## Usage Examples

### Example 1: Identity Within Transaction

```java
TransactionManager.beginTransaction();
try {
    // First load - hits database
    Product product1 = productRepository.findById(1L);
    product1.setName("Updated Name");

    // Second load - hits cache, returns same instance
    Product product2 = productRepository.findById(1L);

    // Both references point to same object
    assert product1 == product2;
    assert "Updated Name".equals(product2.getName());

    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}
```

### Example 2: Relationship Loading

```java
TransactionManager.beginTransaction();
try {
    // Load author with books
    Author author = authorRepository.findById(1L);

    // First access to books - triggers database query
    List<Book> books = author.getBooks();

    // Load one of the books directly
    Book book = bookRepository.findById(books.get(0).getId());

    // Same instance from cache
    assert books.get(0) == book;

    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}
```

### Example 3: Manual Cache Operations

```java
// Get cache reference
EntityCache cache = TransactionManager.getCache();

// Check if entity is cached
EntityKey key = EntityKey.of(Product.class, 1L);
if (cache.contains(key)) {
    Product product = (Product) cache.get(key);
    System.out.println("Found in cache: " + product.getName());
}

// Manually remove from cache
cache.remove(key);

// Next access will hit database
Product product = productRepository.findById(1L);
```

### Example 4: Batch Caching

```java
TransactionManager.beginTransaction();
try {
    // Load multiple products
    List<Product> products = (List<Product>) productRepository.findAll();

    // All products now cached
    EntityCache cache = TransactionManager.getCache();

    for (Product p : products) {
        EntityKey key = EntityKey.of(p);
        assert cache.contains(key);  // All are cached
    }

    // Subsequent individual lookups use cache
    Product p1 = productRepository.findById(products.get(0).getId());  // Cache hit

    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}
```

---

## Best Practices

### 1. Let the Framework Manage the Cache

```java
// Good - automatic caching
TransactionManager.beginTransaction();
Product p = productRepository.findById(1L);
TransactionManager.commit();

// Avoid manual cache manipulation unless necessary
```

### 2. Use Transactions for Cache Benefits

```java
// Good - cache active within transaction
TransactionManager.beginTransaction();
try {
    Product p1 = productRepository.findById(1L);  // DB hit
    Product p2 = productRepository.findById(1L);  // Cache hit
    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}

// Bad - no transaction, no cache
Product p1 = productRepository.findById(1L);  // DB hit
Product p2 = productRepository.findById(1L);  // DB hit again
```

### 3. Be Aware of Memory Usage

```java
// Good - appropriate cache size
EntityCache cache = EntityCache.builder()
    .maxSize(1000)  // Reasonable for typical use
    .build();

// Problematic - too large, may cause memory issues
EntityCache hugecache = EntityCache.builder()
    .maxSize(1000000)  // 1 million entities!
    .build();
```

### 4. Keep Transactions Short

```java
// Good - short transaction, limited cache scope
TransactionManager.beginTransaction();
try {
    Product p = productRepository.findById(1L);
    p.setPrice(99.99);
    productRepository.update(p);
    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}

// Bad - long transaction, cache grows large
TransactionManager.beginTransaction();
try {
    for (int i = 0; i < 100000; i++) {
        Product p = productRepository.findById(i);  // Cache grows huge
        // ... long processing
    }
    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}
```

### 5. Don't Rely on Cache Between Transactions

```java
// Bad - expecting cache to persist
TransactionManager.beginTransaction();
Product p1 = productRepository.findById(1L);
TransactionManager.commit();

// Cache is cleared here!

TransactionManager.beginTransaction();
Product p2 = productRepository.findById(1L);  // DB hit, not cache
assert p1 != p2;  // Different instances
TransactionManager.commit();
```

### 6. Clear Cache on Error if Needed

```java
TransactionManager.beginTransaction();
try {
    // Operations
    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();  // Cache automatically cleared
    throw e;
}
```

---

## Performance Considerations

### Cache Hit Benefits

```java
// Without cache: 3 database queries
Product p1 = productRepository.findById(1L);  // Query 1
Product p2 = productRepository.findById(1L);  // Query 2
Product p3 = productRepository.findById(1L);  // Query 3

// With cache in transaction: 1 database query
TransactionManager.beginTransaction();
Product p1 = productRepository.findById(1L);  // Query (cached)
Product p2 = productRepository.findById(1L);  // Cache hit
Product p3 = productRepository.findById(1L);  // Cache hit
TransactionManager.commit();
```

### Cache Misses

Cache misses occur when:

- First access to an entity in transaction
- Entity was explicitly removed from cache
- Cache was cleared due to max size
- Different transaction (new cache instance)

---

## Summary

The caching system provides:

- ✅ First-level (session) cache
- ✅ Automatic cache management
- ✅ Object identity guarantee
- ✅ Reduced database queries
- ✅ Transaction-scoped lifetime
- ✅ Thread-safe implementation
- ✅ Configurable size and expiration

For transaction integration, see [transaction.md](transaction.md).  
For repository usage, see [repository.md](repository.md).
