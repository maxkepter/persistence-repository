package com.example.persistence_repository.persistence.cache;

import java.util.Iterator;

public interface PersistenceCache<K, V> {
    V get(K key);

    void put(K key, V value);

    void put(Iterable<K> keys, Iterable<V> values);

    boolean contains(K key);

    void remove(K key);

    void clear();

    void clear(Class<?> cls);

}
