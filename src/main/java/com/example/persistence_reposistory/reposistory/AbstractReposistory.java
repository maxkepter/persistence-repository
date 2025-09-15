package com.example.persistence_reposistory.reposistory;

import java.sql.Connection;
import java.util.List;

public abstract class AbstractReposistory<E, K> implements ListCrudReposistory<E, K> {
    private Connection connection;

    public AbstractReposistory(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Iterable<E> findAll() {

        return null;
    }

    @Override
    public void deleteById(K key) {
        // TODO Auto-generated method stub

    }

    @Override
    public E findById(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E save(E entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public E update(E entity) {
        // TODO Auto-generated method stub
        return null;
    }

}
