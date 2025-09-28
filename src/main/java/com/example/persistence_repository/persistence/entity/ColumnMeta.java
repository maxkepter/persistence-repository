package com.example.persistence_repository.persistence.entity;

public class ColumnMeta {
    private String name;
    private String type;
    private boolean nullable;
    private int length;
    private boolean unique;

    public ColumnMeta(String name, String type, boolean nullable, int length, boolean unique) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.length = length;
        this.unique = unique;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public int getLength() {
        return length;
    }

    public boolean isUnique() {
        return unique;
    }

}
