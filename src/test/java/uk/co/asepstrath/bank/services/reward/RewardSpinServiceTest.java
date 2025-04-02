package uk.co.asepstrath.bank.services.reward;

import io.jooby.Context;
import io.jooby.Session;
import io.jooby.ValueNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.asepstrath.bank.Reward;
import uk.co.asepstrath.bank.services.data.RewardDataService;
import uk.co.asepstrath.bank.services.repository.RewardRepository;
import java.lang.reflect.*;

import javax.sql.DataSource;

import org.slf4j.Logger;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.co.asepstrath.bank.Constants.SESSION_ACCOUNT_ID;

class RewardSpinServiceTest {
    @Mock
    private DataSource dataSource;
    @Mock
    private Logger logger;
    @Mock
    private RewardRepository rewardRepository;
    @Mock
    private RewardDataService rewardDataService;
    @Mock
    private Context mockContext;
    @Mock
    private Session mockSession;
    private RewardSpinService rewardSpinService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rewardSpinService = spy(new RewardSpinService(dataSource, logger, rewardRepository));
        rewardSpinService.rewardRepository = rewardRepository;
        when(mockContext.session()).thenReturn(mockSession);
        ValueNode accountID = mock(ValueNode.class);
        when(mockSession.get(SESSION_ACCOUNT_ID)).thenReturn(accountID);
        when(accountID.value()).thenReturn("acc123");

    }

    @Test
    void tsetProcessSpinSuccess() throws SQLException{
        Reward reward = new Reward("ASUS TUF", "Gaming Laptop", new BigDecimal(3500), 50.00);
        List<Reward> rewards = List.of(reward);

        when(rewardRepository.getAllRewards(any())).thenReturn(rewards);
        rewardSpinService.processSpin(mockContext);

        doNothing().when(rewardSpinService).addMessageToSession(any(),anyString(),anyString());
        verify(rewardRepository, times(1)).getAllRewards(any());
    }

    @Test
    void testSelectWeightedRandomRewardDistributionMatches() throws NoSuchFieldException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        // Create test rewards with known probabilities
        Reward laptop = new Reward("ASUS TUF", "Gaming Laptop", new BigDecimal(3500), 50.0);
        Reward trip = new Reward("Vacation trip", "Trip to Dubai", new BigDecimal(5000), 20.0);
        Reward tickets = new Reward("DisneyLand", "Disney Land Tickets", new BigDecimal(900), 30.0);
        List<Reward> rewards = Arrays.asList(laptop, trip, tickets);

        SecureRandom mockRandom = mock(SecureRandom.class);
        Field randomField = RewardSpinService.class.getDeclaredField("secureRandom");
        randomField.setAccessible(true);
        randomField.set(rewardSpinService, mockRandom);

        // Test specific boundaries
        // For laptop (0-49.9%)
        when(mockRandom.nextDouble()).thenReturn(0.0);
        assertEquals(laptop, invokeSelectWeightedRandomReward(rewardSpinService, rewards));

        when(mockRandom.nextDouble()).thenReturn(0.499);
        assertEquals(laptop, invokeSelectWeightedRandomReward(rewardSpinService, rewards));

        // For trip (50-69.9%)
        when(mockRandom.nextDouble()).thenReturn(0.5);
        assertEquals(trip, invokeSelectWeightedRandomReward(rewardSpinService, rewards));

        when(mockRandom.nextDouble()).thenReturn(0.699);
        assertEquals(trip, invokeSelectWeightedRandomReward(rewardSpinService, rewards));

        // For tickets (70-100%)
        when(mockRandom.nextDouble()).thenReturn(0.7);
        assertEquals(tickets, invokeSelectWeightedRandomReward(rewardSpinService, rewards));

        when(mockRandom.nextDouble()).thenReturn(0.999);
        assertEquals(tickets, invokeSelectWeightedRandomReward(rewardSpinService, rewards));
    }

    // Helper method to invoke the private method
    private Reward invokeSelectWeightedRandomReward(RewardSpinService service, List<Reward> rewards)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method selectRewardMethod = RewardSpinService.class.getDeclaredMethod("selectWeightedRandomReward", List.class);
        selectRewardMethod.setAccessible(true);
        return (Reward) selectRewardMethod.invoke(service, rewards);
    }

    @Test
    void testSelectWeightedRandomRewardStatisticalDistribution()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Create test rewards with known probabilities
        Reward laptop = new Reward("ASUS TUF", "Gaming Laptop", new BigDecimal(3500), 50.0);
        Reward trip = new Reward("Vacation trip", "Trip to Dubai", new BigDecimal(5000), 20.0);
        Reward tickets = new Reward("DisneyLand", "Disney Land Tickets", new BigDecimal(900), 30.0);
        List<Reward> rewards = Arrays.asList(laptop, trip, tickets);

        int totalTrials = 10000;
        Map<Reward, Integer> distribution = new HashMap<>();

        for (int i = 0; i < totalTrials; i++) {
            Reward selected = invokeSelectWeightedRandomReward(rewardSpinService, rewards);
            distribution.put(selected, distribution.getOrDefault(selected, 0) + 1);
        }

        // Check if distribution is close to expected probabilities (within 3% margin)
        double laptopPercentage = (double) distribution.getOrDefault(laptop, 0) / totalTrials * 100;
        double tripPercentage = (double) distribution.getOrDefault(trip, 0) / totalTrials * 100;
        double ticketsPercentage = (double) distribution.getOrDefault(tickets, 0) / totalTrials * 100;

        assertTrue(Math.abs(laptopPercentage - 50.0) < 3.0);
        assertTrue(Math.abs(tripPercentage - 20.0) < 3.0);
        assertTrue(Math.abs(ticketsPercentage - 30.0) < 3.0);
    }

    @Test
    void testSelectWeightedRandomRewardEmptyList() {
        assertThrows(InvocationTargetException.class, () ->
                invokeSelectWeightedRandomReward(rewardSpinService, Collections.emptyList())
        );
    }

    @Test
    void testSelectWeightedRandomRewardSingleReward()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Reward reward = new Reward("ASUS TUF", "Gaming Laptop", new BigDecimal(3500), 100.0);
        List<Reward> rewards = Collections.singletonList(reward);

        assertEquals(reward, invokeSelectWeightedRandomReward(rewardSpinService, rewards));
    }
}