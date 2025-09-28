package com.example.persistence_repository.persistence.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class LazyListTest {

    @Test
    public void testLazyLoadOnce() {
        AtomicInteger counter = new AtomicInteger();
        LazyList<Integer> list = new LazyList<>(() -> {
            counter.incrementAndGet();
            List<Integer> data = new ArrayList<>();
            data.add(1);
            data.add(2);
            return data;
        });
        assertFalse(list.isLoaded());
        assertEquals(2, list.size());
        assertTrue(list.isLoaded());
        assertEquals(2, list.get(1).intValue());
        assertEquals(1, counter.get()); // chỉ load 1 lần
    }

    @Test
    public void testIteratorTriggersLoad() {
        AtomicInteger counter = new AtomicInteger();
        LazyList<String> list = new LazyList<>(() -> {
            counter.incrementAndGet();
            return List.of("a", "b", "c");
        });
        assertFalse(list.isLoaded());
        int count = 0;
        for (String s : list) {
            count++;
        }
        assertEquals(3, count);
        assertEquals(1, counter.get());
        assertTrue(list.isLoaded());
    }

    @Test
    public void testIsEmpty() {
        AtomicInteger counter = new AtomicInteger();
        LazyList<String> list = new LazyList<>(() -> {
            counter.incrementAndGet();
            return List.of();
        });
        assertTrue(list.isEmpty());
        assertEquals(1, counter.get());
    }
}
