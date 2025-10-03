package com.example.persistence_repository.persistence.entity.load;

import java.util.List;
import java.util.function.Supplier;

/**
 * Factory / helper entry point for constructing lazy wrappers around
 * relationship
 * data. Abstracts instantiation so future cross-cutting concerns (metrics,
 * logging, batch loading) can be injected centrally without changing call
 * sites.
 */
public class RelationLoader {

    public static <T> LazyList<T> lazyCollection(Supplier<List<T>> supplier) {
        return new LazyList<>(supplier);
    }

    /**
     * Creates a single-valued lazy reference wrapper around the supplied resolver.
     * 
     * @param supplier idempotent function producing the target entity (may return
     *                 null)
     * @return new {@link LazyReference}
     */
    public static <T> LazyReference<T> lazyReference(Supplier<T> supplier) {
        return new LazyReference<>(supplier);
    }
}
