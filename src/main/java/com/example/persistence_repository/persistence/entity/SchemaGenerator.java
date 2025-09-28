package com.example.persistence_repository.persistence.entity;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.persistence_repository.persistence.config.TransactionManager;
import com.example.persistence_repository.persistence.entity.relation.RelationshipType;

/**
 * Sinh DDL (DROP/CREATE TABLE) dựa trên metadata đã đăng ký trong
 * {@link EntityRegistry}.
 * Hỗ trợ:
 * - Topological sort theo quan hệ MANY_TO_ONE để tạo bảng theo thứ tự phụ
 * thuộc.
 * - Sinh FOREIGN KEY (ON DELETE CASCADE ON UPDATE CASCADE) cho cột joinColumn
 * có trong entity nguồn.
 * - Tuỳ chọn drop trước khi create.
 */
public class SchemaGenerator {

    public static class Options {
        public boolean dropIfExists = true;
        public boolean printDdl = true;
        public String fkOnDelete = "CASCADE"; // có thể đổi thành RESTRICT nếu muốn
        public String fkOnUpdate = "CASCADE";
    }

    private final Options options;

    public SchemaGenerator(Options options) {
        this.options = options;
    }

    public static SchemaGenerator withDefault() {
        return new SchemaGenerator(new Options());
    }

    public void generateAll() throws Exception {
        if (EntityRegistry.isEmpty()) {
            throw new IllegalStateException("These is no entity registered in EntityRegistry");
        }
        List<EntityMeta<?>> ordered = topologicalSort(EntityRegistry.getAllMetas());
        Connection conn = TransactionManager.getConnection();
        try (Statement st = conn.createStatement()) {
            if (options.dropIfExists) {
                // Drop ngược thứ tự tạo: phụ thuộc trước
                for (int i = ordered.size() - 1; i >= 0; i--) {
                    String table = ordered.get(i).getTableName();
                    String sql = "DROP TABLE IF EXISTS " + table;
                    if (options.printDdl)
                        System.out.println(sql + ";");
                    st.executeUpdate(sql);
                }
            }
            for (EntityMeta<?> meta : ordered) {
                String create = buildCreateTable(meta);
                if (options.printDdl)
                    System.out.println(create + ";");
                st.executeUpdate(create);
            }
        }
    }

    private String buildCreateTable(EntityMeta<?> meta) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(meta.getTableName()).append(" (");
        List<String> columnDefs = new ArrayList<>();
        // Columns
        meta.getFieldToColumnMap().forEach((fieldName, colMeta) -> {
            String col = colMeta.getName();
            String type = sqlType(colMeta);
            StringBuilder def = new StringBuilder(col).append(" ").append(type);
            if (!colMeta.isNullable())
                def.append(" NOT NULL");
            if (colMeta.isUnique())
                def.append(" UNIQUE");
            columnDefs.add(def.toString());
        });
        // Primary key
        if (meta.getKeyField() != null) {
            String pkCol = meta.getFieldToColumnMap().get(meta.getKeyField().getName()).getName();
            columnDefs.add("PRIMARY KEY (" + pkCol + ")");
        }
        // Foreign keys từ quan hệ MANY_TO_ONE: joinColumn phải là cột đã có trong map
        meta.getRelationships().stream()
                .filter(r -> r.getType() == RelationshipType.MANY_TO_ONE && r.getJoinColumn() != null
                        && !r.getJoinColumn().isBlank())
                .forEach(r -> {
                    @SuppressWarnings("unchecked")
                    EntityMeta<Object> target = (EntityMeta<Object>) EntityRegistry
                            .getMeta((Class<Object>) r.getTargetType());
                    if (target != null && target.getKeyField() != null) {
                        String targetPk = target.getFieldToColumnMap().get(target.getKeyField().getName()).getName();
                        String fkName = "fk_" + meta.getTableName().toLowerCase() + "_"
                                + r.getJoinColumn().toLowerCase();
                        String fk = String.format(
                                "CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE %s ON UPDATE %s",
                                fkName, r.getJoinColumn(), target.getTableName(), targetPk, options.fkOnDelete,
                                options.fkOnUpdate);
                        columnDefs.add(fk);
                    }
                });
        sb.append(String.join(", ", columnDefs));
        sb.append(")");
        return sb.toString();
    }

    private String sqlType(ColumnMeta c) {
        // Nhanh gọn: nếu type = "VARCHAR" thì thêm (length); nếu INT thì giữ nguyên;
        // cho phép người dùng tự set type tùy ý.
        String t = c.getType().toUpperCase();
        if (t.startsWith("VARCHAR")) {
            return "VARCHAR(" + c.getLength() + ")";
        }
        return t; // các kiểu khác assume đã chuẩn
    }

    private List<EntityMeta<?>> topologicalSort(java.util.Collection<EntityMeta<?>> metas) {
        // Xây đồ thị: edge A->B nếu A (nguồn) MANY_TO_ONE tới B (đích) => B phải tạo
        // trước A
        Map<EntityMeta<?>, Set<EntityMeta<?>>> adj = new HashMap<>();
        Map<EntityMeta<?>, Integer> indeg = new HashMap<>();
        for (EntityMeta<?> m : metas) {
            adj.putIfAbsent(m, new HashSet<>());
            indeg.putIfAbsent(m, 0);
        }
        for (EntityMeta<?> m : metas) {
            m.getRelationships().stream()
                    .filter(r -> r.getType() == RelationshipType.MANY_TO_ONE)
                    .forEach(r -> {
                        @SuppressWarnings("unchecked")
                        EntityMeta<Object> target = (EntityMeta<Object>) EntityRegistry
                                .getMeta((Class<Object>) r.getTargetType());
                        if (target != null && target != m) {
                            // edge target -> m
                            adj.get(target).add(m);
                            indeg.put(m, indeg.getOrDefault(m, 0) + 1);
                        }
                    });
        }
        Deque<EntityMeta<?>> dq = new ArrayDeque<>();
        indeg.forEach((k, v) -> {
            if (v == 0)
                dq.add(k);
        });
        List<EntityMeta<?>> order = new ArrayList<>();
        while (!dq.isEmpty()) {
            EntityMeta<?> x = dq.removeFirst();
            order.add(x);
            for (EntityMeta<?> nb : adj.getOrDefault(x, Set.of())) {
                indeg.put(nb, indeg.get(nb) - 1);
                if (indeg.get(nb) == 0)
                    dq.add(nb);
            }
        }
        if (order.size() != metas.size()) {
            String cycle = metas.stream().map(m -> m.getTableName()).collect(Collectors.joining(","));
            throw new IllegalStateException("Phát hiện vòng phụ thuộc (cycle) trong quan hệ: " + cycle);
        }
        return order;
    }
}
