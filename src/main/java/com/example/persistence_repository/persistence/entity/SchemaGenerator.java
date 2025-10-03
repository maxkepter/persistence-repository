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
        List<EntityMeta<?>> metas = new ArrayList<>(EntityRegistry.getAllMetas());
        TopoResult topo = topologicalSortOrDetectCycle(metas);
        Connection conn = TransactionManager.getConnection();
        try (Statement st = conn.createStatement()) {
            List<EntityMeta<?>> creationOrder = topo.hasCycle ? metas : topo.ordered; // if cycle just use original
                                                                                      // order

            if (options.dropIfExists) {
                // Disable foreign key checks to allow dropping tables in any order
                st.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                if (options.printDdl)
                    System.out.println("SET FOREIGN_KEY_CHECKS = 0;");

                // Drop in reverse order
                for (int i = creationOrder.size() - 1; i >= 0; i--) {
                    String table = creationOrder.get(i).getTableName();
                    String sql = "DROP TABLE IF EXISTS " + table;
                    if (options.printDdl)
                        System.out.println(sql + ";");
                    st.executeUpdate(sql);
                }

                // Re-enable foreign key checks
                st.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
                if (options.printDdl)
                    System.out.println("SET FOREIGN_KEY_CHECKS = 1;");
            }

            // Phase 1: create tables (if cycle: without FKs; else: with FKs)
            for (EntityMeta<?> meta : creationOrder) {
                String create = buildCreateTable(meta, !topo.hasCycle); // include FKs only if acyclic
                if (options.printDdl)
                    System.out.println(create + ";");
                st.executeUpdate(create);
            }

            // Phase 2 (only if cycle): add foreign keys via ALTER TABLE now that all tables
            // exist
            if (topo.hasCycle) {
                if (options.printDdl) {
                    System.out.println(
                            "-- Cycle detected in relationships. Foreign keys will be added after table creation.");
                }
                for (EntityMeta<?> meta : metas) {
                    List<String> fkAlters = buildForeignKeyAlters(meta);
                    for (String alter : fkAlters) {
                        if (options.printDdl)
                            System.out.println(alter + ";");
                        st.executeUpdate(alter);
                    }
                }
            }
        }
    }

    private String buildCreateTable(EntityMeta<?> meta, boolean includeForeignKeys) {
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
        if (includeForeignKeys) {
            // Foreign keys từ quan hệ MANY_TO_ONE: joinColumn phải là cột đã có trong map
            meta.getRelationships().stream()
                    .filter(r -> r.getType() == RelationshipType.MANY_TO_ONE && r.getJoinColumn() != null
                            && !r.getJoinColumn().isBlank())
                    .forEach(r -> {
                        @SuppressWarnings("unchecked")
                        EntityMeta<Object> target = (EntityMeta<Object>) EntityRegistry
                                .getMeta((Class<Object>) r.getTargetType());
                        if (target != null && target.getKeyField() != null) {
                            String targetPk = target.getFieldToColumnMap().get(target.getKeyField().getName())
                                    .getName();
                            String fkName = "fk_" + meta.getTableName().toLowerCase() + "_"
                                    + r.getJoinColumn().toLowerCase();
                            String fk = String.format(
                                    "CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE %s ON UPDATE %s",
                                    fkName, r.getJoinColumn(), target.getTableName(), targetPk, options.fkOnDelete,
                                    options.fkOnUpdate);
                            columnDefs.add(fk);
                        }
                    });
        }
        sb.append(String.join(", ", columnDefs));
        sb.append(")");
        return sb.toString();
    }

    private List<String> buildForeignKeyAlters(EntityMeta<?> meta) {
        List<String> alters = new ArrayList<>();
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
                        String alter = String.format(
                                "ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE %s ON UPDATE %s",
                                meta.getTableName(), fkName, r.getJoinColumn(), target.getTableName(), targetPk,
                                options.fkOnDelete, options.fkOnUpdate);
                        alters.add(alter);
                    }
                });
        return alters;
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

    private static class TopoResult {
        List<EntityMeta<?>> ordered = new ArrayList<>();
        boolean hasCycle = false;
    }

    private TopoResult topologicalSortOrDetectCycle(java.util.Collection<EntityMeta<?>> metas) {
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
        TopoResult result = new TopoResult();
        while (!dq.isEmpty()) {
            EntityMeta<?> x = dq.removeFirst();
            result.ordered.add(x);
            for (EntityMeta<?> nb : adj.getOrDefault(x, Set.of())) {
                indeg.put(nb, indeg.get(nb) - 1);
                if (indeg.get(nb) == 0)
                    dq.add(nb);
            }
        }
        if (result.ordered.size() != metas.size()) {
            result.hasCycle = true; // we'll fallback
        }
        return result;
    }
}
