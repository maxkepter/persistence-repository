package com.example.persistence_reposistory.reposistory;

public interface ListCrudReposistory<E, K> extends CrudReposistory<E, K> {
    Iterable<E> findAll();

}
