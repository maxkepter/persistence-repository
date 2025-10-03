package com.example.persistence_repository.persistence.entity.convert;

public interface AttributeConverter<C, F> {
    C convertToDatabaseColumn(F attribute);

    F convertToEntityAttribute(C dbData);
}
