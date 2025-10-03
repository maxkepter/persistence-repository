package com.example.persistence_repository.common.model.enums;

public enum ProductStatus {
    In_stock,
    Exported;

    @Override
    public String toString() {
        // Map enum constant to DB literal with space for "In stock"
        if (this == In_stock) return "In stock";
        return name();
    }
}
