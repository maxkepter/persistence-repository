package com.example.persistence_repository.persistence.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.annotation.Entity;
import com.example.persistence_repository.persistence.annotation.Key;
import com.example.persistence_repository.persistence.annotation.ManyToOne;
import com.example.persistence_repository.persistence.annotation.OneToMany;
import com.example.persistence_repository.persistence.annotation.OneToOne;
import com.example.persistence_repository.persistence.entity.relation.RelationshipMeta;
import com.example.persistence_repository.persistence.entity.relation.RelationshipType;
import com.example.persistence_repository.persistence.exception.DuplicateKeyException;

/**
 * Holds structural and relational metadata for a single entity type.
 * <p>
 * Produced once (typically at startup) via {@link #scanAnnotation(Class)} and
 * then
 * reused for query building, result materialization, and relation resolution.
 * This abstraction decouples reflection scanning from runtime logic.
 * </p>
 *
 * <h3>Captured aspects</h3>
 * <ul>
 * <li>Table name (derived or explicit via {@code @Entity}).</li>
 * <li>Field to column mapping (only fields annotated with
 * {@code @Column}).</li>
 * <li>Primary key field (annotated with {@code @Key}).</li>
 * <li>Declared relationships (OneToOne / OneToMany / ManyToOne) as
 * {@link RelationshipMeta}.</li>
 * </ul>
 *
 * <h3>Design notes</h3>
 * <ul>
 * <li>Immutable after construction (no setters).</li>
 * <li>Does not perform any DB access.</li>
 * <li>Currently no caching layer here; caller should reuse instances
 * externally.</li>
 * </ul>
 *
 * <h3>Limitations / Future extensions</h3>
 * <ul>
 * <li>No fetch strategy, cascade metadata yet.</li>
 * <li>No validation of {@code mappedBy} consistency (can be added on
 * bootstrap).</li>
 * </ul>
 */
public class EntityMeta<E> {
    private Class<E> clazz;
    private String tableName;
    private Map<String, ColumnMeta> fieldToColumnMap;
    private List<Field> fields;
    private Field keyField;
    private List<RelationshipMeta> relationships;

    public EntityMeta(Class<E> clazz, String tableName, Map<String, ColumnMeta> fieldToColumnMap, List<Field> fields,
            Field keyField,
            List<RelationshipMeta> relationships) {
        this.clazz = clazz;
        this.tableName = tableName;
        this.fieldToColumnMap = fieldToColumnMap;
        this.fields = fields;
        this.keyField = keyField;
        this.relationships = relationships;
    }

    /**
     * Scans a concrete entity class for ORM-style annotations and produces an
     * {@link EntityMeta}.
     *
     * <h4>Rules</h4>
     * <ul>
     * <li>Class must be annotated with {@code @Entity}.</li>
     * <li>Only fields annotated with {@code @Column} are mapped as DB columns.</li>
     * <li>The first (and only) field annotated with {@code @Key} becomes the
     * primary key.</li>
     * <li>Relationships are discovered from {@code @OneToOne}, {@code @OneToMany},
     * {@code @ManyToOne}.</li>
     * <li>Generic element type of a {@code @OneToMany} collection is resolved when
     * possible; otherwise falls back to {@code Object.class}.</li>
     * </ul>
     *
     * @param clazz concrete entity class
     * @return constructed immutable metadata
     * @throws IllegalArgumentException if the class is not annotated with
     *                                  {@code @Entity} or duplicate key columns
     *                                  found
     */
    public static <E> EntityMeta<E> scanAnnotation(Class<E> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @Entity");
        }

        String tableName = clazz.getAnnotation(Entity.class).tableName();
        if (tableName.isEmpty()) {
            tableName = clazz.getSimpleName().toLowerCase() + "s";
        }

        List<Field> fields = List.of(clazz.getDeclaredFields());
        Field keyField = null;

