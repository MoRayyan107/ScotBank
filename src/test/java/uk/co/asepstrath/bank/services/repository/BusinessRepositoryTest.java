package uk.co.asepstrath.bank.services.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class BusinessRepositoryTest {

    BusinessRepository businessRepository;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private Logger logger;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        businessRepository = new BusinessRepository(logger);
    }

    @Test
    void testCreateTable() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        businessRepository.createTable(connection);

        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsert() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        Business business = new Business("12345", "JPMorgan", "Evil", false);

        businessRepository.insert(connection, business);

        verify(connection).prepareStatement(contains("INSERT INTO Business"));
        verify(preparedStatement).setString(1, "12345");
        verify(preparedStatement).setString(2, "JPMorgan");
        verify(preparedStatement).setString(3, "Evil");
        verify(preparedStatement).setBoolean(4, false);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsertException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        SQLException sqlException = new SQLException("Error");
        doThrow(sqlException).when(preparedStatement).executeUpdate();

        Business business = new Business("12345", "JPMorgan", "Evil", false);
        assertThrows(SQLException.class, () -> {
            businessRepository.insert(connection, business);
        });
    }
}
