package com.example.persistence_repository.repository;

public interface CrudReposistory<E, K> {
    E save(E entity);

    E findById(K key);

    E update(E entity);

    void deleteById(K key);

    Iterable<E> findAll();
}
