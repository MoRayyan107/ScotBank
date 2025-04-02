/**
 * Unit Testing for Rewards class
 */

package uk.co.asepstrath.bank;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RewardsClassTest {

    @Test
    void testGetters(){
        Reward reward = new Reward("Sim Racing Kit","The best experience you will ever have",new BigDecimal(5000),5.001);
        assertEquals("Sim Racing Kit",reward.getName());
        assertEquals("The best experience you will ever have",reward.getDescription());
        assertEquals(BigDecimal.valueOf(5000), reward.getValue());
        assertEquals(5.001, reward.getChance());
        String expectedToString = String.format("name: %s%ndescription: %s%nvalue: %s%nchance: %s%n",
                "Sim Racing Kit",
                "The best experience you will ever have",
                new BigDecimal("5000"),
                5.001);
        assertEquals(expectedToString,reward.toString());

        String accountId = "abc123";
        String expectedToHJson = String.format("{\"rewardType\": \"%s\", \"team\": \"%s\", \"account\": \"%s\"}",
                "Sim Racing Kit", "Team05", accountId);

        assertEquals(expectedToHJson,reward.toJson(accountId));
    }
}
