package uk.co.asepstrath.bank.services.repository;

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

/**
 * The Account repository service
 */
public class AccountRepository extends BaseRepository {

    private static final String SQL_CREATE_TABLE = """
                CREATE TABLE Accounts (
                AccountID VARCHAR(64) NOT NULL,
                Password VARCHAR(255) NOT NULL,
                Balance DECIMAL(12,2) NOT NULL,
                Name VARCHAR(128) NOT NULL,
                RoundUpEnabled BIT NOT NULL,
                RoundUpAmount DECIMAL(12,2) NULL DEFAULT 0,
                Postcode VARCHAR(8) NOT NULL,
                CardNumber VARCHAR(32) NOT NULL,
                CardCVV VARCHAR(3) NOT NULL,
                PRIMARY KEY (AccountID)
            )
            """;

    private static final String SQL_INSERT_ACCOUNT =
            "INSERT INTO Accounts (AccountID, Password, Balance, Name, RoundUpEnabled, Postcode, CardNumber, CardCVV)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_BALANCE =
            "UPDATE Accounts SET Balance = ?, RoundUpAmount = ? WHERE AccountID = ?";

    private static final String SQL_GET_ACCOUNT =
            "SELECT Balance, Name, RoundUpEnabled, RoundUpAmount, CardNumber, CardCVV FROM Accounts WHERE AccountID =" +
                    " ?";

    public AccountRepository(Logger logger) {
        super(logger);
    }

    /**
     * Creates the Account table
     *
     * @param connection Database connection
     * @throws SQLException Database connection failure
     */
    public void createTable(Connection connection) throws SQLException {
        executeUpdate(connection, SQL_CREATE_TABLE);
    }

    /**
     * Inserts an Account into the Account table
     *
     * @param connection Database Connection
     * @param account    An Account object
     * @throws SQLException Database connection failure
     */
    public void insert(Connection connection, Account account, String password) throws SQLException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        // Hash plaintext password before storing in the database
        String hashedPassword = HashingPasswordService.hashPassword(password);
        // Attempt to insert a new Account into the database
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_ACCOUNT)) {
            statement.setString(1, account.getAccountID());
            statement.setString(2, hashedPassword);
            statement.setBigDecimal(3, account.getBalance());
            statement.setString(4, account.getName());
            statement.setBoolean(5, account.isRoundUpEnabled());
            statement.setString(6, account.getPostcode()); // Get admin status somehow
            statement.setString(7, account.getCard().getCardNumber());
            statement.setString(8, account.getCard().getCvv());
            statement.executeUpdate();
            logger.info("Inserted account {}, round up enabled: {}, Default password: {}, postcode: {}, Card: {}",
                    account.getAccountID(),
                    account.isRoundUpEnabled(),
                    password,
                    account.getPostcode(),
                    account.getCard()
            );
        }
    }

    /**
     * Updates the balance of an Account
     *
     * @param connection Database connection
     * @param account    An Account object
     * @throws SQLException Database connection failure
     */
    public void updateBalance(Connection connection, Account account) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BALANCE)) {
            statement.setBigDecimal(1, account.getBalance());
            statement.setBigDecimal(2, account.getRoundUpBalance()); // Update round up balance (don't need NULL anymore?)
            statement.setString(3, account.getAccountID());
            statement.executeUpdate();
        }
    }

    /**
     * Gets an Account object from the database
     *
     * @param connection Database connection
     * @param accountID  Unique identifier of an account
     * @return An Account object parsed from the database
     * @throws SQLException Database connection failure
     */
    public Account getAccount(Connection connection, String accountID) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_ACCOUNT)) {
            statement.setString(1, accountID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BigDecimal balance = resultSet.getBigDecimal("Balance");
                    String name = resultSet.getString("Name");
                    boolean roundUpEnabled = resultSet.getBoolean("RoundUpEnabled");
                    Account account = new Account(accountID, name, balance, roundUpEnabled, new Card(
                            resultSet.getString("CardNumber"),
                            resultSet.getString("CardCVV")
                    ));
                    BigDecimal roundUpAmount = resultSet.getBigDecimal("RoundUpAmount");
                    if (roundUpAmount == null) {
                        roundUpAmount = BigDecimal.ZERO;
                    }
                    account.updateRoundUpBalance(roundUpAmount);
                    
                    return account;
                }
            }
        }
        return null;
    }
}
