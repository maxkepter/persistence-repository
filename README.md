# Java Persistence Framework

A lightweight, annotation-based Object-Relational Mapping (ORM) framework for Java applications, providing CRUD operations, query building, transaction management, and caching capabilities.

## Features

- 🚀 **Annotation-Based Mapping**: Define entities using simple annotations
- 📦 **Repository Pattern**: Clean data access layer with CRUD operations
- 🔍 **Query Builders**: Type-safe SQL generation without raw strings
- 💾 **Transaction Management**: Thread-safe transaction context with nested support
- ⚡ **First-Level Caching**: Automatic entity caching within transactions
- 🔗 **Relationship Support**: OneToOne, OneToMany, ManyToOne with lazy/eager loading
- 🔄 **Type Converters**: Custom type conversion for complex fields
- 🎯 **Schema Generation**: Automatic DDL generation from entities
- 📖 **Pagination**: Built-in pagination support for large result sets

---

## Table of Contents

- [Quick Start](#quick-start)
- [Installation](#installation)
- [Core Concepts](#core-concepts)
- [Usage Examples](#usage-examples)
- [Documentation](#documentation)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Contributing](#contributing)

---

## Quick Start

### 1. Define Your Entity

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

    // Getters and setters...
}
```

### 2. Create a Repository

```java
public class ProductRepository extends AbstractRepository<Product, Long> {
    public ProductRepository() {
        super(Product.class);
    }

    public List<Product> findByCategory(String category) {
        ClauseBuilder clause = new ClauseBuilder()
            .where("category = ?", category)
            .orderBy("name ASC");
        return (List<Product>) findWithCondition(clause);
    }
}
```

### 3. Use the Repository

```java
// Initialize
EntityRegistry.register(Product.class);
ProductRepository productRepo = new ProductRepository();

// Create
TransactionManager.beginTransaction();
try {
    Product product = new Product();
    product.setName("Laptop");
    product.setPrice(999.99);
    productRepo.save(product);

    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}

// Read
Product product = productRepo.findById(1L);
List<Product> allProducts = (List<Product>) productRepo.findAll();

// Update
product.setPrice(899.99);
productRepo.update(product);

// Delete
productRepo.deleteById(1L);
```

---

## Installation

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ or H2 Database (for testing)

### Maven Dependencies

```xml
<dependencies>
    <!-- MySQL Connector -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.4.0</version>
    </dependency>

    <!-- H2 Database (for testing) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.224</version>
    </dependency>

    <!-- JUnit 5 (for testing) -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Configuration

Create `application.properties`:

```properties
# Database Connection
db.url=jdbc:mysql://localhost:3306/your_database
db.username=your_username
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

---

## Core Concepts

### Entities

Entities are Java classes annotated with `@Entity` that map to database tables.

```java
@Entity(tableName = "users")
public class User {
    @Key
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;
}
```

### Repositories

Repositories provide CRUD operations and query capabilities for entities.

```java
public class UserRepository extends AbstractRepository<User, Long> {
    public UserRepository() {
        super(User.class);
    }
}
```

### Transactions

Group multiple operations into atomic units:

```java
TransactionManager.beginTransaction();
try {
    userRepo.save(user);
    accountRepo.update(account);
    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
}
```

### Query Builders

Build SQL queries programmatically:

```java
SelectBuilder<Product> builder = SelectBuilder.builder("products")
    .columns("id", "name", "price")
    .where("price > ?", 100.0)
    .orderBy(List.of(new Order("name", true)))
    .limit(10);
```

---

## Usage Examples

### Example 1: Basic CRUD Operations

```java
// Register entity
EntityRegistry.register(Product.class);

// Create repository
ProductRepository repo = new ProductRepository();

// CREATE
TransactionManager.beginTransaction();
Product product = new Product();
product.setName("Wireless Mouse");
product.setPrice(29.99);
Product saved = repo.save(product);
TransactionManager.commit();

// READ
Product found = repo.findById(saved.getId());
System.out.println(found.getName());

// UPDATE
found.setPrice(24.99);
repo.update(found);

// DELETE
repo.deleteById(found.getId());
```

### Example 2: Complex Queries

```java
// Find products in price range
ClauseBuilder clause = new ClauseBuilder()
    .where("price BETWEEN ? AND ?", 50.0, 200.0)
    .where("status = ?", "ACTIVE")
    .orderBy("price DESC")
    .limit(20);

List<Product> products = (List<Product>) repo.findWithCondition(clause);
```

### Example 3: Pagination

```java
// Page 1: items 0-19
PageRequest pageRequest = new PageRequest(0, 20);
Page<Product> page = repo.findAll(pageRequest);

System.out.println("Total products: " + page.getTotalElements());
System.out.println("Total pages: " + page.getTotalPages());

for (Product product : page.getContent()) {
    System.out.println(product.getName());
}
```

### Example 4: Relationships

```java
@Entity(tableName = "authors")
public class Author {
    @Key
    private Long id;

    @OneToMany(mappedBy = "author", fetch = FetchMode.LAZY)
    private List<Book> books;
}

@Entity(tableName = "books")
public class Book {
    @Key
    private Long id;

    @ManyToOne(joinColumn = "author_id")
    private Author author;
}

// Usage
Author author = authorRepo.findById(1L);
List<Book> books = author.getBooks();  // Lazy loaded
```

### Example 5: Type Converters

```java
// Define enum
public enum Status {
    ACTIVE, INACTIVE, ARCHIVED
}

// Create converter
public class StatusConverter extends EnumConverter<Status> {
    public StatusConverter() {
        super(Status.class);
    }
}

// Use in entity
@Column(name = "status", type = "VARCHAR", length = 20)
@Convert(converter = StatusConverter.class)
private Status status;
```

---

## Documentation

Comprehensive documentation is available in the `docs/` directory:

| Document                                  | Description                                               |
| ----------------------------------------- | --------------------------------------------------------- |
| [annotations.md](docs/annotations.md)     | Entity mapping annotations (@Entity, @Column, @Key, etc.) |
| [repository.md](docs/repository.md)       | Repository pattern and CRUD operations                    |
| [entity.md](docs/entity.md)               | Entity metadata, relationships, and lazy loading          |
| [query-builder.md](docs/query-builder.md) | Query builders (SELECT, INSERT, UPDATE, DELETE)           |
| [transaction.md](docs/transaction.md)     | Transaction management and patterns                       |
| [cache.md](docs/cache.md)                 | First-level caching system                                |
| [Converter.md](docs/Converter.md)         | Type converters for custom mappings                       |

---

## Project Structure

```
persistence-repository/
├── src/
│   ├── main/
│   │   ├── java/com/example/persistence_repository/
│   │   │   ├── common/              # Application entities and repositories
│   │   │   │   ├── model/           # Domain models
│   │   │   │   └── repository/      # Repository implementations
│   │   │   ├── persistence/         # Framework code
│   │   │   │   ├── annotation/      # @Entity, @Column, @Key, etc.
│   │   │   │   ├── cache/           # Caching system
│   │   │   │   ├── config/          # Database configuration
│   │   │   │   ├── entity/          # Entity metadata and schema
│   │   │   │   ├── exception/       # Custom exceptions
│   │   │   │   ├── query/           # Query builders
│   │   │   │   └── repository/      # Repository base classes
│   │   │   ├── Main.java            # Application entry point
│   │   │   └── SampleDataLoader.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                        # Unit and integration tests
├── docs/                            # Documentation
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────┐
│     Application Layer               │
│  (Services, Controllers)            │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│     Repository Layer                │
│  (Data Access, CRUD Operations)     │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│     Persistence Framework           │
│  (ORM, Query Builders, Cache)       │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│     Database                        │
│  (MySQL, H2, etc.)                  │
└─────────────────────────────────────┘
```

### Key Components

1. **Annotations**: Define entity mappings
2. **EntityMeta**: Runtime metadata from annotations
3. **EntityRegistry**: Global registry of all entities
4. **Repositories**: Data access abstraction
5. **Query Builders**: SQL generation
6. **TransactionManager**: Transaction coordination
7. **EntityCache**: First-level caching
8. **SchemaGenerator**: DDL generation

---

## Best Practices

### 1. Always Use Transactions

```java
TransactionManager.beginTransaction();
try {
    // Your operations here
    TransactionManager.commit();
} catch (SQLException e) {
    TransactionManager.rollback();
    throw e;
}
```

### 2. Register All Entities at Startup

```java
public class Application {
    public static void main(String[] args) {
        // Register entities
        EntityRegistry.register(Product.class);
        EntityRegistry.register(Category.class);
        EntityRegistry.register(User.class);

        // Generate schema
        SchemaGenerator.withDefault().generateAll();

        // Start application
    }
}
```

### 3. Use Service Layer for Business Logic

```java
public class ProductService {
    private ProductRepository repo = new ProductRepository();

    public Product createProduct(Product product) throws SQLException {
        TransactionManager.beginTransaction();
        try {
            // Validation
            if (product.getPrice() < 0) {
                throw new IllegalArgumentException("Price must be positive");
            }

            // Save
            Product saved = repo.save(product);

            TransactionManager.commit();
            return saved;
        } catch (Exception e) {
            TransactionManager.rollback();
            throw e;
        }
    }
}
```

### 4. Prefer Lazy Loading for Collections

```java
// Good - lazy loading
@OneToMany(mappedBy = "author", fetch = FetchMode.LAZY)
private List<Book> books;

// Use eager only when always needed
@OneToOne(joinColumn = "profile_id", fetch = FetchMode.EAGER)
private Profile profile;
```

### 5. Use Pagination for Large Result Sets

```java
// Good
PageRequest pageRequest = new PageRequest(0, 50);
Page<Product> page = repo.findAll(pageRequest);

// Avoid loading all at once
Iterable<Product> all = repo.findAll();  // Could be millions!
```

---

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

```bash
# Clone repository
git clone https://github.com/maxkepter/persistence-repository.git
cd persistence-repository

# Build project
mvn clean install

# Run tests
mvn test

# Generate documentation
mvn javadoc:javadoc
```

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## Authors

- **Kepter** - _Framework Developer_
- **Nguyen Anh Tu** - _Framework Developer_

---

## Acknowledgments

- Inspired by JPA (Java Persistence API)
- Built with simplicity and performance in mind
- Designed for educational and production use

---

## Support

For questions, issues, or feature requests:

- 📧 Email: [Your email]
- 🐛 Issues: [GitHub Issues](https://github.com/maxkepter/persistence-repository/issues)
- 📖 Documentation: See `docs/` directory

---

**Happy Coding! 🚀**
