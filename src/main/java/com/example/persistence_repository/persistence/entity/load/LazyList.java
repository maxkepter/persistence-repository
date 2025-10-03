package com.example.persistence_repository.persistence.entity.load;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.Objects;

/**
 * Lazily materialized read-only style {@link List} wrapper.
 * <p>
 * The underlying data supplier is invoked exactly once upon the first data
 * access
 * (size / iteration / get). Subsequent calls reuse the resolved list reference.
 * The supplier reference is nulled after successful load to allow GC & guard
 * against accidental re-loading.
 * </p>
 *
 * <h3>Thread-safety</h3>
 * Uses double-checked locking to guarantee single execution of the supplier.
 * The returned list itself is not defensively copied; callers must ensure they
 * do
 * not mutate it concurrently. For fully immutable semantics, have the supplier
 * return an unmodifiable list.</h3>
 *
 * <h3>Typical usage</h3>
 * 
 * <pre>{@code
 * LazyList<Order> orders = new LazyList<>(() -> orderRepository.findByUserId(userId));
 * if (!orders.isLoaded()) {
 *     // still not touched
 * }
 * int size = orders.size(); // triggers load
 * }</pre>
 *
 * <h3>Limitations</h3>
 * <ul>
 * <li>No support for partial loading / pagination inherently.</li>
 * <li>No explicit invalidation / reload (create a new instance instead).</li>
 * <li>Assumes supplier is idempotent and side-effect free.</li>
 * </ul>
 */
public class LazyList<T> extends AbstractList<T> {
    private Supplier<List<T>> supplier;
    private List<T> data;
    private volatile boolean loaded = false;

    private void init() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    List<T> result = supplier.get();
                    data = Objects.requireNonNull(result, "LazyList supplier returned null");
                    loaded = true;
                    supplier = null;
                }
            }

        }
    }

    /**
     * Creates a new lazy wrapper.
     * 
     * @param supplier idempotent data provider returning the full list when
     *                 invoked.
     *                 Must not return null.
     */
    public LazyList(Supplier<List<T>> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
    }

    /**
     * @return true if the underlying supplier has already been executed.
     */
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    /**
     * Triggers loading (if needed) then returns element count.
     */
    public int size() {
        init();
        return data.size();
    }

    @Override
    /**
     * Triggers loading (if needed) then returns the element at the specified index.
     */
    public T get(int index) {
        init();
        return data.get(index);
    }

    @Override
    /**
     * Triggers loading then reports if the underlying list is empty.
     */
    public boolean isEmpty() {
        init();
        return data.isEmpty();
    }

    @Override
    /**
     * Triggers loading then returns an iterator over the resolved list.
     */
    public Iterator<T> iterator() {
        init();
        return data.iterator();
    }

}
