package com.example.persistence_repository.persistence.repository;

public interface CrudReposistory<E, K> {
    E save(E entity);

    E findById(K key);

    E merge(E entity);

    void deleteById(K key);

    Iterable<E> findAll();
}
