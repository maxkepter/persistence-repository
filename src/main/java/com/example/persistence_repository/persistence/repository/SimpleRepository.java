package com.example.persistence_repository.persistence.repository;

public class SimpleRepository<E, K> extends AbstractRepository<E, K> {

    public SimpleRepository(Class<E> cls) {
        super(cls);
    }

}
