package com.example.persistence_repository.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.persistence_repository.persistence.entity.relation.FetchMode;

/**
 * Declares a one-to-many relationship where the annotated field holds a
 * collection of child entities referencing this entity (the one side).
 * <p>
 * Two styles are typically supported:
 * <ul>
 * <li>Bidirectional: use {@link #mappedBy()} to point to the field name on the
 * many-side entity that owns the foreign key.</li>
 * <li>Unidirectional (join table / direct FK): specify {@link #joinColumn()} if
 * the foreign key is stored on the many side referencing this entity.</li>
 * </ul>
 * The framework should validate that at least one of {@code mappedBy} or
 * {@code joinColumn} is meaningful for the configured association pattern.
 * </p>
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
    /**
     * Name of the field on the child (many side) entity that owns the relation.
     * Used for bidirectional mappings. Empty if unidirectional.
     * 
     * @return owning-side field name
     */
    String mappedBy();

    /**
     * Foreign key column in the child table referencing this entity's primary
     * key. Used when the relation is managed via a direct FK without mappedBy.
     * 
     * @return FK column name
     */
    String joinColumn();

    /**
     * Fetch strategy for the collection; defaults to lazy to avoid loading
     * large graphs eagerly.
     * 
     * @return fetch mode
     */
    FetchMode fetch() default FetchMode.LAZY;
}
