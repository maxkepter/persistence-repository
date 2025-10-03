# Entity Lazy Loading & Relation Resolution

This document explains how relationships between entities are lazily loaded using `LazyReference`, `LazyList`, and `RelationLoader`.

## Table of Contents

- [Overview](#overview)
- [Lazy Loading Goals](#lazy-loading-goals)
- [Components](#components)
  - [LazyReference](#lazyreference)
  - [LazyList](#lazylist)
  - [RelationLoader](#relationloader)
- [Lifecycle of a Lazy Load](#lifecycle-of-a-lazy-load)
- [Usage Examples](#usage-examples)
  - [ManyToOne / OneToOne](#manytoone--onetoone)
  - [OneToMany Collection](#onetomany-collection)
  - [Chained Access](#chained-access)
- [Grouping & Batching Considerations](#grouping--batching-considerations)
- [Thread Safety](#thread-safety)
- [Common Pitfalls](#common-pitfalls)
- [Performance Tips](#performance-tips)
- [FAQ](#faq)

---

## Overview

The framework defers loading of related entities until they're actually accessed. This avoids unnecessary SQL queries when only a subset of an aggregate root is needed.

---

## Lazy Loading Goals

| Goal                                       | Benefit                                |
| ------------------------------------------ | -------------------------------------- |
| Minimize initial queries                   | Faster first response time             |
| Load only what's accessed                  | Reduces DB I/O                         |
| Preserve domain model richness             | Navigation via getters remains natural |
| Avoid N+1 explosions (when used carefully) | Enables manual tuning                  |

---

## Components

### LazyReference

Represents a lazily loaded single-valued association (e.g., `ManyToOne`, `OneToOne`).

Key ideas:

- Holds a target ID / foreign key reference internally.
- First call to `get()` triggers a select query.
- Subsequent calls return the cached instance.
- Null-safe: if no row found the resolved value remains `null`.

### LazyList

Represents a lazily loaded collection association (e.g., `OneToMany`). Extends `ArrayList` and overrides read access points to ensure initialization.

Trigger points include:

- Iteration (`forEach`, enhanced for loop)
- `size()`, `isEmpty()` (if overridden to call internal load)
- Access via iterator

Once loaded, elements are cached in memory for the life of the list.

### RelationLoader

Encapsulates the logic for fetching related records.

Typical responsibilities:

- Build appropriate select statements
- Bind foreign key parameters
- Map result sets to entity instances via the repository mapping layer

Pseudo-interface:

```java
class RelationLoader {
    static <T> T loadReference(Class<T> targetType, String joinColumn, Object fkValue) { /* ... */ }
    static <T> List<T> loadCollection(Class<T> targetType, String foreignKeyColumn, Object fkValue) { /* ... */ }
}
```

---

## Lifecycle of a Lazy Load

1. Root entity hydrated (only scalar fields + foreign key IDs present).
2. Relationship field replaced with `LazyReference` or `LazyList`.
3. Application code accesses the relation.
4. Lazy container checks loaded flag.
5. If not loaded: invokes `RelationLoader` to execute a query.
6. Materialized entities cached inside the container.
7. Future accesses are in-memory only.

---

## Usage Examples

### ManyToOne / OneToOne

```java
Order order = orderRepository.findById(42L);
// Customer not loaded yet
Customer customer = order.getCustomer();
String name = customer.getName(); // load happens now (first property read)
```

### OneToMany Collection

```java
Author author = authorRepository.findById(10L);
// Books collection still empty proxy
for (Book b : author.getBooks()) { // triggers load once
    System.out.println(b.getTitle());
}
// Second iteration reuses loaded list
author.getBooks().forEach(b -> process(b));
```

### Chained Access

```java
Invoice invoice = invoiceRepo.findById(5L);
// Access deep relation lazily
String region = invoice.getCustomer().getAddress().getRegion();
```

---

## Grouping & Batching Considerations

Lazy loading issues often arise from repeated access inside loops:

```java
List<Order> orders = orderRepo.findAll();
for (Order o : orders) {
    // Each call may trigger its own query (N+1)
    System.out.println(o.getCustomer().getName());
}
```

Mitigation strategies:

- Pre-fetch with an explicit tailored query (e.g. join fetch style via `SelectBuilder`).
- Collect foreign keys then bulk query manually.
- Introduce a custom repository method that performs a batched join.

---

## Thread Safety

- `LazyReference` and `LazyList` are NOT guaranteed thread-safe.
- Access from multiple threads may cause duplicated loads (benign) or visibility races.
- If multi-threaded access is needed, wrap access in higher-level synchronization or load eagerly before sharing.

---

## Common Pitfalls

| Pitfall                         | Description                                     | Mitigation                                     |
| ------------------------------- | ----------------------------------------------- | ---------------------------------------------- |
| N+1 queries                     | Loop triggers per-row relation load             | Batch or custom join query                     |
| Access outside transaction      | Relation load after connection closed           | Ensure active transaction / open session scope |
| Modifying LazyList directly     | Adding elements doesn't persist automatically   | Use repository save operations                 |
| Null expectation mismatch       | `equal(col, null)` skipped; `isNull()` required | Use explicit null helpers                      |
| Serialization of lazy container | May serialize unloaded proxy only               | Force initialization before serialize          |

---

## Performance Tips

- Use logging (SQL print) in development to spot N+1 patterns.
- Combine filters into fewer queries with `ClauseBuilder.group`.
- Paginate large root entity traversals using `PageRequest`.
- Cache read-mostly reference data (if safe) at application layer.

---

## FAQ

**Q: How do I force load a relation early?**  
Call a terminal method such as iterate the list or access a simple getter on the reference.

**Q: How do I detect if something is already loaded?**  
Current implementation does not expose a public `isLoaded()`—you can trigger a benign access or extend the lazy classes.

**Q: Can I replace lazy with eager globally?**  
Not currently; configure fetch strategy via annotations per relationship.

**Q: Does adding to a `LazyList` persist the new entity?**  
No. Persist via its repository; then refresh or rely on eventual reload.

**Q: Why did I get multiple queries for the same relation?**  
Likely accessing the relation across detached copies or in multiple threads—ensure entity identity and scope management.

---

## Summary

Lazy loading gives you control over when related data is fetched. Use it intentionally: pre-fetch when you know you will traverse large graphs, and monitor SQL output to avoid accidental N+1 patterns.
