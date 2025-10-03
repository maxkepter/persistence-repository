# Entity Metadata and Mapping Documentation

This document describes the entity metadata system that powers the persistence framework's Object-Relational Mapping (ORM) capabilities.

## Table of Contents

- [Overview](#overview)
- [EntityMeta](#entitymeta)
- [EntityRegistry](#entityregistry)
- [ColumnMeta](#columnmeta)
- [Relationship Metadata](#relationship-metadata)
- [Lazy Loading](#lazy-loading)
- [Type Conversion](#type-conversion)
- [Schema Generation](#schema-generation)

---

## Overview

The entity metadata system scans annotated Java classes and builds runtime metadata that describes:

- Table and column mappings
- Primary key fields
- Relationships between entities
- Type converters
- Lazy loading proxies

This metadata is used by repositories to generate SQL, map ResultSets to objects, and manage relationships.

---

## EntityMeta

`EntityMeta<E>` holds all metadata for an entity class, including:

### Key Components

| Component       | Description                                 |
| --------------- | ------------------------------------------- |
| Table Name      | Physical database table name from `@Entity` |
| Key Field       | Primary key field marked with `@Key`        |
| Column Mappings | Field name → `ColumnMeta` mappings          |
| Relationships   | OneToOne, OneToMany, ManyToOne metadata     |
| Field Accessors | Cached reflection fields for performance    |

### Creating EntityMeta

```java
// Scan annotations and build metadata
EntityMeta<Product> meta = EntityMeta.scanAnnotation(Product.class);

// Access metadata
String tableName = meta.getTableName(); // "products"
Field keyField = meta.getKeyField(); // id field
Map<String, ColumnMeta> columns = meta.getFieldToColumnMap();
```

### Common Methods

```java
// Get table name
String getTableName()

// Get primary key field
Field getKeyField()

// Get column name for a field
String getColnumName(String fieldName)

// Get all persistent fields (excluding relationships)
List<Field> getFields()

// Get column metadata
Map<String, ColumnMeta> getFieldToColumnMap()

// Get relationship metadata
Map<String, RelationshipMeta> getRelationships()
```

### Example Entity with Metadata

```java
@Entity(tableName = "products")
public class Product {
    @Key
    @Column(type = "BIGINT", nullable = false)
    private Long id;

    @Column(name = "product_name", length = 200, nullable = false)
    private String name;

    @Column(type = "DECIMAL(10,2)")
    private Double price;

    @ManyToOne(joinColumn = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product")
    private List<Review> reviews;

    // Getters and setters...
}

// Metadata structure:
// - tableName = "products"
// - keyField = id
// - columns:
//   * id → ColumnMeta(name="id", type="BIGINT", nullable=false)
//   * name → ColumnMeta(name="product_name", type="VARCHAR", length=200)
//   * price → ColumnMeta(name="price", type="DECIMAL(10,2)")
// - relationships:
//   * category → ManyToOne relationship
//   * reviews → OneToMany relationship
```

---

## EntityRegistry

`EntityRegistry` is a global registry that stores `EntityMeta` instances for all registered entity classes.

### Purpose

- Central repository for all entity metadata
- Enables schema generation for all entities
- Supports relationship resolution across entities
- Thread-safe singleton pattern

### Registration

```java
// Register individual entity
EntityRegistry.register(Product.class);
EntityRegistry.register(Category.class);

// Register multiple entities
Class<?>[] entities = {Product.class, Category.class, Review.class};
for (Class<?> entityClass : entities) {
    EntityRegistry.register(entityClass);
}
```

### Retrieval

```java
// Get metadata for a specific entity
EntityMeta<Product> productMeta = EntityRegistry.getMeta(Product.class);

// Get all registered entities
Collection<EntityMeta<?>> allMetas = EntityRegistry.getAllMetas();

// Check if entity is registered
boolean isRegistered = EntityRegistry.isRegistered(Product.class);

// Check if registry is empty
boolean isEmpty = EntityRegistry.isEmpty();
```

### Typical Usage Pattern

```java
public class Application {
    public static void main(String[] args) {
        // 1. Register all entities at startup
        EntityRegistry.register(Account.class);
        EntityRegistry.register(Role.class);
        EntityRegistry.register(Feature.class);
        // ... register all entities

        // 2. Generate database schema
        SchemaGenerator.withDefault().generateAll();

        // 3. Use repositories (they access EntityRegistry automatically)
        AccountRepository accountRepo = new AccountRepository();
    }
}
```

---

## ColumnMeta

`ColumnMeta` represents metadata for a single database column.

### Properties

```java
public class ColumnMeta {
    private String name;           // Column name in database
    private String type;           // SQL type (VARCHAR, INT, etc.)
    private int length;            // Length for VARCHAR, etc.
    private boolean nullable;      // NULL constraint
    private boolean unique;        // UNIQUE constraint
    private boolean isKey;         // Is primary key?
    private AttributeConverter<?, ?> converter;  // Type converter
}
```

### Usage Example

```java
EntityMeta<Product> meta = EntityMeta.scanAnnotation(Product.class);
ColumnMeta nameMeta = meta.getFieldToColumnMap().get("name");

System.out.println("Column: " + nameMeta.getName());        // "product_name"
System.out.println("Type: " + nameMeta.getType());          // "VARCHAR"
System.out.println("Length: " + nameMeta.getLength());      // 200
System.out.println("Nullable: " + nameMeta.isNullable());   // false
System.out.println("Unique: " + nameMeta.isUnique());       // false
```

### Type Inference

If `@Column` is not specified, default metadata is inferred:

```java
// Without @Column annotation
private String description;

// Inferred ColumnMeta:
// name = "description"
// type = "VARCHAR"
// length = 255
// nullable = true
// unique = false
```

---

## Relationship Metadata

`RelationshipMeta` describes associations between entities.

### Structure

```java
public class RelationshipMeta {
    private RelationshipType type;    // ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE
    private String mappedBy;          // Field name on inverse side
    private String joinColumn;        // Foreign key column name
    private FetchMode fetchMode;      // LAZY or EAGER
    private Class<?> targetEntity;    // Related entity class
}
```

### Relationship Types

#### ONE_TO_ONE

Single-valued relationships must be wrapped in `LazyReference<T>` to support uniform lazy/eager semantics and optional deferred loading.

```java
@Entity(tableName = "users")
public class User {
    @Key
    private Long id;

    @OneToOne(joinColumn = "profile_id", fetch = FetchMode.EAGER)
    private LazyReference<Profile> profile; // wrapped

    public Profile getProfile() { // accessor triggers load
        return profile.get();
    }

    public void setProfile(Profile p) {
        this.profile.setValue(p);
    }
}

// RelationshipMeta:
// - type = ONE_TO_ONE
// - joinColumn = "profile_id"
// - fetchMode = EAGER (may still defer until first access)
// - targetEntity = Profile.class
```

#### ONE_TO_MANY

```java
@Entity(tableName = "authors")
public class Author {
    @Key
    private Long id;

    @OneToMany(mappedBy = "author", fetch = FetchMode.LAZY)
    private List<Book> books;
}

// RelationshipMeta:
// - type = ONE_TO_MANY
// - mappedBy = "author"
// - fetchMode = LAZY
// - targetEntity = Book.class
```

#### MANY_TO_ONE

Like `@OneToOne`, a `@ManyToOne` target must be wrapped in `LazyReference<T>`.

```java
@Entity(tableName = "books")
public class Book {
    @Key
    private Long id;

    @ManyToOne(joinColumn = "author_id", fetch = FetchMode.LAZY)
    private LazyReference<Author> author; // wrapped

    public Author getAuthor() {
        return author.get();
    }

    public void setAuthor(Author a) {
        this.author.setValue(a);
    }
}

// RelationshipMeta:
// - type = MANY_TO_ONE
// - joinColumn = "author_id"
// - fetchMode = LAZY
// - targetEntity = Author.class
```

---

## Lazy Loading

The framework provides lazy loading proxies to defer database access until needed.

### LazyReference<T>

Used for single-value relationships (`@OneToOne`, `@ManyToOne`). All such fields in entities MUST be declared as `LazyReference<T>` (not the raw target type). Public getters should return the underlying entity to hide the wrapper.

```java
@ManyToOne(joinColumn = "RoleID", fetch = FetchMode.EAGER)
private LazyReference<Role> role;

public Role getRole() { // consumer-facing accessor
    return role.get();
}

public void setRole(Role r) {
    this.role.setValue(r);
}
```

Rationale:

- Consistent API for lazy vs eager fetch modes
- Allows deferred resolution while keeping domain model simple
- Prevents accidental premature loading during serialization if getter unused
- Enables future enhancements (e.g., change tracking) without altering field signatures

### LazyList<T>

Used for collection relationships (`@OneToMany`):

```java
// Internal implementation
public class LazyList<T> extends ArrayList<T> {
    private boolean loaded = false;

    @Override
    public Iterator<T> iterator() {
        ensureLoaded();
        return super.iterator();
    }

    private void ensureLoaded() {
        if (!loaded) {
            addAll(loadCollectionFromDatabase());
            loaded = true;
        }
    }
}
```

### Loading Behavior

```java
Author author = authorRepo.findById(1L);

// Books not loaded yet (LAZY)
List<Book> books = author.getBooks();

// First access triggers database query
for (Book book : books) {  // Query executed here
    System.out.println(book.getTitle());
}

// Subsequent access uses loaded data
for (Book book : books) {  // No query
    System.out.println(book.getTitle());
}
```

### RelationLoader

`RelationLoader` handles the actual database queries for lazy loading:

```java
public class RelationLoader {
    // Load single entity (OneToOne, ManyToOne)
    public static <T> T loadReference(
        Class<T> targetType,
        String joinColumn,
        Object foreignKeyValue
    );

    // Load collection (OneToMany)
    public static <T> List<T> loadCollection(
        Class<T> targetType,
        String foreignKeyColumn,
        Object foreignKeyValue
    );
}
```

---

## Type Conversion

### AttributeConverter

Custom converters handle complex type mappings:

```java
public interface AttributeConverter<X, Y> {
    // Convert entity attribute to database column
    Y convertToDatabaseColumn(X attribute);

    // Convert database column to entity attribute
    X convertToEntityAttribute(Y dbData);
}
```

### EnumConverter

Built-in converter for enums:

```java
public class ProductStatusConverter implements AttributeConverter<ProductStatus, String> {
    @Override
    public String convertToDatabaseColumn(ProductStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public ProductStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ProductStatus.valueOf(dbData);
    }
}
```

### Using Converters

```java
@Entity(tableName = "products")
public class Product {
    @Column(name = "status", type = "VARCHAR", length = 20)
    @Convert(converter = ProductStatusConverter.class)
    private ProductStatus status;
}
```

See [Converter.md](Converter.md) for details.

---

## Schema Generation

`SchemaGenerator` uses entity metadata to create database tables.

### Features

- Automatic DDL generation from annotations
- Topological sorting for foreign key dependencies
- Support for circular references
- Foreign key constraint generation
- Drop and recreate capability

### Usage

```java
// Register entities
EntityRegistry.register(Product.class);
EntityRegistry.register(Category.class);
EntityRegistry.register(Review.class);

// Generate schema
SchemaGenerator generator = SchemaGenerator.withDefault();
generator.generateAll();
```

### Generated DDL Example

For the `Product` entity:

```sql
DROP TABLE IF EXISTS products;

CREATE TABLE products (
    id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    price DECIMAL(10,2),
    category_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
```

### Options

```java
SchemaGenerator.Options options = new SchemaGenerator.Options();
options.dropIfExists = true;           // Drop tables before creating
options.printDdl = true;               // Print DDL to console
options.fkOnDelete = "CASCADE";        // ON DELETE action
options.fkOnUpdate = "CASCADE";        // ON UPDATE action

SchemaGenerator generator = new SchemaGenerator(options);
generator.generateAll();
```

---

## Best Practices

### 1. Register Entities at Application Startup

```java
public static void initializeEntities() {
    Class<?>[] entities = {
        Account.class, Role.class, Feature.class,
        Product.class, Category.class, Review.class
    };

    for (Class<?> entityClass : entities) {
        EntityRegistry.register(entityClass);
    }
}
```

### 2. Use Lazy Loading for Collections

```java
// Good - won't load all books unless needed
@OneToMany(mappedBy = "author", fetch = FetchMode.LAZY)
private List<Book> books;

// Avoid - always loads all books
@OneToMany(mappedBy = "author", fetch = FetchMode.EAGER)
private List<Book> books;
```

### 3. Specify Column Metadata Explicitly

```java
// Good - explicit and clear
@Column(name = "email_address", type = "VARCHAR", length = 320, unique = true)
private String email;

// Less clear - relies on defaults
@Column
private String email;
```

### 4. Handle Circular References Carefully

```java
// Parent entity
@OneToMany(mappedBy = "parent")
private List<Child> children;

// Child entity
@ManyToOne(joinColumn = "parent_id")
private Parent parent;

// ⚠️ Avoid eager loading both sides to prevent infinite loops
```

---

## Summary

The entity metadata system provides:

- ✅ Annotation-based OR mapping
- ✅ Automatic metadata scanning
- ✅ Relationship management
- ✅ Lazy loading support
- ✅ Type conversion
- ✅ Schema generation
- ✅ Global entity registry

For annotation details, see [annotations.md](annotations.md).  
For repository usage, see [repository.md](repository.md).  
For schema generation, see the `SchemaGenerator` class.
