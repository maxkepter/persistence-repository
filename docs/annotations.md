# Persistence Annotations Guide

This document explains the custom persistence annotations provided by the framework and demonstrates how to use them to map Java domain models to relational database tables.

## Table of Contents

- [@Entity](#entity)
- [@Column](#column)
- [@Key](#key)
- [@OneToOne](#onetoone)
- [@OneToMany](#onetomany)
- [@ManyToOne](#manytoone)
- [Fetch Strategies](#fetch-strategies)
- [Complete Example](#complete-example)
- [Best Practices](#best-practices)

---

## @Entity

Marks a class as a persistent entity. Required attribute:

| Attribute   | Type   | Required | Description                                  |
| ----------- | ------ | -------- | -------------------------------------------- |
| `tableName` | String | Yes      | Physical name of the database table or view. |

```java
@Entity(tableName = "authors")
public class Author { /* ... */ }
```

## @Column

Maps a field to a database column and optionally customizes its metadata.

| Attribute  | Type    | Default   | Description                                                                  |
| ---------- | ------- | --------- | ---------------------------------------------------------------------------- |
| `name`     | String  | (derived) | Explicit column name; if empty the framework infers one from the field name. |
| `type`     | String  | `VARCHAR` | SQL column type token (e.g. VARCHAR, INT, DATE).                             |
| `nullable` | boolean | `true`    | Whether NULL values are allowed.                                             |
| `length`   | int     | `255`     | Length for variable-sized types (e.g. VARCHAR). Ignored otherwise.           |
| `unique`   | boolean | `false`   | Declares a single-column UNIQUE constraint.                                  |

```java
@Column(name = "first_name", length = 100, nullable = false)
private String firstName;

@Column(name = "email", unique = true, length = 320)
private String email;
```

## @Key

Marks a field as the primary key of the entity. Typically used on a single field:

```java
@Key
@Column(type = "BIGINT", nullable = false)
private Long id;
```

The framework should treat `@Key` fields as identifier columns for lookups, updates, and joins.

## @OneToOne

Defines a one-to-one association. At most one instance of each entity is related to the other.

| Attribute    | Type      | Required | Description                                                                                    |
| ------------ | --------- | -------- | ---------------------------------------------------------------------------------------------- |
| `mappedBy`   | String    | depends  | Field name on the owning side when this side is inverse. Empty if this side owns the relation. |
| `joinColumn` | String    | depends  | FK column name when this side is owning. Ignored if `mappedBy` is non-empty.                   |
| `fetch`      | FetchMode | `LAZY`   | Loading strategy.                                                                              |

Owning side example:

```java
@OneToOne(joinColumn = "profile_id")
private Profile profile;
```

Inverse side example:

```java
@OneToOne(mappedBy = "profile")
private User user;
```

## @OneToMany

Declares a one-to-many relationship where this side holds a collection of children.

| Attribute    | Type      | Required | Description                                                                                           |
| ------------ | --------- | -------- | ----------------------------------------------------------------------------------------------------- |
| `mappedBy`   | String    | depends  | Field name on the child (many side) that owns the FK (bidirectional). Empty for unidirectional style. |
| `joinColumn` | String    | depends  | FK column in child table referencing this entity when `mappedBy` is empty.                            |
| `fetch`      | FetchMode | `LAZY`   | Loading strategy.                                                                                     |

Bidirectional example:

```java
@OneToMany(mappedBy = "author")
private List<Book> books = new ArrayList<>();
```

Unidirectional example (framework stores FK in `books.author_id`):

```java
@OneToMany(joinColumn = "author_id")
private List<Book> books = new ArrayList<>();
```

## @ManyToOne

Defines the many side of a many-to-one relationship.

| Attribute    | Type      | Required | Description                                              |
| ------------ | --------- | -------- | -------------------------------------------------------- |
| `joinColumn` | String    | Yes      | FK column in this entity's table referencing the parent. |
| `fetch`      | FetchMode | `LAZY`   | Loading strategy.                                        |

```java
@ManyToOne(joinColumn = "author_id")
private Author author;
```

## Fetch Strategies

The `fetch` attribute uses `FetchMode` (e.g., `LAZY`, `EAGER`). Lazy loading defers database access until the association or collection is accessed, reducing unnecessary queries. Eager loading retrieves the association immediately with the parent entity.

Choose `EAGER` only when the association is always needed, otherwise prefer `LAZY`.

## Complete Example

Below is a simple domain model using all annotations together.

```java
@Entity(tableName = "authors")
public class Author {
    @Key
    @Column(type = "BIGINT", nullable = false)
    private Long id;

    @Column(length = 120, nullable = false)
    private String name;

    @OneToMany(mappedBy = "author")
    private List<Book> books = new ArrayList<>();
}

@Entity(tableName = "books")
public class Book {
    @Key
    @Column(type = "BIGINT", nullable = false)
    private Long id;

    @Column(length = 200, nullable = false)
    private String title;

    @ManyToOne(joinColumn = "author_id")
    private Author author;

    @OneToOne(joinColumn = "detail_id")
    private BookDetail detail;
}

@Entity(tableName = "book_details")
public class BookDetail {
    @Key
    @Column(type = "BIGINT", nullable = false)
    private Long id;

    @Column(length = 30)
    private String language;

    @OneToOne(mappedBy = "detail")
    private Book book;
}
```

## Best Practices

1. Always supply explicit `tableName` for clarity and portability.
2. Keep column names consistent (snake_case or lower case) according to your database convention.
3. Use `@Column(length=...)` for any user input string to avoid vendor defaults.
4. Prefer `LAZY` fetch unless there's a compelling reason for eager loading.
5. Ensure only one side of a bidirectional relationship is marked as owning (defines the foreign key).
6. Avoid circular eager graphs (A eager loads B, B eager loads A) to prevent performance issues.
7. Consider composite keys carefully—current `@Key` supports simple keys; introduce a separate mechanism if you need composites.

## Future Extensions (Ideas)

- Add `@Index` annotation for secondary indexes.
- Support composite unique constraints at the `@Entity` level.
- Add cascade options to relationship annotations.

---

If you have further questions or want to extend the mapping capabilities, update this document to keep it aligned with implementation changes.
