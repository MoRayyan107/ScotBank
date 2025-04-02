package uk.co.asepstrath.bank.services.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.UnirestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Reward;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RewardDataServiceTest {

    private RewardDataService rewardDataService ;
    private UnirestWrapper unirestWrapper;
    private Logger logger;
    private ObjectMapper objectMapper;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        unirestWrapper = mock(UnirestWrapper.class);
        logger = mock(Logger.class);
        objectMapper = new ObjectMapper();
        dataSource = mock(DataSource.class);
        rewardDataService = new RewardDataService(logger, unirestWrapper, objectMapper, dataSource);
    }

    @Test
    void testFetchDataSuccess() throws IOException {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccess()).thenReturn(true);
        when(mockResponse.getBody()).thenReturn("""
                [
                    {
                    "name": "Sim Racing Kit",
                    "description": "The best experience you will ever have",
                    "value": 5000,
                    "chance": 5.001
                    }
                
                ]
                """);

        when(unirestWrapper.get("https://api.asep-strath.co.uk/api/rewards")).thenReturn(mockResponse);
        List<Reward> rewards = rewardDataService.fetchData();

        assertNotNull(rewards);
        assertEquals(1, rewards.size());
        assertEquals("Sim Racing Kit", rewards.getFirst().getName());
        assertEquals("The best experience you will ever have", rewards.getFirst().getDescription());
        assertEquals(5000,rewards.getFirst().getValue().intValue());
        assertEquals(5.001,rewards.getFirst().getChance());
    }

    @Test
    void testFetchDataFailure(){
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccess()).thenReturn(false);

        when(unirestWrapper.get(anyString())).thenReturn(mockResponse);
        assertThrows(IOException.class, () -> rewardDataService.fetchData());
    }

    @Test
    void testForStatusCodeException(){
        when(unirestWrapper.get(anyString())).thenThrow(new UnirestException("failed to fetch"));

        assertThrows(StatusCodeException.class, () -> rewardDataService.fetchData());
    }

    @Test
    void testPostRewardSuccess(){
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccess()).thenReturn(true);
        when(mockResponse.getStatus()).thenReturn(StatusCode.valueOf(200).value());

        when(unirestWrapper.post(eq("https://api.asep-strath.co.uk/api/rewards"),anyString(),anyMap())).thenReturn(mockResponse);

        Reward reward  =new Reward("Some Reward","Something",new BigDecimal(2000),1);
        rewardDataService.postReward(reward, "acc123");

        verify(unirestWrapper).post(eq("https://api.asep-strath.co.uk/api/rewards"),anyString(),anyMap());
    }

    @Test
    void testPostRewardStatusCodeNot200(){
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccess()).thenReturn(true);
        when(mockResponse.getStatus()).thenReturn(StatusCode.valueOf(500).value());

        when(unirestWrapper.post(eq("https://api.asep-strath.co.uk/api/rewards"),anyString(),anyMap())).thenReturn(mockResponse);

        Reward reward  =new Reward("Some Reward","Something",new BigDecimal(2000),1);

        StatusCodeException exception = assertThrows(StatusCodeException.class, () -> rewardDataService.postReward(reward, "acc123"));
        assertEquals(StatusCode.SERVER_ERROR, exception.getStatusCode());

    }

    @Test
    void testPostRewardFailure(){
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccess()).thenReturn(false);
        when(mockResponse.getStatus()).thenReturn(400);

        when(unirestWrapper.post(eq("https://api.asep-strath.co.uk/api/rewards"),anyString(),anyMap())).thenThrow(new UnirestException("failed to fetch"));
        Reward reward  =new Reward("Some Reward","Something",new BigDecimal(2000),1);

        StatusCodeException exception = assertThrows(StatusCodeException.class, () -> rewardDataService.postReward(reward, "acc123"));
        assertEquals(StatusCode.BAD_REQUEST,exception.getStatusCode());
    }
}