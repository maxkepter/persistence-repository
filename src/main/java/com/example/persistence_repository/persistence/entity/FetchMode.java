package com.example.persistence_repository.persistence.entity;

/**
 * Fetch strategy hint for relationship resolution.
 * EAGER indicates the relationship should be resolved immediately at
 * materialization time.
 * LAZY defers resolution until first access (default for performance safety).
 */
public enum FetchMode {
    LAZY,
    EAGER;
}
