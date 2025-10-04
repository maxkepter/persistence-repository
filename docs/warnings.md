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

## 12. Dual-Field (FK ID + LazyReference) Requirement

All `@ManyToOne` and `@OneToOne` relationships MUST declare both:

1. A concrete foreign key column field (e.g. `private Long roleId;`)
2. A `LazyReference<T>` field for the related entity (e.g. `private LazyReference<Role> role;`)

### Why This Is Mandatory

| Concern             | Without FK Field                        | With FK Field                                      |
| ------------------- | --------------------------------------- | -------------------------------------------------- |
| Filtering / queries | Must load relation to get ID            | Use ID directly (`clause.equal("RoleID", roleId)`) |
| Serialization       | Risk of triggering load unintentionally | Serialize ID only                                  |
| Debugging           | Hard to see linkage                     | Explicit FK visible                                |
| Batch operations    | Need entity materialization             | Operate on IDs only                                |

### Required Pattern

```java
@Column(name = "RoleID", type = "BIGINT")
private Long roleId; // persisted FK

@ManyToOne(joinColumn = "RoleID", fetch = FetchMode.LAZY)
private LazyReference<Role> role; // wrapped relation

public Role getRole() { return role.get(); }
public Long getRoleId() { return roleId; }
public void setRole(Role r) {
    this.role.setValue(r);
    this.roleId = (r == null ? null : r.getId()); // keep in sync manually
}
```

### Pitfalls If Omitted

- Hidden N+1 queries: every access to get the FK requires loading the entity.
- Harder migration scripts: FK column name not represented in code.
- Inefficient bulk deletes/updates (need sub-selects instead of simple `WHERE role_id IN (...)`).
- Serialization frameworks may force-load the relation unexpectedly.

### Enforcement Suggestions

- Add a static build-time check (annotation processor or reflection scan) to assert every `@ManyToOne/@OneToOne` has a matching `<name>Id` field.
- Add unit tests that reflectively verify the convention.

> WARNING: Future versions may throw an initialization error if the dual-field pattern is not followed.

---

## Recommendation

Review this file after upgrading the framework—capabilities may evolve and remove some limitations. Propose enhancements by opening an issue referencing section numbers above.

---

## 13. Getter/Setter đúng cách cho LazyReference

Khi làm việc với `LazyReference<T>` trong entity, luôn tuân theo các quy tắc sau để tránh NullPointerException và giữ dữ liệu đồng bộ:

- Không expose `LazyReference<T>` ra ngoài API công khai. Chỉ expose entity thông qua getter: `T getX()`.
- Getter phải an toàn null: nếu wrapper chưa được khởi tạo (entity tạo thủ công, không qua mapper), trả về null thay vì gọi trực tiếp `lazy.get()`.
- Setter không được thay thế thể hiện `LazyReference` bằng thể hiện mới (trừ khi bạn chủ đích thiết kế lại). Hãy dùng `lazy.setValue(value)` để gán giá trị.
- Nếu entity có cả trường FK (ví dụ `roleID`) và `LazyReference<Role> role`, luôn đồng bộ FK trong setter: khi set `role`, cập nhật `roleID` tương ứng; khi set `roleID` trực tiếp, có thể cân nhắc làm rỗng (invalidate) `LazyReference` nếu cần.
- Khi wrapper có thể null (entity tạo bằng constructor), hãy khởi tạo trước khi `setValue`: `if (this.role == null) this.role = new LazyReference<>(() -> value); this.role.setValue(value);`.

### Ví dụ đúng (Account)

```java
@ManyToOne(joinColumn = "RoleID", fetch = FetchMode.EAGER)
private LazyReference<Role> role;

@Column(name = "RoleID", type = "BIGINT")
private Long roleID;

public Role getRole() {
    return role == null ? null : role.get();
}

public void setRole(Role r) {
    if (this.role == null) {
        this.role = new LazyReference<>(() -> r);
    }
    this.role.setValue(r);
    this.roleID = (r == null ? null : r.getRoleID());
}
```

### Ví dụ sai và cách sửa

| Sai                                                                      | Vấn đề                                             | Sửa                                                                         |
| ------------------------------------------------------------------------ | -------------------------------------------------- | --------------------------------------------------------------------------- |
| `this.role.setValue(r);` khi `this.role` có thể null                     | NPE khi entity chưa được mapper khởi tạo wrapper   | Kiểm tra null và khởi tạo `LazyReference` trước khi `setValue`              |
| `this.role = new LazyReference<>(() -> repo.findById(id));` trong setter | Thay thế wrapper phá hỏng cache/trạng thái đã load | Chỉ `setValue` trên wrapper hiện tại, hoặc thiết kế invalidate/reload riêng |
| Không cập nhật `roleID` khi `setRole(r)`                                 | Mất đồng bộ FK và entity liên quan                 | Đồng bộ: `this.roleID = (r == null ? null : r.getRoleID());`                |
| Truy cập trực tiếp `role.get()` từ ngoài entity                          | Rò rỉ chi tiết lazy và khó kiểm soát               | Chỉ expose `getRole()`                                                      |

### Gợi ý bổ sung

- Cân nhắc thêm helper chung trong base entity để khởi tạo an toàn: `protected static <T> LazyReference<T> ensure(LazyReference<T> ref, T v) { if (ref == null) ref = new LazyReference<>(() -> v); ref.setValue(v); return ref; }`.
- Nếu cần biết trạng thái đã load mà không trigger query, thêm method dùng `peekIfLoaded()` trong entity.
