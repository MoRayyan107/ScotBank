package uk.co.asepstrath.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagerClassTest {

    private Manager manager;

    @BeforeEach
    void setUp() {
        manager = new Manager("12345", "Mr. Manager");
    }

    @Test
    void testGetManagerID() {
        assertEquals("12345", manager.getManagerID());
    }

    @Test
    void testGetManagerName() {
        assertEquals("Mr. Manager", manager.getName());
    }

    @Test
    void testGetManagerToString() {
        assertEquals("id: 12345" + System.lineSeparator() + "name: Mr. Manager", manager.toString());
    }
}
