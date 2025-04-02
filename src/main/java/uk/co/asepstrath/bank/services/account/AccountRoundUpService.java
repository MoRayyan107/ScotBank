package uk.co.asepstrath.bank.services.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


import javax.sql.DataSource;

import org.slf4j.Logger;

import io.jooby.Context;
import uk.co.asepstrath.bank.Account;

import uk.co.asepstrath.bank.services.repository.AccountRepository;

public class AccountRoundUpService extends AccountService {
    private final AccountRepository accountRepository;

    public AccountRoundUpService(DataSource dataSource, Logger logger) {
        super(dataSource, logger);
        accountRepository = new AccountRepository(logger);
    }

    // Enable round up for the account
    public void enableDatabaseRoundUp(Context ctx) throws SQLException {
        String accountId = getAccountIdFromSession(ctx);
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
                "UPDATE Accounts SET RoundUpEnabled = TRUE WHERE AccountID = ?")) {
            statement.setString(1, accountId);
            statement.executeUpdate();
            Account account = accountRepository.getAccount(connection, accountId);
            logger.info("{} Round Up is now {}", accountId,account.isRoundUpEnabled());
        }
    }

    // Disable round up for the account
    public void disableDatabaseRoundUp(Context ctx) throws SQLException {
        String accountId = getAccountIdFromSession(ctx);
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
                "UPDATE Accounts SET RoundUpEnabled = FALSE WHERE AccountID = ?")) {
            statement.setString(1, accountId);
            statement.executeUpdate();
            Account account = accountRepository.getAccount(connection, accountId);
            logger.info("{} Round Up is now {}", accountId,account.isRoundUpEnabled());
        }
    }

    // Move savings from round up pot to the current balance
    public void reclaimSavings(Context ctx) throws SQLException{
        String accountId = getAccountIdFromSession(ctx);
        try (Connection connection = getConnection()){  
            Account account = accountRepository.getAccount(connection, accountId);
            account.reclaimSavings();
            accountRepository.updateBalance(connection, account);
            logger.info("Reclaimed savings for account {}", accountId);
        }
    }
}
