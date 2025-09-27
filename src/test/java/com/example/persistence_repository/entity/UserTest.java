package com.example.persistence_repository.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void constructorAndGettersWork() {
        User user = new User(1, "Alice", "alice@example.com");
        assertEquals(1, user.getId());
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
    }

    @Test
    void settersUpdateFields() {
        User user = new User();
        user.setId(2);
        user.setName("Bob");
        user.setEmail("bob@example.com");
        assertAll(
                () -> assertEquals(2, user.getId()),
                () -> assertEquals("Bob", user.getName()),
                () -> assertEquals("bob@example.com", user.getEmail()));
    }

    @Test
    void toStringContainsFields() {
        User user = new User(3, "Carol", "carol@example.com");
        String s = user.toString();
        assertTrue(s.contains("3"));
        assertTrue(s.contains("Carol"));
        assertTrue(s.contains("carol@example.com"));
    }
}
