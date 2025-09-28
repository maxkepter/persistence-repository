package com.example.persistence_repository.persistence.exception;

/**
 * Exception thrown when a duplicate key is encountered in the database.
 * <p>
 * This exception indicates that an attempt was made to insert or update a
 * record
 * with a key that already exists in the database, violating unique constraints.
 * </p>
 * 
 * @author Kepter
 * @author Nguyen Anh Tu
 * @since 1.0
 * 
 */
public class DuplicateKeyException extends RuntimeException {
    public DuplicateKeyException() {
        super("Duplicate key found.");
    }

    public DuplicateKeyException(String message) {
        super(message);
    }
}
