package uk.co.asepstrath.bank.services.data;

import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.plugins.MockMaker;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MockitoMockMakerTest {
    @Test
    void testMockMaker() {
        MockMaker mockMaker = Plugins.getMockMaker();
        assertTrue(mockMaker.toString().contains("InlineByteBuddyMockMaker"));
    }
}
