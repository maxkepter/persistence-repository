package com.example.persistence_repository.persistence.entity.relation;

/**
 * Enumeration of supported relationship cardinalities between two entity types.
 * <p>
 * Semantics (mirrors common ORM concepts):
 * <ul>
 * <li><b>ONE_TO_ONE</b>: Each side references at most one row on the other
 * side.
 * There is a single foreign key on the owning side (declared via
 * {@code joinColumn}).</li>
 * <li><b>ONE_TO_MANY</b>: The source entity ("one") is referenced by multiple
 * rows of the target entity ("many"). Typically materialized lazily as a
 * {@code LazyList}.</li>
 * <li><b>MANY_TO_ONE</b>: A single-valued reference from the source entity to a
 * parent entity. Back-reference of a ONE_TO_MANY.</li>
 * </ul>
 * </p>
 */
public enum RelationshipType {
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE;
}
