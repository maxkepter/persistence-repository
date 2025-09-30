package com.example.persistence_repository.persistence.cache;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.persistence_repository.persistence.annotation.Key;

/**
 * A simple in-memory cache implementation for entities.
 * <p>
 * This cache uses a ConcurrentHashMap to store entities with their keys.
 * </p>
 * 
 * @Key Entity name + primary key value
 * 
 *      <pre>
 * e.g., "User:1", "Product:42"
 *      </pre>
 * 
 * @Value Entity object
 * 
 * @author Kepter
 * @since 1.0
 */
public class EntityCache implements PersistenceCache<EntityKey, Object> {

    private final int maxSize;
    public static final int DEFAULT_MAX_SIZE = 1000;
    private long expirationTimeMillis = 10 * 60 * 1000;
    private final Map<EntityKey, Object> cache = new ConcurrentHashMap<>();

    public EntityCache(int maxSize, long expirationTimeMillis) {
        this.maxSize = maxSize;
        this.expirationTimeMillis = expirationTimeMillis;
    }

    @Override
    public Object get(EntityKey key) {
        return cache.get(key);
    }

    @Override
    public void put(EntityKey key, Object value) {
        if (cache.size() >= maxSize) {
            // Simple eviction policy: clear the cache when max size is reached
            cache.clear();
        }
        cache.put(key, value);
    }

    @Override
    public boolean contains(EntityKey key) {
        return cache.containsKey(key);
    }

    @Override
    public void remove(EntityKey key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public long getExpirationTimeMillis() {
        return expirationTimeMillis;
    }

    public Map<EntityKey, Object> getCache() {
        return cache;
    }

    public static CacheBuilder builder() {
        return new CacheBuilder();
    }

    public static EntityCache defaultCache() {
        return new EntityCache(DEFAULT_MAX_SIZE, 10 * 60 * 1000);
    }

    @Override
    public void put(Iterable<EntityKey> keys, Iterable<Object> values) {
        while (keys.iterator().hasNext() && values.iterator().hasNext()) {
            put(keys.iterator().next(), values.iterator().next());
        }
    }

    public void put(Iterable<?> entities) {
        for (Object entity : entities) {
            EntityKey key = EntityKey.of(entity);
            if (key != null) {
                put(key, entity);
            }
        }
    }

    public void remove(Iterable<EntityKey> keys) {
        for (EntityKey key : keys) {
            remove(key);
        }
    }

    @Override
    public void clear(Class<?> cls) {
        cache.keySet().removeIf(key -> key.getEntityClass().equals(cls));
    }

}
