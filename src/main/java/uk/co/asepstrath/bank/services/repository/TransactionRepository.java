package uk.co.asepstrath.bank.services.repository;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import static uk.co.asepstrath.bank.Constants.ACCOUNT_OBJECT_MAX_BALANCE;

/**
 * The Transaction repository service
 */
public class TransactionRepository extends BaseRepository {

    private static final String SQL_CREATE_TABLE = """
            CREATE TABLE Transactions (
                Timestamp DATETIME NOT NULL,
                Amount DECIMAL(12,2) NOT NULL,
                SenderID VARCHAR(255) NULL,
                TransactionID VARCHAR(255) NOT NULL,
                ReceiverAccountID VARCHAR(255) NULL,
                ReceiverBusinessID VARCHAR(255) NULL,
                TransactionType VARCHAR(255) NOT NULL,
                TransactionAccepted BIT NOT NULL,
                PRIMARY KEY (TransactionID),
                FOREIGN KEY (ReceiverAccountID) REFERENCES Accounts(AccountID),
                FOREIGN KEY (ReceiverBusinessID) REFERENCES Businesses(BusinessID),
                FOREIGN KEY (SenderID) REFERENCES Accounts(AccountID),
                CONSTRAINT CHK_Receiver CHECK (ReceiverAccountID IS NOT NULL OR ReceiverBusinessID IS NOT NULL OR SenderID IS NOT NULL)
            )
            """;

