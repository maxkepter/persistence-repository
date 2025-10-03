package com.example.persistence_repository.persistence.entity;

import com.example.persistence_repository.persistence.entity.convert.AttributeConverter;

public class ColumnMeta {
    private String name;
    private String type;
    private boolean nullable;
    private int length;
    private boolean unique;
    private AttributeConverter<?, ?> converter;

    public ColumnMeta(String name, String type, boolean nullable, int length, boolean unique,
                      AttributeConverter<?, ?> converter) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.length = length;
        this.unique = unique;
        this.converter = converter;
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

    public AttributeConverter<?, ?> getConverter() {
        
        return converter;
    }

}
