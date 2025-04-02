package uk.co.asepstrath.bank.services.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.asepstrath.bank.Manager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagerDataServiceTest {
    private ManagerDataService managerDataService;

    @BeforeEach
    void setUp() {
        managerDataService = new ManagerDataService();
    }

    @Test
    void testCreateManagers() {
        List<Manager> managers = managerDataService.fetchData();
        assertEquals(5, managers.size());
        assertEquals("Oles Vynnychuk", managers.getFirst().getName());
    }
}
