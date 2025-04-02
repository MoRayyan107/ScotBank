package uk.co.asepstrath.bank.services.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Card;
import uk.co.asepstrath.bank.services.login.HashingPasswordService;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class AccountRepositoryTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private Logger logger;

    private AccountRepository accountRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        accountRepository = new AccountRepository(logger);
    }

    @Test
    void testCreateTable() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        accountRepository.createTable(connection);

        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsert() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        Account account = new Account("12345", "Peter Tasker", new BigDecimal("90.21"), true, new Card("123", "345"));
        String psw = "Password123";
        String hashedPassword = HashingPasswordService.hashPassword(psw);
        accountRepository.insert(connection, account, hashedPassword);

        verify(connection).prepareStatement(contains("INSERT INTO Accounts"));
        verify(preparedStatement).setString(1, "12345");
        verify(preparedStatement).setBigDecimal(3, new BigDecimal("90.21"));
        verify(preparedStatement).setString(4, "Peter Tasker");
        verify(preparedStatement).setBoolean(5, true);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsertException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        SQLException sqlException = new SQLException("Database error");
        doThrow(sqlException).when(preparedStatement).executeUpdate();

        Account account = new Account("12345", "Peter Tasker", new BigDecimal("90.21"), true, new Card("123", "345"));
        assertThrows(SQLException.class, () -> {
            accountRepository.insert(connection, account, "Password123");
        });
    }


    @Test
    void testUpdateBalance() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        Account account = new Account("12345", "Peter Tasker", new BigDecimal("90.21"), true, new Card("123", "345"));

        accountRepository.updateBalance(connection, account);

        verify(connection).prepareStatement(contains("UPDATE Accounts"));
        verify(preparedStatement).setBigDecimal(1, new BigDecimal("90.21"));
        preparedStatement.setBigDecimal(2, BigDecimal.ZERO);
        preparedStatement.setString(3, "12345");
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    @Test
    void testGetAccount() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBigDecimal("Balance")).thenReturn(new BigDecimal("90.21"));
        when(resultSet.getString("Name")).thenReturn("Peter Tasker");
        when(resultSet.getBoolean("RoundUpEnabled")).thenReturn(true);

        Account account = accountRepository.getAccount(connection, "12345");

        assertNotNull(account);
        assertEquals("12345", account.getAccountID());
        assertEquals("Peter Tasker", account.getName());
        assertEquals(new BigDecimal("90.21"), account.getBalance());
        assertTrue(account.isRoundUpEnabled());
        verify(preparedStatement).setString(1, "12345");
    }

    @Test
    void testGetAccountException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Account account = accountRepository.getAccount(connection, "12345");
        assertNull(account);
        verify(preparedStatement).setString(1, "12345");
    }
}
