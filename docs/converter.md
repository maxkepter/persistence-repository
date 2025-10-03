# Type Converter Documentation

This document describes the type conversion system for mapping complex Java types to database columns and vice versa.

## Table of Contents

- [Overview](#overview)
- [AttributeConverter Interface](#attributeconverter-interface)
- [EnumConverter](#enumconverter)
- [@Convert Annotation](#convert-annotation)
- [Creating Custom Converters](#creating-custom-converters)
- [Common Use Cases](#common-use-cases)
- [Best Practices](#best-practices)

---

## Overview

The type converter system enables bidirectional conversion between:

- **Entity Attributes**: Java types used in your domain models
- **Database Columns**: SQL types stored in the database

This is essential for:

- Enums → String/Integer conversion
- JSON → String conversion
- LocalDateTime → Timestamp conversion
- Custom value objects → Primitive types
- Encrypted fields

### Key Components

- **AttributeConverter**: Interface for defining conversions
- **EnumConverter**: Built-in converter for enums
- **@Convert**: Annotation to specify which converter to use

---

## AttributeConverter Interface

The base interface for all type converters.

### Interface Definition

```java
public interface AttributeConverter<C, F> {
    C convertToDatabaseColumn(F attribute);
    F convertToEntityAttribute(C dbData);
}
```

### Type Parameters

- **C**: Column type (database representation)
- **F**: Field type (Java attribute type)

### Methods

#### convertToDatabaseColumn()

```java
C convertToDatabaseColumn(F attribute)
```

Converts an entity attribute to its database representation.

**Parameters**:

- `attribute`: The Java object to convert (can be null)

**Returns**: Database column value

**Called**: When saving or updating entities

#### convertToEntityAttribute()

```java
F convertToEntityAttribute(C dbData)
```

Converts a database column value to its Java representation.

**Parameters**:

- `dbData`: The database value to convert (can be null)

**Returns**: Java attribute value

**Called**: When loading entities from database

---

## EnumConverter

Built-in converter for enum types using string representation.

### Structure

```java
public class EnumConverter<E extends Enum> implements AttributeConverter<String, Enum<?>> {
    private final Class<E> enumType;

    public EnumConverter(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public String convertToDatabaseColumn(Enum<?> attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public Enum<?> convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Enum.valueOf(enumType, dbData);
    }
}
```

### Usage

#### Define Enum

```java
public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    DISCONTINUED,
    OUT_OF_STOCK
}
```

#### Create Converter

```java
public class ProductStatusConverter extends EnumConverter<ProductStatus> {
    public ProductStatusConverter() {
        super(ProductStatus.class);
    }
}
```

#### Apply to Entity

```java
@Entity(tableName = "products")
public class Product {
    @Key
    private Long id;

    @Column(name = "status", type = "VARCHAR", length = 20)
    @Convert(converter = ProductStatusConverter.class)
    private ProductStatus status;

    // Getters and setters...
}
```

### Database Storage

```sql
-- Stored as string in database
INSERT INTO products (id, status) VALUES (1, 'ACTIVE');
INSERT INTO products (id, status) VALUES (2, 'DISCONTINUED');
```

---

## @Convert Annotation

Specifies which converter to use for a field.

### Syntax

```java
@Convert(converter = ConverterClass.class)
private FieldType fieldName;
```

### Attributes

| Attribute   | Type  | Required | Description                |
| ----------- | ----- | -------- | -------------------------- |
| `converter` | Class | Yes      | The converter class to use |

### Example

```java
@Column(name = "account_status", type = "VARCHAR", length = 15)
@Convert(converter = AccountStatusConverter.class)
private AccountStatus status;
```

---

## Creating Custom Converters

### Example 1: Boolean to Y/N Converter

```java
public class BooleanYNConverter implements AttributeConverter<String, Boolean> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return "Y".equalsIgnoreCase(dbData);
    }
}
```

**Usage**:

```java
@Column(name = "is_active", type = "CHAR", length = 1)
@Convert(converter = BooleanYNConverter.class)
private Boolean active;
```

### Example 2: JSON Converter

```java
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter<T> implements AttributeConverter<String, T> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Class<T> type;

    public JsonConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(dbData, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
}
```

**Usage**:

```java
public class AddressJsonConverter extends JsonConverter<Address> {
    public AddressJsonConverter() {
        super(Address.class);
    }
}

@Entity(tableName = "users")
public class User {
    @Column(name = "address_json", type = "TEXT")
    @Convert(converter = AddressJsonConverter.class)
    private Address address;
}
```

### Example 3: LocalDateTime to Timestamp Converter

```java
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LocalDateTimeConverter implements AttributeConverter<Timestamp, LocalDateTime> {

    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null ? null : Timestamp.valueOf(attribute);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp dbData) {
        return dbData == null ? null : dbData.toLocalDateTime();
    }
}
```

**Usage**:

```java
@Column(name = "created_at", type = "TIMESTAMP")
@Convert(converter = LocalDateTimeConverter.class)
private LocalDateTime createdAt;
```

---

## Best Practices

### 1. Handle Null Values

```java
// Good - null-safe
@Override
public String convertToDatabaseColumn(Status attribute) {
    return attribute == null ? null : attribute.name();
}

// Bad - NullPointerException risk
@Override
public String convertToDatabaseColumn(Status attribute) {
    return attribute.name();  // Fails if attribute is null
}
```

### 2. Keep Converters Stateless

```java
// Good - stateless
public class StatusConverter implements AttributeConverter<String, Status> {
    @Override
    public String convertToDatabaseColumn(Status attribute) {
        return attribute == null ? null : attribute.name();
    }
}
```

### 3. Use Appropriate Database Types

```java
// Good - sufficient length for enum names
@Column(name = "status", type = "VARCHAR", length = 20)
@Convert(converter = StatusConverter.class)
private Status status;

// Good - large enough for JSON
@Column(name = "metadata", type = "TEXT")
@Convert(converter = JsonConverter.class)
private Metadata metadata;
```

---

## Summary

The type converter system provides:

- ✅ Bidirectional type conversion
- ✅ Built-in enum support
- ✅ Custom converter capability
- ✅ Transparent integration
- ✅ Null-safe handling
- ✅ Reusable converters

For entity mapping, see [entity.md](entity.md).  
For annotations, see [annotations.md](annotations.md).
