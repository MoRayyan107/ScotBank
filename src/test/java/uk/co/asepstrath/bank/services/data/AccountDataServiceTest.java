package uk.co.asepstrath.bank.services.data;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.core.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountDataServiceTest {

    private AccountDataService accountDataService;
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
        accountDataService = new AccountDataService(logger, unirestWrapper, objectMapper, dataSource);

    }

    @Test
    void testFetchDataSuccess() throws IOException {
        // Mock token response
        HttpResponse<String> mockTokenResponse = mock(HttpResponse.class);
        when(mockTokenResponse.isSuccess()).thenReturn(true);
        when(mockTokenResponse.getBody()).thenReturn("{\"access_token\":\"mock-token\"}");

        // Mock accounts response
        HttpResponse<String> mockAccountsResponse = mock(HttpResponse.class);
        when(mockAccountsResponse.isSuccess()).thenReturn(true);
        when(mockAccountsResponse.getBody()).thenReturn("""
                    [
                        {
                            "id": "1",
                            "name": "Account1",
                            "startingBalance": 100.0,
                            "roundUpEnabled": true,
                            "postcode": "AB12 3CD"
                        }
                    ]
                """);

        // Configure mock wrapper
        when(unirestWrapper.post(eq("https://api.asep-strath.co.uk/oauth2/token"), anyString(), anyMap()))
                .thenReturn(mockTokenResponse);
        when(unirestWrapper.get(eq("https://api.asep-strath.co.uk/api/accounts"), anyMap(), anyMap()))
                .thenReturn(mockAccountsResponse);

        List<Account> accounts = accountDataService.fetchData();

        // Assertions
        assertNotNull(accounts);
        assertEquals(1, accounts.size());
        assertEquals("1", accounts.getFirst().getAccountID());
        assertEquals("Account1", accounts.getFirst().getName());
    }

    @Test
    void testFetchDataFailure() {
        // Mock token response
        HttpResponse<String> mockTokenResponse = mock(HttpResponse.class);
        when(mockTokenResponse.isSuccess()).thenReturn(true);
        when(mockTokenResponse.getBody()).thenReturn("{\"access_token\":\"mock-token\"}");

        // Mock failed accounts response
        HttpResponse<String> mockAccountsResponse = mock(HttpResponse.class);
        when(mockAccountsResponse.isSuccess()).thenReturn(false);
        when(mockAccountsResponse.getStatus()).thenReturn(404);

        // Configure mock wrapper
        when(unirestWrapper.post(anyString(), anyString(), anyMap())).thenReturn(mockTokenResponse);
        when(unirestWrapper.get(anyString(), anyMap(), anyMap())).thenReturn(mockAccountsResponse);

        assertThrows(JsonParseException.class, () -> accountDataService.fetchData());
    }
}