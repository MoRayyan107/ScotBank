package uk.co.asepstrath.bank.services.repository;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Transaction;

import java.math.BigDecimal;
import java.sql.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionRepositoryTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private Logger logger;

    @Mock
    private AccountRepository accountRepository;

    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionRepository = new TransactionRepository(logger);
    }

    @Test
    void testInsertDeposit() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        DateTime dateTime = new DateTime(2025, 2, 14, 8, 30, 0);
        Transaction transaction = new Transaction(
                dateTime,
                new BigDecimal(250),
                "thisshouldnotmatter",
                "123",
                "ABC123",
                "DEPOSIT",
                true
        );

        transactionRepository.insert(connection, transaction);

        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).setTimestamp(1, new Timestamp(dateTime.getMillis()));
        verify(preparedStatement).setString(2, "250");
        verify(preparedStatement).setNull(3, Types.VARCHAR);
        verify(preparedStatement).setString(4, transaction.getId());
        verify(preparedStatement).setString(5, transaction.getTo());
        verify(preparedStatement).setNull(6, Types.VARCHAR);
        verify(preparedStatement).setString(7, "DEPOSIT");
        verify(preparedStatement).setBoolean(8, true);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInsertPayment() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        DateTime dateTime = new DateTime(2025, 2, 14, 8, 30, 0);
        Transaction transaction = new Transaction(
                dateTime,
                new BigDecimal(100),
                "thisshouldnotmatter",
                "TXN456",
                "iGotThisMoney",
                "PAYMENT",
                true
        );

        transactionRepository.insert(connection, transaction);

        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).setTimestamp(1, new Timestamp(dateTime.getMillis()));
        verify(preparedStatement).setString(2, "100");
        verify(preparedStatement).setString(3, transaction.getFrom());
        verify(preparedStatement).setString(4, transaction.getId());
        verify(preparedStatement).setNull(5, Types.VARCHAR);
        verify(preparedStatement).setString(6, transaction.getTo());
        verify(preparedStatement).setString(7, "PAYMENT");
        verify(preparedStatement).setBoolean(8, true);
        verify(preparedStatement).executeUpdate();

    }

    @Test
    void testArithmeticException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        DateTime dateTime = new DateTime(2025, 2, 14, 8, 30, 0);
        Transaction transaction = new Transaction(
                dateTime,
                new BigDecimal(1000000000),
                "thisshouldnotmatter",
                "123",
                "ABC123",
                "DEPOSIT",
                true
        );

        Assertions.assertThrows(ArithmeticException.class, () -> transactionRepository.insert(connection, transaction));
    }
}
