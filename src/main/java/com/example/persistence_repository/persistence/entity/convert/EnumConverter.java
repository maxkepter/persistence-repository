package com.example.persistence_repository.persistence.entity.convert;

public class EnumConverter<E extends Enum> implements AttributeConverter<String, Enum<?>> {
    private final Class<E> enumType;

    public EnumConverter(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public String convertToDatabaseColumn(Enum<?> attribute) {

        return attribute == null ? null : attribute.name();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enum<?> convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Enum.valueOf(enumType, dbData);
    }

    public Class<E> getEnumType() {
        return enumType;
    }

}
