package com.example.persistence_repository.persistence.entity.relation;

import java.lang.reflect.Field;

import com.example.persistence_repository.persistence.entity.FetchMode;

/**
 * Immutable metadata descriptor for a single relationship declared on an entity
 * field.
 * <p>
 * Captured at bootstrap time by {@link EntityMeta#scanAnnotation(Class)} and
 * later
 * consumed by materialization logic (lazy loaders, join fetch planners, cascade
 * handlers).
 * </p>
 * <h3>Fields</h3>
 * <ul>
 * <li>{@code type}: Cardinality / direction ({@link RelationshipType}).</li>
 * <li>{@code field}: The reflective {@link Field} representing the declaration
 * on the source entity.</li>
 * <li>{@code targetType}: The target entity Java type (parsed from generic for
 * collections).</li>
 * <li>{@code joinColumn}: Physical FK column (only meaningful on owning
 * side).</li>
 * <li>{@code mappedBy}: Name of the field on the inverse (non-owning) side (if
 * bidirectional).</li>
 * <li>{@code collection}: Whether this relationship is multi-valued (e.g.
 * {@code List<T>}).</li>
 * </ul>
 * <h3>Thread-safety</h3>
 * Instances are immutable and safe to share.</h3>
 * <h3>Limitations</h3>
 * <ul>
 * <li>No fetch mode / cascade info yet (future extension point).</li>
 * <li>Generic wildcards (e.g. {@code List<? extends X>}) fallback to
 * {@code Object.class}.</li>
 * </ul>
 */
public class RelationshipMeta {
    private final RelationshipType type;
    private final Field field;
    private final Class<?> targetType;
    private final String joinColumn;
    private final String mappedBy;
    private final boolean collection;
    private final FetchMode fetchMode;

    public RelationshipMeta(RelationshipType type, Field field, Class<?> targetType, String joinColumn, String mappedBy,
            boolean collection, FetchMode fetchMode) {
        this.type = type;
        this.field = field;
        this.targetType = targetType;
        this.joinColumn = joinColumn;
        this.mappedBy = mappedBy;
        this.collection = collection;
        this.fetchMode = fetchMode == null ? FetchMode.LAZY : fetchMode;
        this.field.setAccessible(true);
    }

    /**
     * @return the cardinality / direction of this relationship.
     */
    public RelationshipType getType() {
        return type;
    }

    /**
     * @return reflective field on the declaring (source) entity.
     */
    public Field getField() {
        return field;
    }

    /**
     * @return Java class of the target entity (may be {@code Object.class} fallback
     *         if not resolved).
     */
    public Class<?> getTargetType() {
        return targetType;
    }

    /**
     * @return foreign key column name on owning side, or null when not applicable.
     */
    public String getJoinColumn() {
        return joinColumn;
    }

    /**
     * @return name of inverse-side field (for bidirectional mappings) or null if
     *         unidirectional.
     */
    public String getMappedBy() {
        return mappedBy;
    }

    /**
     * @return true if this relationship represents a multi-valued collection.
     */
    public boolean isCollection() {
        return collection;
    }

    /**
     * @return configured fetch strategy (never null; defaults to LAZY)
     */
    public FetchMode getFetchMode() {
        return fetchMode;
    }
}
