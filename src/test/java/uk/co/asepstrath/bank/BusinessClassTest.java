package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BusinessClassTest {

    @Test
    void testGetters() {
        Business business = new Business("ALD", "Aldi", "Groceries", false);
        Assertions.assertEquals("ALD", business.getID());
        Assertions.assertEquals("Aldi", business.getName());
        Assertions.assertEquals("Groceries", business.getCategory());
        Assertions.assertFalse(business.isSanctioned());
    }
}
