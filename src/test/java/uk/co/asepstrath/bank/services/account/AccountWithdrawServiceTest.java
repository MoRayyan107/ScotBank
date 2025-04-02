package uk.co.asepstrath.bank.services.account;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Transaction;
import uk.co.asepstrath.bank.services.repository.AccountRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.co.asepstrath.bank.Constants.TEMPLATE_WITHDRAW;

class AccountWithdrawServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Logger logger;

    @Mock
    private Context context;

    @Mock
    private Session session;

    @Mock
    private Connection connection;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private AccountWithdrawService service;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        // Inject the mock repository directly into the service constructor
        service = new AccountWithdrawService(dataSource, logger, accountRepository);

        when(dataSource.getConnection()).thenReturn(connection);
        when(context.session()).thenReturn(session);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }


    @Test
    void testRenderWithdraw() {
        ModelAndView<Map<String, Object>> result = service.renderWithdraw(context);
        assertNotNull(result);
        assertEquals(TEMPLATE_WITHDRAW, result.getView());
    }

    @Test
    void testProcessWithdraw() throws SQLException {
        // Setup
        Account account = new Account("ABC123", "John Doe", new BigDecimal(100), true, null);
        BigDecimal withdrawAmount = new BigDecimal(50);

        AccountWithdrawService testService = new AccountWithdrawService(dataSource, logger, accountRepository) {
            @Override
            protected String getAccountIdFromSession(Context ctx) {
                return "ABC123";
            }

            @Override
            public BigDecimal getFormBigDecimal(Context ctx, String name) {
                return withdrawAmount;
            }

            @Override
            protected void executeTransaction(Context ctx, Connection connection, Transaction transaction) {
                // Do nothing in test
            }

            @Override
            protected void executeWithdrawal(Context ctx, Account account, BigDecimal amount) {
                // Do nothing in test
            }
        };

        when(accountRepository.getAccount(any(Connection.class), eq("ABC123"))).thenReturn(account);
        when(connection.createStatement()).thenReturn(mock(Statement.class));

        // Execute
        testService.processWithdraw(context);

        // Verify
        verify(logger).info("Enter withdraw process");
        verify(accountRepository).getAccount(any(Connection.class), eq("ABC123"));
        verify(connection).close();
    }
}