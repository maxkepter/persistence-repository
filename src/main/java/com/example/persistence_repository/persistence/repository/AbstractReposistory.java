package com.example.persistence_repository.persistence.repository;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.persistence_repository.entity.User;
import com.example.persistence_repository.persistence.annotation.Key;
import com.example.persistence_repository.persistence.config.RepositoryConfig;
import com.example.persistence_repository.persistence.exception.DuplicateKeyException;
import com.example.persistence_repository.persistence.query.common.Page;
import com.example.persistence_repository.persistence.query.common.PageRequest;
import com.example.persistence_repository.persistence.query.crud.DeleteBuilder;
import com.example.persistence_repository.persistence.query.crud.InsertBuilder;
import com.example.persistence_repository.persistence.query.crud.SelectBuilder;
import com.example.persistence_repository.persistence.query.crud.UpdateBuilder;

public abstract class AbstractReposistory<E, K> implements CrudReposistory<E, K> {

    private Connection connection;
    private Class<E> cls;
    private List<Field> fields;
    private Field keyField;
    private String tableName;

    public AbstractReposistory(Class<E> cls) {
        this.connection = RepositoryConfig.getConnection();
        this.cls = cls;
        this.fields = Arrays.asList(cls.getDeclaredFields());
        this.tableName = cls.getSimpleName();
        this.keyField = getKeyField(fields);
    }

    /**
     * Creates a MySQL table for the entity class using reflection.
     */
    public void createTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        List<String> columns = new ArrayList<>();
        String primaryKey = null;
        for (Field field : fields) {
            String name = field.getName();
            String type;
            if (field.getType() == int.class || field.getType() == Integer.class) {
                type = "INT";
            } else if (field.getType() == long.class || field.getType() == Long.class) {
                type = "BIGINT";
            } else if (field.getType() == String.class) {
                type = "VARCHAR(255)";
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                type = "BOOLEAN";
            } else {
                type = "VARCHAR(255)"; // default fallback
            }
            String colDef = name + " " + type;
            if (field.isAnnotationPresent(Key.class)) {
                colDef += " PRIMARY KEY";
                primaryKey = name;
            }
            columns.add(colDef);
        }
        sb.append(String.join(", ", columns));
        sb.append(")");
        String sql = sb.toString();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isExist(K key) {
        SelectBuilder builder = SelectBuilder.builder(tableName)
                .columns(List.of("1"))
                .where(keyField.getName() + " = ?", key)
                .limit(1);
        boolean exists = false;
        try (PreparedStatement preparedSt = connection.prepareStatement(builder.build());
                ResultSet rs = preparedSt.executeQuery()) {
            if (rs.next()) {
                exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }

    @Override
    public int count() {
        SelectBuilder builder = SelectBuilder.builder(tableName)
                .columns(List.of("COUNT(*) AS total"));
        int count = 0;
        try (PreparedStatement preparedSt = connection.prepareStatement(builder.build());
                ResultSet rs = preparedSt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public Iterable<E> findAll() {
        SelectBuilder builder = SelectBuilder.builder(tableName)
                .columns(fields.stream().map(f -> f.getName()).toList());
        List<E> result = null;
        try (PreparedStatement preparedSt = connection.prepareStatement(builder.build());
                ResultSet rs = preparedSt.executeQuery()) {
            result = mapListResultSet(rs, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Page<E> findAll(PageRequest request) {
        SelectBuilder builder = SelectBuilder.builder("user")
                .columns(List.of(User.class.getDeclaredFields()).stream().map(f -> f.getName()).toList());
        String query = builder.build(false);
        String countQuery = "SELECT COUNT(1) AS total FROM (" + query + ") AS count_table";
        int total = 0;
        try (PreparedStatement countPs = connection.prepareStatement(countQuery);
                ResultSet countRs = countPs.executeQuery()) {
            if (countRs.next()) {
                total = countRs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int offset = (request.getPageNumber() - 1) * request.getPageSize();

        builder.limit(request.getPageSize() > total ? total : request.getPageSize()).offset(offset);
        if (request.getSort() != null && request.getSort().getOrders() != null
                && !request.getSort().getOrders().isEmpty()) {
            builder.orderBy(request.getSort().getOrders());
        }

        List<E> result = null;
        try (PreparedStatement ps = connection.prepareStatement(builder.build());
                ResultSet rs = ps.executeQuery()) {
            result = mapListResultSet(rs, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Page<>(total, request, result);
    }

    @Override
    public void deleteById(K key) {
        DeleteBuilder builder = DeleteBuilder.builder(tableName).where(keyField.getName() + " = ?", key);
        System.out.println(builder.build());
        try (PreparedStatement ps = connection.prepareStatement(builder.build())) {
            setPreparedStatementValue(ps, builder.getParameters());
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public E findById(K key) {
        SelectBuilder builder = SelectBuilder.builder(tableName).where(keyField.getName() + " = ?", key);
        E entity = null;
        try (PreparedStatement ps = connection.prepareStatement(builder.build());) {
            setPreparedStatementValue(ps, builder.getParameters());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    entity = mapResultSet(rs, cls);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    @Override
    public E save(E entity) {
        try {
            List<Object> values = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();
            for (Field field : fields) {
                field.setAccessible(true);
                columnNames.add(field.getName());
                values.add(field.get(entity));
            }
            InsertBuilder builder = InsertBuilder.builder(tableName)
                    .columns(columnNames)
                    .values(values);
            try (PreparedStatement ps = connection.prepareStatement(builder.build())) {
                setPreparedStatementValue(ps, builder.getParameters());
                ps.executeUpdate();
            }
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public E update(E entity) {
        try {
            UpdateBuilder builder = UpdateBuilder.builder(tableName);
            Object keyValue = null;
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (field.getName().equals(keyField.getName())) {
                    keyValue = value;
                    continue;
                }
                builder.set(field.getName(), value);
            }
            builder.where(" WHERE " + keyField.getName() + " = ?", keyValue);

            try (PreparedStatement ps = connection.prepareStatement(builder.build())) {
                setPreparedStatementValue(ps, builder.getParameters());
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("No rows updated, entity may not exist.");
                }
            }
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public E mapResultSet(ResultSet rs, Class<E> cls) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        E obj = cls.getDeclaredConstructor().newInstance();
        for (int i = 1; i <= colCount; i++) {
            String colName = meta.getColumnLabel(i);
            Object value = rs.getObject(i);

            try {
                Field field = cls.getDeclaredField(colName);
                field.setAccessible(true);
                field.set(obj, value);
            } catch (NoSuchFieldException ignored) {
                // ignore if entity has no corresponding field
            }
        }
        return obj;
    }

    public List<E> mapListResultSet(ResultSet rs, Class<E> cls) throws Exception {
        List<E> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapResultSet(rs, cls));
        }
        return result;
    }

    private void setPreparedStatementValue(PreparedStatement ps, List<Object> objects) throws SQLException {
        int idx = 1;
        for (Object p : objects) {
            ps.setObject(idx++, p);
        }
    }

    private Field getKeyField(List<Field> fields) {
        Field keyField = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Key.class)) {
                if (keyField != null) {
                    throw new DuplicateKeyException();
                }
                keyField = field;
            }
        }
        return keyField;
    }

}
