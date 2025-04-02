package uk.co.asepstrath.bank.controllers;

import io.jooby.*;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountControllerTest {


    @Mock
    private AccountController accountController;
    @Mock
    private Context mockCtx;
    @Mock
    private Session mockSession;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private DataSource mockDataSource;
    @Mock
    private Logger mockLogger;

    @BeforeEach
    void setUp() throws Exception {
        mockCtx = mock(Context.class);
        mockSession = mock(Session.class);
        mockDataSource = mock(DataSource.class);
        mockLogger = mock(Logger.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockCtx.session()).thenReturn(mockSession);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        accountController = new AccountController(mockDataSource, mockLogger);
    }

    @Test
    void testViewAccountWithTransactions() throws Exception {
        // Mock session values



        Value mockNameValue = mock(Value.class);
        when(mockNameValue.toString()).thenReturn("John Doe");
        Value mockAccountValue = mock(Value.class);
        when(mockAccountValue.toString()).thenReturn("12345");

        ValueNode mockPageValue = mock(ValueNode.class);
        when(mockPageValue.intValue(1)).thenReturn(1);
        when(mockCtx.query("page")).thenReturn(mockPageValue);

        ValueNode mockLimitValue = mock(ValueNode.class);
        when(mockLimitValue.intValue(10)).thenReturn(10);
        when(mockCtx.query("limit")).thenReturn(mockLimitValue);

        when(mockSession.get("name")).thenReturn(mockNameValue);
        when(mockSession.get("accountid")).thenReturn(mockAccountValue);

        // Mock result set with transactions
        when(mockResultSet.next()).thenReturn(true, false); // One transaction
        Timestamp mockTimestamp = new Timestamp(new DateTime(2024, 2, 21, 12, 0).getMillis());
        when(mockResultSet.getTimestamp("Timestamp")).thenReturn(mockTimestamp);
        when(mockResultSet.getInt("Amount")).thenReturn(100);
        when(mockResultSet.getString("SenderID")).thenReturn("12345");
        when(mockResultSet.getString("TransactionID")).thenReturn("TXN001");
        when(mockResultSet.getString("ReceiverID")).thenReturn("67890");
        when(mockResultSet.getString("TransactionType")).thenReturn("Transfer");

        // Execute method
        ModelAndView<Map<String, Object>> result = accountController.viewAccount(mockCtx);

        // Validate ModelAndView
        assertNotNull(result);
        Map<String, Object> model = result.getModel();
        assertEquals("John Doe", model.get("name").toString());
        assertEquals("12345", model.get("accountid").toString());

    }

    @Test
    void testViewAccountWithNoTransactions() throws Exception {
        // Mock session values
        Value mockNameValue = mock(Value.class);
        when(mockNameValue.toString()).thenReturn("Jane Smith");
        Value mockAccountValue = mock(Value.class);
        when(mockAccountValue.toString()).thenReturn("67890");

        ValueNode mockPageValue = mock(ValueNode.class);
        when(mockPageValue.intValue(1)).thenReturn(1);
        when(mockCtx.query("page")).thenReturn(mockPageValue);

        ValueNode mockLimitValue = mock(ValueNode.class);
        when(mockLimitValue.intValue(10)).thenReturn(10);
        when(mockCtx.query("limit")).thenReturn(mockLimitValue);

        when(mockSession.get("name")).thenReturn(mockNameValue);
        when(mockSession.get("accountid")).thenReturn(mockAccountValue);

        // Mock empty result set (no transactions)
        when(mockResultSet.next()).thenReturn(false);

        // Execute method
        ModelAndView<Map<String, Object>> result = accountController.viewAccount(mockCtx);

        // Validate ModelAndView
        assertNotNull(result);
        Map<String, Object> model = result.getModel();
        assertEquals("Jane Smith", model.get("name").toString());
        assertEquals("67890", model.get("accountid").toString());
        assertNotNull(model.get("transactions"));
    }

    @Test
    void testDepositRender() throws Exception {
        // Mock session values
        Value mockNameValue = mock(Value.class);
        when(mockNameValue.toString()).thenReturn("John Doe");
        Value mockAccountValue = mock(Value.class);
        when(mockAccountValue.toString()).thenReturn("12345");

        when(mockSession.get("name")).thenReturn(mockNameValue);
        when(mockSession.get("accountid")).thenReturn(mockAccountValue);

        // Mock balance retrieval
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("Balance")).thenReturn(500);

        // Execute method
        ModelAndView<Map<String, Object>> result = accountController.deposit(mockCtx);

        // Validate ModelAndView without assuming specific model entries
        assertNotNull(result);
    }

    @Test
    void testWithdrawRender() throws Exception {
        Value mockNameValue = mock(Value.class);
        when(mockNameValue.toString()).thenReturn("John Doe");
        Value mockAccountValue = mock(Value.class);
        when(mockAccountValue.toString()).thenReturn("12345");

        when(mockSession.get("name")).thenReturn(mockNameValue);
        when(mockSession.get("accountid")).thenReturn(mockAccountValue);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("Balance")).thenReturn(500);

        ModelAndView<Map<String, Object>> result = accountController.withdraw(mockCtx);
        assertNotNull(result);
    }
}
