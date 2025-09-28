package com.example.persistence_repository.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.persistence_repository.persistence.entity.FetchMode;

/**
 * Defines a many-to-one association from the declaring entity to a target
 * entity. The annotated field typically holds a reference to the parent/owner
 * side in a relational model (e.g. many books relate to one author).
 * <p>
 * The {@link #joinColumn()} attribute specifies the foreign key column present
 * in the table of the owning (many) side referencing the primary key of the
 * target entity. Fetch behavior can be controlled via {@link #fetch()} to
 * choose
 * between lazy or eager loading strategies.
 * </p>
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToOne {
    /**
     * Name of the foreign key column in this entity's table referencing the
     * target entity's primary key.
     * 
     * @return FK column name
     */
    String joinColumn();

    /**
     * Fetch strategy for the association (e.g. LAZY or EAGER). Defaults to
     * {@link FetchMode#LAZY}.
     * 
     * @return fetch mode
     */
    FetchMode fetch() default FetchMode.LAZY;
}
