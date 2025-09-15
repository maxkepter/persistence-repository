package com.example.persistence_reposistory.reposistory;

public interface CrudReposistory<E, K> {
    E save(E entity);

    E findById(K key);

    E update(E entity);

    void deleteById(K key);
}
