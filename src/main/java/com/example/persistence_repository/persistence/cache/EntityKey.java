package com.example.persistence_repository.persistence.cache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.persistence_repository.persistence.annotation.Key;

public final class EntityKey {
    private final Class<?> entityClass;
    private final Object id;

    public EntityKey(Class<?> entityClass, Object id) {
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass must not be null");
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public static EntityKey of(Class<?> clazz, Object id) {
        return new EntityKey(clazz, id);
    }

    public static EntityKey of(Object entity) {
        try {
            Class<?> clazz = entity.getClass();
            Field idField = null;
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Key.class)) {
                    idField = field;
                    break;
                }
            }
            if (idField == null) {
                throw new IllegalArgumentException("No field annotated with @Key found in class " + clazz.getName());
            }
            idField.setAccessible(true);
            return new EntityKey(clazz, idField.get(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Iterable<EntityKey> of(Iterable<?> entities) {
        List<EntityKey> entityKeys = new ArrayList<>();
        for (Object entity : entities) {
            EntityKey key = of(entity);
            if (key != null) {
                entityKeys.add(key);
            }
        }
        return entityKeys;

    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Object getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EntityKey other = (EntityKey) o;
        return entityClass.equals(other.entityClass) &&
                id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityClass, id);
    }

    @Override
    public String toString() {
        return entityClass.getName() + ":" + id;
    }
}
