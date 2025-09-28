package com.example.persistence_repository.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a persistent entity mapped to a table (or view) in the
 * underlying datastore.
 * <p>
 * The required {@link #tableName()} attribute supplies the physical table name
 * used for DDL generation and SQL statements. It should be the exact name as it
 * exists (or will exist) in the database. Naming strategies or quoting rules
 * should be applied by the framework layer when building SQL.
 * </p>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
    /**
     * Physical name of the table (or view) this entity maps to.
     * 
     * @return table name
     */
    String tableName();

}
