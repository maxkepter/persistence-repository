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
import com.example.persistence_repository.persistence.config.DBcontext;
import com.example.persistence_repository.persistence.config.TransactionManager;
import com.example.persistence_repository.persistence.exception.DuplicateKeyException;
import com.example.persistence_repository.persistence.query.common.Page;
import com.example.persistence_repository.persistence.query.common.PageRequest;
import com.example.persistence_repository.persistence.query.crud.DeleteBuilder;
import com.example.persistence_repository.persistence.query.crud.InsertBuilder;
import com.example.persistence_repository.persistence.query.crud.SelectBuilder;
import com.example.persistence_repository.persistence.query.crud.UpdateBuilder;

/**
 * An abstract base class for implementing CRUD (Create, Read, Update, Delete)
 * operations on entities of type {@code E} with primary key of type {@code K}.
 * <p>
 * This class provides common functionality for interacting with a database,
 * including methods for saving, updating, deleting, and finding entities.
 * It uses reflection to map entity fields to database columns and supports
 * pagination through the {@link PageRequest} and {@link Page} classes.
 * </p>
 * 
 * @param <E> the type of entity
 * @param <K> the type of the primary key
 * 
 * @author Kepter
 * @author Nguyen Anh Tu
 * @since 1.0
 * 
 */
public abstract class AbstractReposistory<E, K> implements CrudReposistory<E, K> {

    private Class<E> cls;
    private List<Field> fields;
    private Field keyField;
    private String tableName;

    public AbstractReposistory(Class<E> cls) {
        this.cls = cls;
        this.fields = Arrays.asList(cls.getDeclaredFields());
        this.tableName = cls.getSimpleName();
        this.keyField = getKeyField(fields);
    }

    @Override
    public boolean isExist(K key) {
        Connection connection = DBcontext.getConnection();
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
        Connection connection = DBcontext.getConnection();
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
        Connection connection = DBcontext.getConnection();
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

    /**
     * Retrieves a paginated list of entities from the database based on the
     * provided {@link PageRequest}.
     * <p>
     * This method builds a SQL SELECT query using the {@link SelectBuilder},
     * applies pagination and sorting,
     * executes a count query to determine the total number of records, and then
     * fetches the requested page of results.
     * The results are mapped to a list of entities of type {@code E}.
     * </p>
     *
     * @param request the {@link PageRequest} containing pagination and sorting
     *                information
     * @return a {@link Page} object containing the total number of records, the
     *         page request, and the list of entities
     */
    public Page<E> findAll(PageRequest request) {
        Connection connection = DBcontext.getConnection();
        // Build the base select query
        SelectBuilder builder = SelectBuilder.builder("user")
                .columns(List.of(User.class.getDeclaredFields()).stream().map(f -> f.getName()).toList());
        String query = builder.build(false);

        // Count total records
        int total = countRecord(query, connection);

        int offset = (request.getPageNumber() - 1) * request.getPageSize();
        // Apply pagination and sorting to the original query
        builder.limit(request.getPageSize() > total ? total : request.getPageSize()).offset(offset);
        if (request.getSort() != null && request.getSort().getOrders() != null) {
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
        Connection connection = TransactionManager.getConnection();
        DeleteBuilder builder = DeleteBuilder.builder(tableName).where(keyField.getName() + " = ?", key);
        try (PreparedStatement ps = connection.prepareStatement(builder.build())) {
            setPreparedStatementValue(ps, builder.getParameters());
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public E findById(K key) {
        Connection connection = TransactionManager.getConnection();
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
        Connection connection = TransactionManager.getConnection();
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
        Connection connection = TransactionManager.getConnection();
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
            builder.where(keyField.getName() + " = ?", keyValue);

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

    /**
     * Counts the total number of records for the given SQL query.
     * <p>
     * This method wraps the provided SQL query in a COUNT query to determine the
     * total number of records that would be returned by the original query.
     * It executes the count query and retrieves the total count from the result
     * set.
     * </p>
     *
     * @param query      the base SQL query to count records for
     * @param connection the database connection to use for executing the query
     * @return the total number of records for the given query
     */
    protected int countRecord(String query, Connection connection) {
        // Wrap the base query in a count query to get total number of records
        String countQuery = "SELECT COUNT(1) AS total FROM (" + query + ") AS count_table";
        int total = 0;
        // Count total records
        try (PreparedStatement countPs = connection.prepareStatement(countQuery);
                ResultSet countRs = countPs.executeQuery()) {
            if (countRs.next()) {
                total = countRs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Maps the current row of the given {@link ResultSet} to an instance of the
     * specified class.
     * <p>
     * This method uses reflection to create a new instance of the specified class
     * and populates its fields with values from the current row of the
     * {@link ResultSet}.
     * It matches column names in the result set with field names in the class.
     * </p>
     *
     * @param rs  the {@link ResultSet} positioned at the row to map
     * @param cls the class to map the result set row to
     * @return an instance of the specified class populated with values from the
     *         result set row
     * @throws Exception if an error occurs during reflection or SQL operations
     */
    protected E mapResultSet(ResultSet rs, Class<E> cls) throws Exception {
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

    /**
     * Maps all rows of the given {@link ResultSet} to a list of instances of the
     * specified class.
     * <p>
     * This method iterates over the rows of the {@link ResultSet}, using the
     * {@link #mapResultSet(ResultSet, Class)} method to map each row to an instance
     * of the specified class.
     * The resulting instances are collected into a list and returned.
     * </p>
     *
     * @param rs  the {@link ResultSet} containing the rows to map
     * @param cls the class to map each result set row to
     * @return a list of instances of the specified class populated with values from
     *         the result set rows
     * @throws Exception if an error occurs during reflection or SQL operations
     */
    protected List<E> mapListResultSet(ResultSet rs, Class<E> cls) throws Exception {
        List<E> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapResultSet(rs, cls));
        }
        return result;
    }

    /**
     * Sets the values of the given {@link PreparedStatement} using the provided
     * list of parameters.
     * <p>
     * This method iterates over the list of parameters and sets each value in the
     * {@link PreparedStatement}
     * at the corresponding index (1-based).
     * </p>
     *
     * @param ps      the {@link PreparedStatement} to set values for
     * @param objects the list of parameter values to set in the prepared statement
     * @throws SQLException if an error occurs while setting the parameter values
     */
    private void setPreparedStatementValue(PreparedStatement ps, List<Object> objects) throws SQLException {
        int idx = 1;
        for (Object p : objects) {
            ps.setObject(idx++, p);
        }
    }

    /**
     * Retrieves the field annotated with {@link Key} from the given list of
     * fields.
     * <p>
     * This method iterates over the provided list of fields and checks for the
     * presence of the {@link Key} annotation.
     * If multiple fields are found with the {@link Key} annotation, a
     * {@link DuplicateKeyException} is thrown.
     * If no field is found with the {@link Key} annotation, this method returns
     * {@code null}.
     * </p>
     *
     * @param fields the list of fields to search for the key field
     * @return the field annotated with {@link Key}, or {@code null} if none is
     *         found
     * @throws DuplicateKeyException if multiple fields are found with the
     *                               {@link Key} annotation
     */
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
        if (keyField == null) {
            throw new IllegalStateException("No field annotated with @Key found in class " + cls.getName());

        }
        return keyField;
    }

}
