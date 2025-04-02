package uk.co.asepstrath.bank.services.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.core.HttpResponse;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Transaction;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionDataServiceTest {

    private TransactionDataService transactionDataService;
    private UnirestWrapper unirestWrapper;
    private HttpResponse<String> mockResponse;
    private Logger mockLogger;
    private ObjectMapper objectMapper;
    private DataSource mockDataSource;
    private Connection mockConnection;

    @BeforeEach
    void setUp() throws SQLException {
        unirestWrapper = mock(UnirestWrapper.class);
        mockResponse = mock(HttpResponse.class);
        mockLogger = mock(Logger.class);
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        transactionDataService = new TransactionDataService(mockLogger, unirestWrapper, objectMapper, mockDataSource);
    }

    @Test
    void testFetchDataSuccess() throws XMLStreamException {
        // Mocking HttpResponse
        HttpResponse<String> firstPageResponse = mock(HttpResponse.class);
        when(firstPageResponse.isSuccess()).thenReturn(true);
        // Sample XML Data
        String firstPageXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<pageResult>" +
                "  <hasNext>true</hasNext>" +
                "  <hasPrevious>false</hasPrevious>" +
                "  <page>0</page>" +
                "  <results xsi:type=\"transactionModel\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "    <timestamp>2023-04-10 08:43</timestamp>" +
                "    <amount>21.00</amount>" +
                "    <from>8f95782c-7c83-4dd7-8856-0e19a0e0a075</from>" +
                "    <id>0043d8d9-846d-49cb-9b04-8d3823e9d8c9</id>" +
                "    <to>TOP</to>" +
                "    <type>PAYMENT</type>" +
                "  </results>" +
                "  <size>1</size>" +
                "  <totalPages>154</totalPages>" +
                "</pageResult>";

        when(firstPageResponse.getBody()).thenReturn(firstPageXml);
        when(firstPageResponse.getBody()).thenReturn(firstPageXml);
        when(unirestWrapper.get("https://api.asep-strath.co.uk/api/transactions", "page", 0)).thenReturn(
                firstPageResponse);

        List<Transaction> transactions = transactionDataService.fetchData();

        // Verify the results
        assertNotNull(transactions);
        verify(unirestWrapper).get("https://api.asep-strath.co.uk/api/transactions", "page", 0);
        verify(unirestWrapper, never()).get("https://api.asep-strath.co.uk/api/transactions", "page", 1);
    }

    @Test
    void testFetchDataWithHttpRequestFailure() throws XMLStreamException {
        // Mock a failed HTTP response
        when(mockResponse.isSuccess()).thenReturn(false);
        when(mockResponse.getStatus()).thenReturn(500);
        when(unirestWrapper.get(anyString(), eq("page"), anyInt())).thenReturn(mockResponse);

        List<Transaction> transactions = transactionDataService.fetchData();

        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
        verify(unirestWrapper, times(1)).get(anyString(), eq("page"), anyInt());
    }

    @Test
    void testFetchDataWithXmlParsingError() {
        // Mock a response that will cause an XML parsing error
        when(mockResponse.isSuccess()).thenReturn(true);
        when(mockResponse.getBody()).thenReturn("Invalid XML");
        when(unirestWrapper.get(anyString(), eq("page"), anyInt())).thenReturn(mockResponse);

        // Execute the method and verify exception
        assertThrows(XMLStreamException.class, () -> transactionDataService.fetchData());
    }

    @Test
    void testTransactionHandlingWithNullAmount() throws XMLStreamException {
        // Mock response with a transaction that has null amount
        when(mockResponse.isSuccess()).thenReturn(true);
        String xmlWithNullAmount = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<pageResult>" +
                "  <hasNext>false</hasNext>" +
                "  <hasPrevious>false</hasPrevious>" +
                "  <page>0</page>" +
                "  <results xsi:type=\"transactionModel\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "    <timestamp>2023-04-10 08:43</timestamp>" +
                "    <amount>48.00</amount>" +
                "    <from>8f95782c-7c83-4dd7-8856-0e19a0e0a075</from>" +
                "    <id>null-amount-id</id>" +
                "    <to>TOP</to>" +
                "    <type>PAYMENT</type>" +
                "  </results>" +
                "  <size>1</size>" +
                "  <totalPages>1</totalPages>" +
                "</pageResult>";
        when(mockResponse.getBody()).thenReturn(xmlWithNullAmount);
        when(unirestWrapper.get(anyString(), eq("page"), anyInt())).thenReturn(mockResponse);
        List<Transaction> transactions = transactionDataService.fetchData();

        // Verify the results
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    void testCreateTransactionSafely() throws Exception {
        // Mock dependencies
        TransactionDataService service = new TransactionDataService(mockLogger, unirestWrapper, objectMapper,
                mockDataSource);
        // Create test transaction with null amount
        Transaction fakeTransaction = new Transaction();
        setPrivateField(fakeTransaction, "id", "bad123");
        setPrivateField(fakeTransaction, "amount", null); // Ensure amount is null

        // Access private createTransactionSafely method
        Method createMethod = TransactionDataService.class.getDeclaredMethod(
                "createTransactionSafely", Transaction.class, Connection.class);
        createMethod.setAccessible(true);

        // Call the method
        Transaction result = (Transaction) createMethod.invoke(service, fakeTransaction, mockConnection);

        // Should be null as the transaction had a null amount
        assertNull(result);
        verify(mockLogger).warn("Skipping transaction with null amount: {}", "bad123");
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    void testFetchDataWithMultiplePages() throws XMLStreamException {
        // Create mock responses for two pages
        HttpResponse<String> firstPageResponse = mock(HttpResponse.class);
        HttpResponse<String> lastPageResponse = mock(HttpResponse.class);

        when(firstPageResponse.isSuccess()).thenReturn(true);
        when(lastPageResponse.isSuccess()).thenReturn(true);

        // First transaction on the first XML Page
        String firstPageXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<pageResult>" +
                "  <hasNext>true</hasNext>" +
                "  <hasPrevious>false</hasPrevious>" +
                "  <page>0</page>" +
                "  <results xsi:type=\"transactionModel\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "    <timestamp>2023-04-10 08:43</timestamp>" +
                "    <amount>48.00</amount>" +
                "    <from>ae89778c-0e6e-4bf7-937f-462d66c55974</from>" +
                "    <id>026b53d4-990a-4373-a88d-e491de65489f</id>" +
                "    <to>YAN</to>" +
                "    <type>PAYMENT</type>" +
                "  </results>" +
                "  <size>100</size>" +
                "  <totalPages>154</totalPages>" +
                "</pageResult>";

        // First transaction on the last XML Page
        String lastPageXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<pageResult>" +
                "  <hasNext>false</hasNext>" +
                "  <hasPrevious>true</hasPrevious>" +
                "  <page>153</page>" +
                "  <results xsi:type=\"transactionModel\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "    <timestamp>2023-11-28 08:43</timestamp>" +
                "    <amount>255.25</amount>" +
                "    <from>c9b72caa-4f29-4795-b17c-87c3332aa8a1</from>" +
                "    <id>8b2740bb-ed24-43d3-bf02-3264107db16c</id>" +
                "    <type>WITHDRAWAL</type>" +
                "  </results>" +
                "  <size>100</size>" +
                "  <totalPages>154</totalPages>" +
                "</pageResult>";

        when(firstPageResponse.getBody()).thenReturn(firstPageXml);
        when(lastPageResponse.getBody()).thenReturn(lastPageXml);

        // Configure the wrapper to return different responses for different pages
        when(unirestWrapper.get("https://api.asep-strath.co.uk/api/transactions", "page", 0))
                .thenReturn(firstPageResponse);
        when(unirestWrapper.get("https://api.asep-strath.co.uk/api/transactions", "page", 1))
                .thenReturn(lastPageResponse);

        // Execute the method
        List<Transaction> transactions = transactionDataService.fetchData();

        // Verify results
        assertNotNull(transactions);
        verify(unirestWrapper).get("https://api.asep-strath.co.uk/api/transactions", "page", 0);
        verify(unirestWrapper, never()).get("https://api.asep-strath.co.uk/api/transactions", "page", 153);
    }

    @Test
    void testCreateTransactionSafelyWithSQLException() throws Exception {
        when(mockDataSource.getConnection()).thenReturn(mockConnection);

        // Set up mock to throw SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQL Exception"));

        TransactionDataService service = new TransactionDataService(mockLogger, unirestWrapper, objectMapper,
                mockDataSource);

        // Create a transaction that will trigger SQL code in createTransactionSafely
        Transaction transaction = new Transaction();
        setPrivateField(transaction, "timestamp", new DateTime());
        setPrivateField(transaction, "amount", new BigDecimal("100.00"));
        setPrivateField(transaction, "id", "sql-error-id");
        setPrivateField(transaction, "from", "mock-from");
        setPrivateField(transaction, "to", "mock-to");
        setPrivateField(transaction, "type", "TRANSFER");

        // Access private createTransactionSafely method
        Method createMethod = TransactionDataService.class.getDeclaredMethod(
                "createTransactionSafely", Transaction.class, Connection.class);
        createMethod.setAccessible(true);

        // Call the method - should return null due to SQL exception
        Transaction result = (Transaction) createMethod.invoke(service, transaction, mockConnection);

        // Verify result is null due to exception
        assertNull(result);
        verify(mockLogger).error("Error processing transaction {}: {}", "sql-error-id", "Test SQL Exception");
    }
}


