package com.example.persistence_repository.persistence.entity;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Lazy, single-valued reference wrapper (for MANY_TO_ONE / ONE_TO_ONE
 * relationships).
 * <p>
 * Resolves the wrapped value the first time {@link #get()} (or
 * {@link #forceLoad()}) is invoked.
 * Subsequent calls return the cached value. Implementation is thread-safe using
 * double-checked locking. The supplier reference is released after load.
 * </p>
 *
 * <h3>Usage</h3>
 * 
 * <pre>{@code
 * LazyReference<Department> dept = new LazyReference<>(() -> deptRepo.findById(id));
 * // later
 * Department d = dept.get();
 * }</pre>
 *
 * <h3>Design decisions</h3>
 * <ul>
 * <li>No reload mechanism – create a new instance if you need to refresh.</li>
 * <li>{@link #peekIfLoaded()} allows non-triggering inspection (useful for
 * serializers).</li>
 * <li>Implements {@link java.util.function.Supplier} for ergonomic
 * integration.</li>
 * </ul>
 */
public final class LazyReference<T> implements Supplier<T> {
    private Supplier<T> supplier;
    private volatile boolean loaded = false;
    private T value;

    public LazyReference(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
    }

    /**
     * Indicates whether the underlying supplier has been executed.
     * 
     * @return true if value already loaded (may still be null if supplier returned
     *         null).
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Forces resolution of the underlying value (alias of {@link #get()}).
     * Useful for explicit eager loading semantics inside a materializer.
     * 
     * @return resolved value (may be null).
     */
    public T forceLoad() { // alias get() semantics
        return get();
    }

    @Override
    /**
     * Returns the resolved value, triggering supplier execution on first
     * invocation.
     * Thread-safe: multiple concurrent callers will all receive the same resolved
     * instance.
     * 
     * @return resolved value (may be null if supplier returns null).
     */
    public T get() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    value = supplier.get();
                    supplier = null; // giải phóng
                    loaded = true;
                }
            }
        }
        return value;
    }

    /**
     * Non-triggering inspection of the cached value.
     * 
     * @return value if already loaded; {@code null} if not yet loaded or if
     *         supplier's result was null.
     */
    public T peekIfLoaded() { // passive check
        return loaded ? value : null;
    }
}