    private static final String SQL_INSERT_TRANSACTION =
            "INSERT INTO Transactions (Timestamp, Amount, SenderID, TransactionID, ReceiverAccountID, " +
                    "ReceiverBusinessID, TransactionType, TransactionAccepted)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_GET_TRANSACTIONS_SUM = """
            SELECT
                b.Category,
                SUM(t.Amount) AS TotalAmount
            FROM Transactions t
            INNER JOIN Businesses b ON t.ReceiverBusinessID = b.BusinessID
            WHERE t.SenderID = ?
            AND t.TransactionAccepted = TRUE
            AND t.ReceiverBusinessID IS NOT NULL
            AND t.TransactionType = 'PAYMENT'
            GROUP BY b.Category
            ORDER BY TotalAmount DESC""";

    private static final String SQL_GET_TRANSACTIONS_COUNT = """
            SELECT
                b.Category,
                COUNT(DISTINCT b.BusinessID) AS BusinessCount,
            FROM Transactions t
            INNER JOIN Businesses b ON t.ReceiverBusinessID = b.BusinessID
            WHERE t.SenderID = ?
            AND t.TransactionAccepted = TRUE
            AND t.ReceiverBusinessID IS NOT NULL
            AND t.TransactionType = 'PAYMENT'
            GROUP BY b.Category
            ORDER BY BusinessCount DESC""";

    public TransactionRepository(Logger logger) {
        super(logger);
    }

    /**
     * Creates the Transaction table
     *
     * @param connection Database connection
     * @throws SQLException Database connection failure
     */
    public void createTable(Connection connection) throws SQLException {
        executeUpdate(connection, SQL_CREATE_TABLE);
    }

    /**
     * Inserts a Transaction into the Transaction table
     *
     * @param connection  Database Connection
     * @param transaction a Transaction object
     * @throws SQLException        Database connection failure
     * @throws ArithmeticException If the transaction amount exceeds the maximum allowed balance
     */
    public void insert(Connection connection, Transaction transaction) throws SQLException, ArithmeticException {
        // Check if the transaction amount exceeds the allowed maximum balance
        if (transaction.getAmount().compareTo(ACCOUNT_OBJECT_MAX_BALANCE) > 0) {
            throw new ArithmeticException("Amount given exceeds maximum possible value");
        }

        try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT_TRANSACTION)) {
            stmt.setTimestamp(1, new Timestamp(transaction.getTimestamp().getMillis()));
            stmt.setString(2, transaction.getAmount().toString());

            // If the transaction type is "DEPOSIT", set 'from' field to NULL; otherwise, set it to the transaction's
            // 'from' account
            if (Objects.equals(transaction.getType(), "DEPOSIT")) {
                stmt.setNull(3, java.sql.Types.VARCHAR);
            }
            else {
                stmt.setString(3, transaction.getFrom());
            }

            stmt.setString(4, transaction.getId());

            // If the transaction type is "PAYMENT":
            // - Set 'intermediary' field (column 5) to NULL
            // - Set 'to' field (column 6) to the recipient's account
            if (Objects.equals(transaction.getType(), "PAYMENT")) {
                stmt.setNull(5, Types.VARCHAR);
                stmt.setString(6, transaction.getTo());
            }

            // Otherwise, set 'intermediary' to the 'to' account and leave 'to' as NULL
            else {
                stmt.setString(5, transaction.getTo());
                stmt.setNull(6, Types.VARCHAR);
            }

            stmt.setString(7, transaction.getType());
            stmt.setBoolean(8, transaction.getStatus());
            stmt.executeUpdate();
        }
    }


    public Map<String, BigDecimal> getTransactionsPerBusinessBySum(Connection connection, String accountId) throws
            SQLException {
        Map<String, BigDecimal> map = new HashMap<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_TRANSACTIONS_SUM)) {
            preparedStatement.setString(1, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String category = resultSet.getString("Category");
                    BigDecimal totalAmount = resultSet.getBigDecimal("TotalAmount");
                    map.put(category, totalAmount);
                }
            }
        }
        return map;
    }


    public Map<String, Integer> getTransactionsPerBusinessByCount(Connection connection, String accountId) throws
            SQLException {
        Map<String, Integer> map = new HashMap<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_TRANSACTIONS_COUNT)) {
            preparedStatement.setString(1, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String category = resultSet.getString("Category");
                    int count = resultSet.getInt("BusinessCount");
                    map.put(category, count);
                }
            }
        }
        return map;
    }


    /**
     * Creates a Transaction object from a database result set
     */
    private Transaction createTransactionFromResultSet(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("Timestamp");
        DateTime dateTime = new DateTime(timestamp);
        BigDecimal amount = resultSet.getBigDecimal("Amount");
        String senderID = resultSet.getString("SenderID");
        String transactionID = resultSet.getString("TransactionID");
        String receiverAccountID = resultSet.getString("ReceiverAccountID");
        String receiverBusinessID = resultSet.getString("ReceiverBusinessID");

        String receiverID = (receiverAccountID != null) ? receiverAccountID : receiverBusinessID;

        String transactionType = resultSet.getString("TransactionType");
        boolean transactionAccepted = resultSet.getBoolean("TransactionAccepted");

        return new Transaction(dateTime, amount, senderID, transactionID, receiverID, transactionType,
                transactionAccepted);
    }

    public int getTransactionCountByAccountId(Connection connection, String accountId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Transactions WHERE SenderID = ? OR ReceiverAccountID = ? OR ReceiverBusinessID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            stmt.setString(2, accountId);
            stmt.setString(3, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    public List<Transaction> getPaginatedTransactionsByAccountId(Connection connection, String accountId, int offset, int limit) throws SQLException {
        String sql = "SELECT Timestamp, Amount, SenderID, TransactionID, ReceiverAccountID, " +
                "ReceiverBusinessID, TransactionType, TransactionAccepted " +
                "FROM Transactions " +
                "WHERE SenderID = ? OR ReceiverAccountID = ? OR ReceiverBusinessID = ? " +
                "ORDER BY Timestamp DESC, TransactionID DESC " +
                "LIMIT ? OFFSET ?";

        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            stmt.setString(2, accountId);
            stmt.setString(3, accountId);
            stmt.setInt(4, limit);
            stmt.setInt(5, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(createTransactionFromResultSet(rs));
                }
            }
        }
        return transactions;
    }
}
