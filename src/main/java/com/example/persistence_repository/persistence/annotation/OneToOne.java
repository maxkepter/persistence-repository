package com.example.persistence_repository.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.persistence_repository.persistence.entity.FetchMode;

/**
 * Defines a one-to-one association between two entities. At most one instance
 * of each entity is associated with the other.
 * <p>
 * One side is considered the owning side defining the foreign key (specified
 * via {@link #joinColumn()}); the other side, if bidirectional, references the
 * owning field via {@link #mappedBy()} and does not define a separate foreign
 * key. Fetch behavior defaults to {@link FetchMode#LAZY} but may be overridden.
 * </p>
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {
    /**
     * Field name on the owning entity when this side is the inverse (non-owning)
     * side. Empty if this side owns the relationship.
     * 
     * @return owning field name or empty
     */
    String mappedBy();

    /**
     * Foreign key column for the relationship when this side is the owning side.
     * Ignored if {@link #mappedBy()} is non-empty.
     * 
     * @return FK column name
     */
    String joinColumn();

    /**
     * Fetch strategy; defaults to lazy to defer loading until first access.
     * 
     * @return fetch mode
     */
    FetchMode fetch() default FetchMode.LAZY;
}
