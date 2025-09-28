package com.example.persistence_repository.persistence.repository;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.config.DBcontext;
import com.example.persistence_repository.persistence.config.TransactionManager;
import com.example.persistence_repository.persistence.entity.EntityMeta;

import com.example.persistence_repository.persistence.entity.FetchMode;
import com.example.persistence_repository.persistence.entity.ColumnMeta;
import com.example.persistence_repository.persistence.entity.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.RelationshipMeta;
import com.example.persistence_repository.persistence.entity.LazyList;
import com.example.persistence_repository.persistence.query.clause.ClauseBuilder;
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

    private final Class<E> cls;
    private final EntityMeta<E> entityMeta;
    /**
     * Persistent (scalar) fields only (those annotated with @Column) used for DML.
     */
    private final List<Field> persistentFields;
    private final Field keyField;

    public AbstractReposistory(Class<E> cls) {
        this.cls = cls;
        // Build metadata once
        this.entityMeta = EntityMeta.scanAnnotation(cls);
        this.keyField = entityMeta.getKeyField();
        // derive persistent (scalar) fields from column map to avoid including
        // relationship fields
        List<Field> tmp = new ArrayList<>();
        for (Field f : entityMeta.getFields()) {
            if (f.isAnnotationPresent(Column.class)) {
                f.setAccessible(true);
                tmp.add(f);
            }
        }
        this.persistentFields = List.copyOf(tmp);
    }

    @Override
    public boolean isExist(K key) {
        Connection connection = DBcontext.getConnection();
        SelectBuilder<E> builder = SelectBuilder.builder(entityMeta)
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
        SelectBuilder<E> builder = SelectBuilder.builder(entityMeta)
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
        SelectBuilder<E> builder = SelectBuilder.builder(entityMeta)
                .columns(persistentFields.stream().map(Field::getName).toList());
        List<E> result = null;
        try (PreparedStatement preparedSt = connection.prepareStatement(builder.build());
                ResultSet rs = preparedSt.executeQuery()) {
            result = mapListResultSet(rs, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Iterable<E> findWithCondition(ClauseBuilder clause) {
        Connection connection = DBcontext.getConnection();
        SelectBuilder<E> builder = SelectBuilder.builder(entityMeta)
                .columns(persistentFields.stream().map(Field::getName).toList())
                .where(clause.build());
        builder.setParameters(clause.getParameters());
        System.out.println(builder.getParameters());
        List<E> result = null;
        try (PreparedStatement preparedSt = connection.prepareStatement(builder.build());) {
            setPreparedStatementValue(preparedSt, builder.getParameters());
            try (ResultSet rs = preparedSt.executeQuery()) {
                result = mapListResultSet(rs, cls);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    public Page<E> findWithCondition(ClauseBuilder clause, PageRequest request) {
        Connection connection = DBcontext.getConnection();
        // Build the base select query
        SelectBuilder<E> builder = SelectBuilder.builder(entityMeta)
                .columns(persistentFields.stream().map(Field::getName).toList())
                .where(clause.build());
        builder.setParameters(clause.getParameters());
        String query = builder.build(false);

        // Count total records
        int total = countRecord(query, connection, builder.getParameters());

        // Calculate offset for pagination
        int offset = (request.getPageNumber() - 1) * request.getPageSize();
        // Apply pagination and sorting to the original query
        builder.limit(request.getPageSize() > total ? total : request.getPageSize()).offset(offset);
        if (request.getSort() != null && request.getSort().getOrders() != null) {
            builder.orderBy(request.getSort().getOrders());
        }

        List<E> result = null;
        try (PreparedStatement ps = connection.prepareStatement(builder.build());) {
            setPreparedStatementValue(ps, builder.getParameters());
            try (ResultSet rs = ps.executeQuery()) {
                result = mapListResultSet(rs, cls);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Page<>(total, request, result);
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
        SelectBuilder<E> builder = SelectBuilder.builder(entityMeta)
                .columns(persistentFields.stream().map(Field::getName).toList());
        String query = builder.build(false);

        // Count total records
        int total = countRecord(query, connection, null);

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
        DeleteBuilder<E> builder = DeleteBuilder.builder(entityMeta).where(keyField.getName() + " = ?", key);
        try (PreparedStatement ps = connection.prepareStatement(builder.build())) {
            setPreparedStatementValue(ps, builder.getParameters());
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteWithCondition(ClauseBuilder clause) {
        Connection connection = TransactionManager.getConnection();
        DeleteBuilder<E> builder = DeleteBuilder.builder(entityMeta).where(clause.build());
        builder.setParameters(clause.getParameters());
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
        SelectBuilder<E> builder = SelectBuilder.builder(entityMeta).where(keyField.getName() + " = ?", key);
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
            if (keyField == null) {
                throw new IllegalStateException("Entity không có khóa chính @Key");
            }
            keyField.setAccessible(true);
            Object keyVal = keyField.get(entity);
            if (keyVal == null) {
                throw new IllegalArgumentException("Khóa chính phải được set thủ công (no AUTO_INCREMENT)");
            }
            List<Object> values = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();
            for (Field field : persistentFields) {
                field.setAccessible(true);
                columnNames.add(field.getName());
                values.add(field.get(entity));
            }
            InsertBuilder<E> builder = InsertBuilder.builder(entityMeta)
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
            UpdateBuilder<E> builder = UpdateBuilder.builder(entityMeta);
            Object keyValue = null;
            for (Field field : persistentFields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (field.getName().equals(keyField.getName())) {
                    keyValue = value;
                    continue;
                }
                builder.set(field.getName(), value);
            }
            if (keyValue == null) {
                throw new IllegalArgumentException("Khóa chính null khi update: cần set trước khi gọi update()");
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
     * @param parameters the list of parameters to set in the prepared statement
     * @return the total number of records for the given query
     */
    public int countRecord(String query, Connection connection, List<Object> parameters) {
        // Wrap the base query in a count query to get total number of records
        String countQuery = "SELECT COUNT(1) AS total FROM (" + query + ") AS count_table";
        int total = 0;
        // Count total records
        try (PreparedStatement countPs = connection.prepareStatement(countQuery);) {
            setPreparedStatementValue(countPs, parameters);
            try (ResultSet countRs = countPs.executeQuery()) {
                if (countRs.next()) {
                    total = countRs.getInt("total");
                }
            } catch (Exception e) {
                e.printStackTrace();
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

        // Tạo map tên cột (lowercase) -> index để tìm nhanh
        Map<String, Integer> labelIndex = new HashMap<>();
        for (int i = 1; i <= colCount; i++) {
            labelIndex.put(meta.getColumnLabel(i).toLowerCase(), i);
        }

        // Duyệt các field có @Column trong metadata
        for (Map.Entry<String, ColumnMeta> entry : entityMeta.getFieldToColumnMap().entrySet()) {
            String fieldName = entry.getKey(); // tên field
            ColumnMeta colMeta = entry.getValue();
            String physicalCol = colMeta.getName();
            Integer idx = labelIndex.get(physicalCol.toLowerCase());
            if (idx == null) {
                continue; // cột không có trong result set
            }
            Object value = rs.getObject(idx);
            try {
                Field field = cls.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
            } catch (NoSuchFieldException ignored) {
            }
        }
        // Attach relationship placeholders (lazy / eager) - simple eager hook
        // placeholder.
        // NOTE: Actual loading logic (e.g., repository lookup, lazy wrappers) not yet
        // implemented here.
        // Future: Iterate entityMeta.getRelationships() and, based on FetchMode, load
        // or assign lazy wrapper.
        // Relationship wiring (prototype implementation):
        for (RelationshipMeta rel : entityMeta.getRelationships()) {
            Field f = rel.getField();
            // Only handle single-valued for now; collections later
            try {
                if (rel.isCollection()) {
                    // Collection side: assign LazyList placeholder (no immediate fetch even if
                    // EAGER until batch logic exists)
                    LazyList<?> list = new LazyList<>(List::of); // empty placeholder supplier
                    f.set(obj, list);
                    continue;
                }
                // Single-valued relation
                if (rel.getFetchMode() == FetchMode.EAGER) {
                    // Attempt eager load via repository lookup if available
                    var repo = resolveRepository(rel.getTargetType());
                    if (repo != null) {
                        // Assume FK column stored on this row as rel.getJoinColumn()
                        Object fkValue = null;
                        if (rel.getJoinColumn() != null && !rel.getJoinColumn().isBlank()) {
                            // attempt to read column by name (case-insensitive fallback loop)
                            Integer fkIdx = labelIndex.get(rel.getJoinColumn().toLowerCase());
                            if (fkIdx != null) {
                                fkValue = rs.getObject(fkIdx);
                            }
                        }
                        Object related = fkValue == null ? null : repo.findById(fkValue);
                        f.set(obj, related);
                    } else {
                        // repository not available -> fallback to lazy reference to allow later
                        // resolution
                        LazyReference<?> ref = new LazyReference<>(() -> {
                            var r = resolveRepository(rel.getTargetType());
                            if (r == null)
                                return null;
                            Object fk = null;
                            try {
                                if (rel.getJoinColumn() != null) {
                                    fk = rs.getObject(rel.getJoinColumn());
                                }
                            } catch (SQLException e) {
                                return null;
                            }
                            return fk == null ? null : r.findById(fk);
                        });
                        // pre-load because fetchMode = EAGER but repo absent
                        ref.forceLoad();
                        f.set(obj, ref.get());
                    }
                } else { // LAZY
                    LazyReference<?> ref = new LazyReference<>(() -> {
                        var r = resolveRepository(rel.getTargetType());
                        if (r == null)
                            return null;
                        Object fk = null;
                        if (rel.getJoinColumn() != null) {
                            Integer fkIdx = labelIndex.get(rel.getJoinColumn().toLowerCase());
                            if (fkIdx != null) {
                                try {
                                    fk = rs.getObject(fkIdx);
                                } catch (SQLException ignored) {
                                }
                            }
                        }
                        return fk == null ? null : r.findById(fk);
                    });
                    f.set(obj, ref);
                }
            } catch (IllegalAccessException iae) {
                // ignore assignment errors for now
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
        if (objects == null || objects.isEmpty()) {
            return;
        }
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
    /**
     * Exposes entity metadata in subclasses if advanced queries (joins, eager fetch
     * planning) are needed.
     */
    protected EntityMeta<E> getEntityMeta() {
        return entityMeta;
    }

    /**
     * Hook for future repository lookup for eager relation loading.
     * Subclasses can override and provide a registry.
     */
    protected <R> CrudReposistory<R, Object> resolveRepository(Class<R> targetType) {
        return null; // placeholder
    }

}
