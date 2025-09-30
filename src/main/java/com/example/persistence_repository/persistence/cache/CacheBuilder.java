package com.example.persistence_repository.persistence.cache;

public class CacheBuilder {
    private int maxSize = EntityCache.DEFAULT_MAX_SIZE;
    private long expirationTimeMillis = 10 * 60 * 1000; // Default 10 minutes

    public CacheBuilder setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public CacheBuilder setExpirationTimeMillis(long expirationTimeMillis) {
        this.expirationTimeMillis = expirationTimeMillis;
        return this;
    }

    public EntityCache build() {
        return new EntityCache(maxSize, expirationTimeMillis);
    }
}
