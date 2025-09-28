package com.example.persistence_repository.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a persistent field is mapped to a database column and specifies
 * column-level metadata used during DDL generation and SQL binding.
 * <p>
 * All attributes are optional except where noted. If {@link #name()} is left
 * empty the framework should derive a column name from the field (typically the
 * field's name using a naming strategy). The {@link #type()} attribute
 * expresses
 * the SQL column type. If unspecified a sensible default (often VARCHAR) is
 * assumed based on the Java field type.
 * </p>
 * <p>
 * Length applies only to character / binary varying types. The
 * {@link #nullable()}
 * flag controls whether a NOT NULL constraint is emitted. {@link #unique()}
 * indicates a single-column uniqueness constraint; composite unique constraints
 * (if supported) should be handled elsewhere (e.g. at table definition level).
 * </p>
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * Explicit column name. When empty a name is inferred from the field.
     * 
     * @return the column name or empty for inferred
     */
    String name() default "";

    /**
     * Database column type (DDL fragment) such as VARCHAR, INT, DATE, etc.
     * 
     * @return SQL type keyword
     */
    String type() default "VARCHAR";

    /**
     * Whether the column allows NULL values (true = nullable, false = NOT NULL).
     * 
     * @return true if nullable
     */
    boolean nullable() default true;

    /**
     * Length / size for variable length types (e.g. VARCHAR). Ignored for types
     * where length is not applicable.
     * 
     * @return maximum length
     */
    int length() default 255;

    /**
     * Whether a UNIQUE constraint should be declared for this single column.
     * 
     * @return true if unique constraint should be applied
     */
    boolean unique() default false;
}
