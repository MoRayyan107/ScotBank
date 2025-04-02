package uk.co.asepstrath.bank.services.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Manager;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ManagerRepositoryTest {

    @Mock
    private Logger mockLogger;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private ManagerRepository managerRepository;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        managerRepository = new ManagerRepository(mockLogger);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @Test
    void testCreateTable() throws SQLException {
        // Act
        managerRepository.createTable(mockConnection);

        // Assert
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testInsertManager() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Arrange
        Manager manager = new Manager("M123", "Test Manager");
        String password = "password123";

        // Act
        managerRepository.insert(mockConnection, manager, password);

        // Assert
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setString(1, manager.getManagerID());
        verify(mockPreparedStatement).setString(2, manager.getName());
        verify(mockPreparedStatement).setString(eq(3), anyString()); // Hashed password
        verify(mockPreparedStatement).executeUpdate();
        verify(mockLogger).info(anyString(), eq(manager.getManagerID()), eq(manager.getName()), eq(password));
    }

    @Test
    void testGetCountOfAccounts() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(5);

        // Act
        int count = managerRepository.getCountOfAccounts(mockConnection);

        // Assert
        assertEquals(5, count);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getInt(1);
    }

    @Test
    void testGetCountOfAccountsWhenEmpty() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        int count = managerRepository.getCountOfAccounts(mockConnection);

        // Assert
        assertEquals(0, count);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet, never()).getInt(anyInt());
    }

    @Test
    void testGetPaginatedAccounts() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true, true, false); // Return 2 accounts
        when(mockResultSet.getString("AccountID")).thenReturn("A123", "A456");
        when(mockResultSet.getString("Name")).thenReturn("John Doe", "Jane Smith");
        when(mockResultSet.getBigDecimal("Balance")).thenReturn(new BigDecimal("100.00"), new BigDecimal("200.00"));
        when(mockResultSet.getBoolean("RoundUpEnabled")).thenReturn(true, false);
        when(mockResultSet.getString("CardNumber")).thenReturn("1234567890", "0987654321");
        when(mockResultSet.getString("CardCVV")).thenReturn("123", "456");

        // Act
        List<Account> accounts = managerRepository.getPaginatedAccounts(mockConnection, 0, 10);

        // Assert
        assertEquals(2, accounts.size());

        // First account
        assertEquals("A123", accounts.get(0).getAccountID());
        assertEquals("John Doe", accounts.get(0).getName());
        assertEquals(new BigDecimal("100.00"), accounts.get(0).getBalance());
        assertTrue(accounts.get(0).isRoundUpEnabled());
        assertEquals("1234567890", accounts.get(0).getCard().getCardNumber());
        assertEquals("123", accounts.get(0).getCard().getCvv());

        // Second account
        assertEquals("A456", accounts.get(1).getAccountID());
        assertEquals("Jane Smith", accounts.get(1).getName());
        assertEquals(new BigDecimal("200.00"), accounts.get(1).getBalance());
        assertFalse(accounts.get(1).isRoundUpEnabled());
        assertEquals("0987654321", accounts.get(1).getCard().getCardNumber());
        assertEquals("456", accounts.get(1).getCard().getCvv());

        // Verify method calls
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(1, 10); // Limit
        verify(mockPreparedStatement).setInt(2, 0);  // Offset
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testGetPaginatedAccountsWhenEmpty() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Account> accounts = managerRepository.getPaginatedAccounts(mockConnection, 0, 10);

        // Assert
        assertTrue(accounts.isEmpty());
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(1, 10); // Limit
        verify(mockPreparedStatement).setInt(2, 0);  // Offset
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testGetTopTenSpenders() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true, true, false); // Return 2 spenders
        when(mockResultSet.getString("Name")).thenReturn("John Doe", "Jane Smith");
        when(mockResultSet.getString("Postcode")).thenReturn("AB12 3CD", "EF45 6GH");
        when(mockResultSet.getBigDecimal("TotalAmount")).thenReturn(new BigDecimal("1500.50"), new BigDecimal("1200.25"));

        // Act
        List<Map<String, Object>> spenders = managerRepository.getTopTenSpenders(mockConnection);

        // Assert
        assertEquals(2, spenders.size());

        // First spender
        assertEquals("John Doe", spenders.get(0).get("Name"));
        assertEquals("AB12 3CD", spenders.get(0).get("Postcode"));
        assertEquals("1,500.50", spenders.get(0).get("TotalAmount"));

        // Second spender
        assertEquals("Jane Smith", spenders.get(1).get("Name"));
        assertEquals("EF45 6GH", spenders.get(1).get("Postcode"));
        assertEquals("1,200.25", spenders.get(1).get("TotalAmount"));

        // Verify method calls
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testFormatCurrency() {
        // Arrange
        BigDecimal amount = new BigDecimal("1234.56");

        // Act
        String formattedAmount = managerRepository.formatCurrency(amount);

        // Assert
        assertEquals("1,234.56", formattedAmount);
    }

    @Test
    void testFormatCurrencyWithNullAmount() {
        // Act
        String formattedAmount = managerRepository.formatCurrency(null);

        // Assert
        assertEquals("£0.00", formattedAmount);
    }
}