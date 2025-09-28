package com.example.persistence_repository.persistence.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class LazyReferenceTest {

    @Test
    public void testLoadOnce() {
        AtomicInteger counter = new AtomicInteger();
        LazyReference<Integer> ref = new LazyReference<>(() -> {
            counter.incrementAndGet();
            return 42;
        });
        assertFalse(ref.isLoaded());
        assertEquals(42, ref.get().intValue());
        assertTrue(ref.isLoaded());
        assertEquals(42, ref.get().intValue());
        assertEquals(1, counter.get());
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        LazyReference<String> ref = new LazyReference<>(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
            counter.incrementAndGet();
            return "ok";
        });
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    assertEquals("ok", ref.get());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(3, TimeUnit.SECONDS);
        pool.shutdown();
        assertEquals(1, counter.get(), "Supplier phải chỉ được gọi đúng 1 lần");
        assertTrue(ref.isLoaded());
    }

    @Test
    public void testPeekIfLoaded() {
        LazyReference<String> ref = new LazyReference<>(() -> "v");
        assertNull(ref.peekIfLoaded());
        ref.get();
        assertEquals("v", ref.peekIfLoaded());
    }
}