        Map<String, ColumnMeta> fieldToColumnMap = new HashMap<>();
        List<RelationshipMeta> relationships = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(ManyToOne.class)) {
                ManyToOne ann = field.getAnnotation(ManyToOne.class);
                Class<?> targetType = field.getType();
                // Unwrap LazyReference<T> if relationship is declared as LazyReference<Book>
                if (targetType == LazyReference.class) {
                    Type gtype = field.getGenericType();
                    if (gtype instanceof ParameterizedType pt) {
                        Type[] args = pt.getActualTypeArguments();
                        if (args.length == 1 && args[0] instanceof Class<?> c) {
                            targetType = c;
                        }
                    }
                }
                relationships.add(new RelationshipMeta(
                        RelationshipType.MANY_TO_ONE,
                        field,
                        targetType,
                        ann.joinColumn(),
                        null,
                        false,
                        ann.fetch()));
            }

            if (field.isAnnotationPresent(OneToMany.class)) {
                OneToMany ann = field.getAnnotation(OneToMany.class);
                Class<?> target = Object.class;
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType pt) {
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length == 1 && args[0] instanceof Class<?> c) {
                        target = c;
                    }
                }
                relationships.add(new RelationshipMeta(
                        RelationshipType.ONE_TO_MANY,
                        field,
                        target,
                        ann.joinColumn(),
                        ann.mappedBy(),
                        true,
                        ann.fetch()));
            }

            if (field.isAnnotationPresent(OneToOne.class)) {
                OneToOne ann = field.getAnnotation(OneToOne.class);
                relationships.add(new RelationshipMeta(
                        RelationshipType.ONE_TO_ONE,
                        field,
                        field.getType(),
                        ann.joinColumn(),
                        ann.mappedBy(),
                        false,
                        ann.fetch()));
            }

            // Default: use exact field name as column name (preserve case) for consistency
            // with code using field.getName()
            String columnName = field.getName();
            String type = "VARCHAR";
            int length = 255;
            boolean nullable = true;
            boolean unique = false;

            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);

                if (!column.name().isEmpty()) {
                    columnName = column.name();
                }
                type = column.type();
                length = column.length();
                nullable = column.nullable();
                unique = column.unique();

                if (field.isAnnotationPresent(Key.class)) {
                    if (keyField != null) {
                        throw new DuplicateKeyException("Duplicate key field in class " + clazz.getName());

                    }
                    keyField = field;
                }

                if (fieldToColumnMap.containsKey(field.getName())) {
                    throw new IllegalArgumentException(
                            "Duplicate column name " + columnName + " in class " + clazz.getName());
                } else {
                    fieldToColumnMap.put(field.getName(), new ColumnMeta(columnName, type, nullable, length, unique));

                }

            }
        }

        EntityMeta<E> entityMeta = new EntityMeta<>(clazz, tableName, fieldToColumnMap, fields, keyField,
                relationships);

        return entityMeta;

    }

    /**
     * @return the concrete Java class representing this entity.
     */
    public Class<E> getClazz() {
        return clazz;
    }

    /**
     * Resolved SQL table name used for generating DML / DDL.
     * 
     * @return non-null table identifier.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Provides a mapping of (Java field name) -> (column metadata) for all scalar
     * persistent fields discovered.
     * 
     * @return immutable map of column metadata (may be empty).
     */
    public Map<String, ColumnMeta> getFieldToColumnMap() {
        return fieldToColumnMap;
    }

    /**
     * All declared fields on the class (including those not annotated with
     * {@code @Column}).
     * 
     * @return immutable list, never null.
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * The primary key field if one was discovered. If multiple keys are needed in
     * the future
     * (composite keys), this API would need extension.
     * 
     * @return the key field or {@code null} if none declared.
     */
    public Field getKeyField() {
        return keyField;
    }

    /**
     * Relationship descriptors discovered from the entity's annotations.
     * 
     * @return immutable list (may be empty if no relationships declared).
     */
    public List<RelationshipMeta> getRelationships() {
        return relationships;
    }

}
