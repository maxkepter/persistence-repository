package com.example.persistence_repository.persistence.repository;

import java.sql.SQLException;

import com.example.persistence_repository.persistence.query.clause.ClauseBuilder;

/**
 * Generic CRUD repository interface for managing entities in a database.
 * <p>
 * This interface defines the standard operations for Create, Read, Update,
 * and Delete (CRUD) functionalities on entities of type E with primary key of
 * type K.
 * </p>
 * 
 * @param <E> the type of the entity
 * @param <K> the type of the primary key
 * 
 * @author Kepter
 * @author Nguyen Anh Tu
 * @since 1.0
 * 
 */
public interface CrudRepository<E, K> {

    // Cấm thằng nào yêu cầu làm UpdateWithCondition

    E save(E entity) throws SQLException;

    Iterable<E> saveAll(Iterable<E> entities) throws SQLException;

    E findById(K key);

    E update(E entity) throws SQLException;

    boolean isExist(K key);

    void deleteById(K key) throws SQLException;

    void deleteWithCondition(ClauseBuilder clause) throws SQLException;

    int count();

    Iterable<E> findAll();

    Iterable<E> findWithCondition(ClauseBuilder clause);
}
