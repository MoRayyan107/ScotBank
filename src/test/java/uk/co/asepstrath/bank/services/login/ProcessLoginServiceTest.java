package uk.co.asepstrath.bank.services.login;

import io.jooby.Context;
import io.jooby.Session;
import io.jooby.StatusCode;
import io.jooby.ValueNode;
import io.jooby.exception.StatusCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Constants;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static uk.co.asepstrath.bank.Constants.ROUTE_ACCOUNT;
import static uk.co.asepstrath.bank.Constants.ROUTE_LOGIN;

class ProcessLoginServiceTest {
    @Mock
    private Logger logger;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private Context context;

    @Mock
    private Session session;

    private ProcessLoginService service;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        service = new ProcessLoginService(dataSource, logger);

        // Setup common mocks
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(context.session()).thenReturn(session);
    }

    @Test
    void testProcessLoginAccountNotFound() throws SQLException {
        ValueNode value = mock(ValueNode.class);
        when(context.form("accountid")).thenReturn(value);
        when(value.valueOrNull()).thenReturn("funfun123"); // Non-existent in DB

        ValueNode password = mock(ValueNode.class);
        when(context.form("password")).thenReturn(password);
        when(password.valueOrNull()).thenReturn("password123");

        when(resultSet.next()).thenReturn(false);

        service.processLogin(context);

        verify(session).put(Constants.SESSION_ERROR_MESSAGE, "Incorrect username or password.");
        verify(context).sendRedirect(ROUTE_LOGIN);
    }

    @Test
    void testProcessLoginEmptyAccountId() throws SQLException {
        ValueNode valueNode = mock(ValueNode.class);
        when(context.form("accountid")).thenReturn(valueNode);
        when(valueNode.valueOrNull()).thenReturn("");

        ValueNode password = mock(ValueNode.class);
        when(context.form("password")).thenReturn(password);
        when(password.valueOrNull()).thenReturn("password123");

        service.processLogin(context);

        verify(session).put(Constants.SESSION_ERROR_MESSAGE, "Account ID cannot be empty.");
        verify(context).sendRedirect(ROUTE_LOGIN);

        // Verify no database interaction
        verify(dataSource, never()).getConnection();

    }

    @Test
    void testProcessLoginNullValue() throws SQLException {
        ValueNode valueNode = mock(ValueNode.class);
        when(context.form("accountid")).thenReturn(valueNode);
        when(valueNode.valueOrNull()).thenReturn(null);

        ValueNode password = mock(ValueNode.class);
        when(context.form("password")).thenReturn(password);
        when(password.valueOrNull()).thenReturn("password123");


        service.processLogin(context);

        verify(session).put(Constants.SESSION_ERROR_MESSAGE, "Account ID cannot be empty.");
        verify(context).sendRedirect(ROUTE_LOGIN);

        verify(dataSource, never()).getConnection();
    }

    @Test
    void testProcessLoginSuccess() throws SQLException {
        String accountId = "account";
        String name = "Peter Tasker";
        String password = "password123";
        String hashedPassword = "any-hashed-password"; // Doesn't matter what this is
        BigDecimal balance = new BigDecimal(25);
        boolean roundUpEnabled = true;

        ValueNode valueNode = mock(ValueNode.class);
        when(context.form("accountid")).thenReturn(valueNode);
        when(valueNode.valueOrNull()).thenReturn(accountId);

        ValueNode passwordValueNode = mock(ValueNode.class);
        when(context.form("password")).thenReturn(passwordValueNode);
        when(passwordValueNode.valueOrNull()).thenReturn(password);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("AccountID")).thenReturn(accountId);
        when(resultSet.getString("Name")).thenReturn(name);
        when(resultSet.getBigDecimal("Balance")).thenReturn(balance);
        when(resultSet.getBoolean("RoundUpEnabled")).thenReturn(roundUpEnabled);
        when(resultSet.getString("Password")).thenReturn(hashedPassword);

        // Mock the password verification to simply return true
        try (MockedStatic<HashingPasswordService> mockedStatic = mockStatic(HashingPasswordService.class)) {
            mockedStatic.when(() -> HashingPasswordService.verifyPassword(anyString(), anyString()))
                    .thenReturn(true);

            service.processLogin(context);
        }

        verify(session).put("accountid", accountId);
        verify(session).put("name", name);

        verify(context).sendRedirect(ROUTE_ACCOUNT);

        verify(dataSource).getConnection();
        verify(preparedStatement).setString(1, accountId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testProcessLoginDatabaseError() throws SQLException {
        ValueNode valueNode = mock(ValueNode.class);
        when(context.form("accountid")).thenReturn(valueNode);
        when(valueNode.valueOrNull()).thenReturn("myId");

        ValueNode password = mock(ValueNode.class);
        when(context.form("password")).thenReturn(password);
        when(password.valueOrNull()).thenReturn("password123");

        when(dataSource.getConnection()).thenThrow(new SQLException("Database connection failed"));

        StatusCodeException exception = assertThrows(StatusCodeException.class, () -> {
            service.processLogin(context);
        });

        assertEquals(StatusCode.SERVER_ERROR, exception.getStatusCode());
        assertEquals("Failed to reach database", exception.getMessage());

        verify(logger).error(contains("Database error"), anyString(), any(SQLException.class));
    }
}
