package com.example.persistence_repository.persistence.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry trung tâm lưu trữ metadata của tất cả entity đã khai báo.
 * Cho phép:
 * - Đăng ký một hoặc nhiều entity (scan annotation sinh {@link EntityMeta}).
 * - Lấy metadata theo class.
 * - Lấy tập toàn bộ metadata để phục vụ schema generation.
 *
 * Đảm bảo insertion-order (LinkedHashMap) để vẫn giữ thứ tự đăng ký ban đầu
 * (dù việc tạo bảng sẽ còn xử lý lại bằng topological sort sau này).
 */
public final class EntityRegistry {
    private static final Map<Class<?>, EntityMeta<?>> REGISTRY = new LinkedHashMap<>();

    private EntityRegistry() {
    }

    public static synchronized <E> void register(Class<E> clazz) {
        if (REGISTRY.containsKey(clazz))
            return; // idempotent
        EntityMeta<E> meta = EntityMeta.scanAnnotation(clazz);
        REGISTRY.put(clazz, meta);
    }

    @SafeVarargs
    public static synchronized <E> void registerAll(Class<E>... classes) {
        for (Class<E> c : classes) {
            register(c);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> EntityMeta<E> getMeta(Class<E> clazz) {
        return (EntityMeta<E>) REGISTRY.get(clazz);
    }

    public static Collection<EntityMeta<?>> getAllMetas() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static boolean isEmpty() {
        return REGISTRY.isEmpty();
    }
}
