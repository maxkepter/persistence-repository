package com.example.persistence_repository.persistence.repository;

public interface CrudReposistory<E, K> {
    E save(E entity);

    E findById(K key);

    E update(E entity);

    boolean isExist(K key);

    void deleteById(K key);

    int count();

    Iterable<E> findAll();
}
