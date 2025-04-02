package uk.co.asepstrath.bank.services.reward;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import uk.co.asepstrath.bank.Reward;
import uk.co.asepstrath.bank.services.repository.RewardRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RewardViewServiceTest {
    @Mock
    private DataSource mockDataSource;
    @Mock
    private RewardRepository mockRepository;
    @Mock
    private Context mockContext;
    @Mock
    private Logger mockLogger;
    @Mock
    private Session mockSession;
    private RewardViewService rewardViewService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        rewardViewService = new RewardViewService(mockDataSource,mockLogger,mockRepository);

        when(mockContext.session()).thenReturn(mockSession);
    }

    @Test
    void testViewRewards() throws SQLException {
        List<Reward> mockRewards = List.of(
                new Reward("Sim Racing Kit","Real life experience",new BigDecimal(5000),5.01),
                new Reward("Gaming PC","Something", new BigDecimal(7500),0.1)
        );

        when(mockRepository.getAllRewards(any())).thenReturn(mockRewards);

        ModelAndView<Map<String,Object>> result = rewardViewService.viewRewardPage(mockContext);

        assertEquals("rewards.hbs",result.getView());
        Map<String,Object> model = result.getModel();
        List<Reward> rewardList = (List<Reward>) model.get("rewards");
        assertNotNull(rewardList);
        assertEquals(2, rewardList.size());
        assertEquals(mockRewards,rewardList);

    }

}
