package uk.co.asepstrath.bank.services.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.core.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Business;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BusinessDataServiceTest {

    private BusinessDataService businessDataService;
    private UnirestWrapper unirestWrapper;
    private Logger logger;
    private ObjectMapper objectMapper;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        unirestWrapper = mock(UnirestWrapper.class);
        logger = mock(Logger.class);
        objectMapper = new ObjectMapper();
        businessDataService = new BusinessDataService(logger, unirestWrapper, objectMapper, dataSource);
    }

    @Test
    void testFetchDataSuccess() throws IOException {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccess()).thenReturn(true);
        String csvData = String.join(System.lineSeparator(),
                "id,name,category,sanctioned",
                "ALD,Aldi,Groceries,false",
                "AMA,Amazon,Online Retailer,false",
                "ARG,Argos,Online Retailer,false",
                "BOO,Boom Battle Bar,Entertainment,false",
                "BOT,Boots,Health & Beauty,false",
                "BUR,Burger King,Eating Out,false",
                "CAF,Cafe Nero,Coffee,false",
                "CEX,CEX,Entertainment,true",
                "CLA,Clarks,Clothing,false"
        );
        when(mockResponse.getBody()).thenReturn(csvData);
        when(unirestWrapper.get(anyString())).thenReturn(mockResponse);
        List<Business> businesses = businessDataService.fetchData();

        // Assertions
        assertEquals(9, businesses.size());
        assertEquals("ALD", businesses.getFirst().getID());
        assertEquals("Aldi", businesses.getFirst().getName());
        assertEquals("Groceries", businesses.getFirst().getCategory());
        assertFalse(businesses.getFirst().isSanctioned());
    }

    @Test
    void testFetchDataFailure() {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccess()).thenReturn(false);
        when(unirestWrapper.get(anyString())).thenReturn(mockResponse);
        assertThrows(IOException.class, () -> businessDataService.fetchData());
    }
}
