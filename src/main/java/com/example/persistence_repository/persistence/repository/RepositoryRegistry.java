package com.example.persistence_repository.persistence.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple static registry for mapping entity classes to their corresponding
 * {@link CrudReposistory} implementations so that relationship materialization
 * can resolve target repositories (used for eager / lazy loaders).
 *
 * NOTE: This is a minimal mechanism; in a real application you might integrate
 * with a DI container instead of a static map.
 */
public final class RepositoryRegistry {

    private static final Map<Class<?>, CrudReposistory<?, ?>> REGISTRY = new ConcurrentHashMap<>();

    private RepositoryRegistry() {
    }

    public static void register(Class<?> entityType, CrudReposistory<?, ?> repository) {
        if (entityType == null || repository == null) {
            throw new IllegalArgumentException("entityType and repository must not be null");
        }
        REGISTRY.put(entityType, repository);
    }

    @SuppressWarnings("unchecked")
    public static <R> CrudReposistory<R, Object> get(Class<R> entityType) {
        return (CrudReposistory<R, Object>) REGISTRY.get(entityType);
    }
}
