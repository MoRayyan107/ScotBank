package uk.co.asepstrath.bank.services.repository;

import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.*;
import uk.co.asepstrath.bank.services.CurrencyFormatter;
import uk.co.asepstrath.bank.services.login.HashingPasswordService;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * The Manager repository service
 */
public class ManagerRepository extends BaseRepository implements CurrencyFormatter {

    private static final String SQL_CREATE_TABLE = """
            CREATE TABLE Managers (
                ManagerID VARCHAR(255) NOT NULL,
                Name VARCHAR(255) NOT NULL,
                Password VARCHAR(512) NOT NULL,
                PRIMARY KEY (ManagerID)
            )
            """;
    private static final String SQL_INSERT_MANAGER =
            "INSERT INTO Managers (ManagerID, Name, Password) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ALL_ACCOUNTS =
            "SELECT AccountID, Name, Balance, RoundUpEnabled, CardNumber, CardCVV FROM Accounts ORDER BY Name ASC LIMIT ? OFFSET ?";
    private static final String SQL_SELECT_TOP_TEN_SPENDERS = """
            SELECT a.Name, a.Postcode, SUM(t.Amount) AS TotalAmount
            FROM Transactions t
            JOIN Accounts a ON t.SenderID = a.AccountID
            WHERE t.TransactionType = 'PAYMENT'
            GROUP BY a.Name, a.Postcode
            ORDER BY TotalAmount DESC
            LIMIT 10;
            """;
    private static final String SQL_GET_SANCTIONED_TRANSACTIONS = """
            SELECT\s
                b.BusinessID,
                b.BusinessName,
                b.Category,
                COUNT(t.TransactionID) AS TotalTransactions,
                SUM(t.Amount) AS TotalAmount,
                MIN(t.Timestamp) AS FirstTransactionDate,
                MAX(t.Timestamp) AS LastTransactionDate,
                SUM(CASE WHEN t.TransactionAccepted = TRUE THEN 1 ELSE 0 END) AS AcceptedTransactions,
                SUM(CASE WHEN t.TransactionAccepted = FALSE THEN 1 ELSE 0 END) AS RejectedTransactions
            FROM\s
                Businesses b
            JOIN\s
                Transactions t ON b.BusinessID = t.ReceiverBusinessID
            WHERE\s
                b.Sanctioned = TRUE
            GROUP BY\s
                b.BusinessID, b.BusinessName, b.Category
            ORDER BY\s
                SUM(t.Amount) DESC;
            """;

    public ManagerRepository(Logger logger) {
        super(logger);
    }

    /**
     * Creates the Manager table
     *
     * @param connection Database connection
     * @throws SQLException Database connection failure
     */
    public void createTable(Connection connection) throws SQLException {
        executeUpdate(connection, SQL_CREATE_TABLE);
    }

    /**
     * Inserts a Manager into the Manager table
     *
     * @param connection Database Connection
     * @param manager    manager object
     * @throws SQLException Database connection failure
     */
    public void insert(Connection connection, Manager manager, String password) throws SQLException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        String hashedPassword = HashingPasswordService.hashPassword(password);
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_MANAGER)) {
            statement.setString(1, manager.getManagerID());
            statement.setString(2, manager.getName());
            statement.setString(3, hashedPassword);
            statement.executeUpdate();
            logger.info("Manager: {}, Name: {}, Password: {}", manager.getManagerID(), manager.getName(), password);
        }
    }

    public int getCountOfAccounts(Connection connection) throws SQLException {
        int n = 0;
        String sql = "SELECT COUNT(AccountID) FROM Accounts";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    n = resultSet.getInt(1);
                }
            }
        }
        return n;
    }

    public List<Account> getPaginatedAccounts(Connection connection, int offset, int limit) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_ALL_ACCOUNTS)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    accounts.add(
                            new Account(
                                    resultSet.getString("AccountID"),
                                    resultSet.getString("Name"),
                                    resultSet.getBigDecimal("Balance"),
                                    resultSet.getBoolean("RoundUpEnabled"),
                                    new Card(
                                            resultSet.getString("CardNumber"),
                                            resultSet.getString("CardCVV")
                                    )
                            )
                    );
                }
            }
        }
        logger.info("Loaded {} accounts from DB", accounts.size());
        return accounts;
    }

    public List<Map<String, Object>> getTopTenSpenders(Connection connection) {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = SQL_SELECT_TOP_TEN_SPENDERS;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("Name", rs.getString("Name"));
                row.put("Postcode", rs.getString("Postcode"));
                row.put("TotalAmount", formatCurrency(rs.getBigDecimal("TotalAmount")));
                results.add(row);
            }
        }
        catch (SQLException e) {
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Failed to get top ten spenders");
        }
        return results;
    }

    @Override
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "£0.00"; // Default value if balance is null
        }

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.UK);
        DecimalFormat decimalFormat = (DecimalFormat) formatter;
        decimalFormat.applyPattern("#,###.00");

        return decimalFormat.format(amount);
    }

    public List<SanctionedBusinessReport> getSanctionedBusinessReports(Connection connection) throws SQLException {
        List<SanctionedBusinessReport> reports = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_SANCTIONED_TRANSACTIONS)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    // Create business from result set
                    Business business = new Business(
                            rs.getString("BusinessID"),
                            rs.getString("BusinessName"),
                            rs.getString("Category"),
                            true // Sanctioned is always true based on the WHERE clause
                    );

                    // Create custom constructor for this use case since we have no setters
                    SanctionedBusinessReport report = new SanctionedBusinessReport(
                            business,
                            rs.getInt("TotalTransactions"),
                            rs.getBigDecimal("TotalAmount"),
                            convertToDateTime(rs.getTimestamp("FirstTransactionDate")),
                            convertToDateTime(rs.getTimestamp("LastTransactionDate")),
                            rs.getInt("AcceptedTransactions"),
                            rs.getInt("RejectedTransactions")
                    );

                    reports.add(report);
                }
            }
        }

        return reports;
    }

    // Helper method to convert java.sql.Timestamp to DateTime
    private DateTime convertToDateTime(Timestamp timestamp) {
        if (timestamp == null) return null;
        return new DateTime(timestamp.getTime());
    }
}
