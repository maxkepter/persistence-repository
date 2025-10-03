# Framework Warnings & Limitations

This document lists important limitations, edge cases, and behavioral nuances of the persistence query & repository framework. Read before using in production.

---

## 1. InsertBuilder – No Automatic Key Retrieval

- The framework does NOT automatically fetch database-generated primary keys.
- `InsertBuilder` only builds the SQL and binds parameters.
- If you need generated IDs, you must manually request them via JDBC (e.g. `PreparedStatement.RETURN_GENERATED_KEYS`) and map them yourself.
- There is no helper abstraction yet for key propagation back into entities.

### Example (manual key handling)

```java
InsertBuilder<?> builder = InsertBuilder.builder("products")
    .columns("name", "price")
    .values("Laptop", 999.99);
String sql = builder.build();
try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    int i = 1;
    for (Object p : builder.getParameters()) {
        ps.setObject(i++, p);
    }
    ps.executeUpdate();
    try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
            long id = keys.getLong(1); // assign manually
        }
    }
}
```

---

## 2. Batch Insert Value Count Rule

- `InsertBuilder` allows batching by appending multiple `values(...)` calls.
- TOTAL parameter count MUST be a multiple of the column count.
- Otherwise: `IllegalStateException("Values count ... must be a multiple of columns count ...")`.

### Correct

```java
InsertBuilder.builder("users")
    .columns("name", "age")
    .values("Alice", 30)   // row 1
    .values("Bob", 25);    // row 2
```

### Incorrect

```java
InsertBuilder.builder("users")
    .columns("name", "age")
    .values("Alice", 30, "EXTRA"); // 3 params for 2 columns -> error
```

---

## 3. ClauseBuilder Behavior

- Ignores null values passed to comparison helpers (condition not added).
- Does NOT support `ORDER BY`, `LIMIT`, or `OFFSET` – those belong in `SelectBuilder`.
- `and()` / `or()` must follow a condition; calling them first throws an exception.
- Grouping via `group(cb -> { ... })` starts a nested set joined with implicit `AND` unless a pending logical operator was set.
- `in` / `notIn` with empty collections are ignored silently.
- Calling `build()` with a dangling logical operator (`and()`, `or()`) throws `IllegalStateException`.

---

## 4. Pagination Semantics

- `PageRequest` is 1-indexed (`PageRequest.of(1, 20)` is the first page).
- Validation rejects page < 1 or size < 1.
- Sorting is optional: create with `PageRequest.of(page, size, Sort.by("column"))`.
- Internally offset = `(pageNumber - 1) * pageSize`.

---

## 5. Sort & Order Utilities

- Prefer factory methods: `Order.asc("name")`, `Order.desc("price")`.
- `Sort.by("col1", "col2")` (if implemented) or construct with list of `Order` objects.
- Passing an empty or null `Sort` leads to no ORDER BY clause being applied.

---

## 6. Caching Layer Note

- Cache integration (if enabled) stores entire result lists as returned.
- Mutating entity instances after retrieval does NOT automatically refresh cached copies.
- No eviction strategy is currently documented—verify `cache.md` for details before relying on freshness.

---

## 7. SQL Printing

- Controlled by `RepositoryConfig.PRINT_SQL`.
- Excessive logging in production can expose sensitive data patterns (though parameters stay bound separately).

---

## 8. Concurrency & Transactions

- Transaction management wrapper assumed; ensure a connection is bound in `TransactionManager` prior to repository calls.
- Clause/Query builders are NOT thread-safe—create a new instance per logical query.

---

## 9. Null Handling in ClauseBuilder

- Passing null to helpers like `equal("col", null)` results in the condition being skipped (not converted to `IS NULL`). Use `isNull("col")` explicitly.

---

## 10. Limitations & TODO Candidates

- No support for generated key propagation into entities.
- No automatic optimistic locking / versioning fields.
- No criteria over joins inside `ClauseBuilder.group` beyond manual column referencing.
- No support for database-specific functions beyond raw string usage in helpers.

---

## Quick Reference Table

| Concern                          | Supported | Notes                                |
| -------------------------------- | --------- | ------------------------------------ |
| Auto-generated key mapping       | No        | Manual JDBC handling required        |
| Batch inserts                    | Yes       | Values count must align with columns |
| ORDER/LIMIT in ClauseBuilder     | No        | Use `SelectBuilder`                  |
| Pagination                       | Yes       | 1-indexed pages                      |
| Null comparison (automatic)      | Partial   | Null skipped; use `isNull()`         |
| In/NotIn empty collections       | Skipped   | No condition added                   |
| Grouped logical blocks           | Yes       | `group(cb -> {...})`                 |
| Auto entity refresh after update | No        | Must re-query                        |

---

## 11. LazyReference Wrapping for Single Entities

Single-valued relationships (e.g. `@ManyToOne`, `@OneToOne`) are wrapped in `LazyReference<T>` instead of exposing the entity field directly:

```java
@ManyToOne(joinColumn = "RoleID", fetch = FetchMode.EAGER)
private LazyReference<Role> role; // internal field

public Role getRole() { // API method provided by entity
    return role.get();  // triggers load if not yet resolved
}
```

Key points:

- Always call the entity's accessor (e.g. `getRole()`), not `role.get()` externally if encapsulated.
- `LazyReference#get()` may return null if the row doesn't exist; code defensively.
- When `fetch = FetchMode.EAGER`, the framework may still wrap the value; it is usually resolved immediately, but you should not rely on the internal representation.
- Serialization frameworks may serialize the proxy instead of the underlying entity unless the getter is accessed first; force load with `getRole()` before serialization if needed.
- Direct assignment should go through a setter that calls `lazyRef.setValue(entity)`; do not replace the `LazyReference` instance.
- There is no automatic refresh; if the related entity changes in the database, create a new aggregate or implement a manual refresh method.

Anti‑patterns:
| Pattern | Problem | Correct Approach |
|---------|---------|------------------|
| Exposing `LazyReference<Role>` in public API | Leaks internal lazy abstraction | Expose `Role getRole()` |
| Calling `role.setValue(null)` to force reload later | No reload mechanism; stays null | Create a new `LazyReference<>(supplier)` |
| Caching `role.get()` result globally | Stale reference risk | Re-access via getter within a valid context |

If you need to check loaded state without triggering a query, extend the entity with an additional method using `peekIfLoaded()` from `LazyReference`.

---

## Recommendation

Review this file after upgrading the framework—capabilities may evolve and remove some limitations. Propose enhancements by opening an issue referencing section numbers above.
