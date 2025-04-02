package uk.co.asepstrath.bank.services.account;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.StatusCode;
import io.jooby.annotation.GET;
import io.jooby.exception.StatusCodeException;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Constants;
import uk.co.asepstrath.bank.Transaction;
import uk.co.asepstrath.bank.services.repository.TransactionRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.co.asepstrath.bank.Constants.*;

/**
 * The Account Viewing service
 */
public class AccountViewService extends AccountService {

    private final TransactionRepository transactionRepository;

    public AccountViewService(DataSource datasource, Logger logger) {
        super(datasource, logger);
        transactionRepository = new TransactionRepository(logger);
    }

    /**
     * Displays the "/account" endpoint
     *
     * @param ctx Session Context
     * @return the "/account" endpoint
     * @throws SQLException Transactions not found in database
     */
    @GET
    public ModelAndView<Map<String, Object>> viewAccount(Context ctx) throws SQLException {
        ensureAccountIsLoggedIn(ctx);

        // Pagination params
        int page = ctx.query("page").intValue(1);
        logger.info("Requesting page: {}", page);
        int limit = ctx.query("limit").intValue(10);

        Map<String, Object> model = createModel();
        Session session = getSession(ctx);

        // Setup basic account info
        addAccountDetailsToModel(model, session);
        addCardDetailsToModel(model, session);

        // Add account balances
        String accountId = getAccountIdFromSession(ctx);
        putAccountBalancesInModel(model, accountId);

        // Transfer session messages
        transferSessionMessages(ctx, model);

        // Get transaction history
        loadTransactionHistory(model, accountId, page, limit);

        // Add insights data to model
        addPaymentSumPerBusinessToModel(accountId, model);
        addPaymentCountPerBusinessToModel(accountId, model);

        return render(TEMPLATE_ACCOUNT, model);
    }

    /**
     * Sets up basic account information in the model
     */
    private void addAccountDetailsToModel(Map<String, Object> model, Session session) {
        model.put(SESSION_ACCOUNT_NAME, session.get(SESSION_ACCOUNT_NAME));
        model.put(SESSION_ACCOUNT_ID, session.get(SESSION_ACCOUNT_ID));
        logger.info("Put name and accountid in model");
    }

    /**
     * Transfers session messages to the model
     */
    private void transferSessionMessages(Context ctx, Map<String, Object> model) {
        transferSessionAttributeToModel(ctx, SESSION_SUCCESS_MESSAGE, model);
        transferSessionAttributeToModel(ctx, Constants.SESSION_ERROR_MESSAGE, model);
    }

    /**
     * Add card details to the model
     */
    private void addCardDetailsToModel(Map<String, Object> model, Session session) {
        try (PreparedStatement preparedStatement =
                     getConnection().prepareStatement("SELECT CardNumber, CardCVV FROM Accounts WHERE AccountId = ?")) {
            preparedStatement.setString(1, String.valueOf(session.get(SESSION_ACCOUNT_ID)));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    model.put(ACCOUNT_CARD_NUMBER, resultSet.getString(1));
                    model.put(ACCOUNT_CARD_CVV, resultSet.getString(2));
                }
            }
        }
        catch (SQLException e) {
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Failed to add card details", e);
        }
    }

    /**
     * Loads transaction history for an account
     */
    private void loadTransactionHistory(Map<String, Object> model, String accountId, int page, int limit) throws SQLException {
        try (Connection connection = getConnection()) {
            // Set up pagination
            int totalTransactions = transactionRepository.getTransactionCountByAccountId(connection, accountId);
            int totalPages = (int) Math.ceil((double) totalTransactions / (double) limit);
            int offset = Math.max(0, (page - 1) * limit);

            List<Transaction> transactions = transactionRepository.getPaginatedTransactionsByAccountId(connection, accountId, offset, limit);
            model.put(TRANSACTION_OBJECT_LIST_EXISTS, !transactions.isEmpty());

            // Create new list with display-ready transactions
            List<Map<String, Object>> displayTransactions = new ArrayList<>();
            for (Transaction transaction : transactions) {
                Map<String, Object> displayTx = new HashMap<>();
                displayTx.put("id", transaction.getId());
                displayTx.put("timestamp", transaction.getTimestamp());
                displayTx.put("from", transaction.getFrom());
                displayTx.put("to", transaction.getTo());
                displayTx.put("type", transaction.getType());
                displayTx.put("status", transaction.getStatus());

                // Format the amount
                displayTx.put("amount", formatCurrency(transaction.getAmount()));

                displayTransactions.add(displayTx);
            }

            model.put(TRANSACTION_OBJECT_LIST, displayTransactions);

            // Add pagination info to model
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("totalPages", totalPages);
            pagination.put("hasNext", page < totalPages);
            pagination.put("hasPrev", page > 1);
            model.put("pagination", pagination);


            logger.info("Pagination data - Page: {}, Total Pages: {}", page, totalPages);
            logger.info("Pagination Model: {}", model.get("pagination"));
            logger.info("Setting pagination: currentPage={}, totalPages={}", page, totalPages);





        }
    }

    /**
     * Counts the number of businesses per category for all payments made by a specific account
     *
     * @param accountId The ID of the logged-in account
     */
    private void addPaymentCountPerBusinessToModel(String accountId, Map<String, Object> model) throws SQLException {
        try (Connection connection = getConnection()) {
            Map<String, Integer> insightMap = transactionRepository.getTransactionsPerBusinessByCount(connection,
                    accountId);
            logger.info("Count businesses per category for account {} is {}", accountId, insightMap);
            model.put(BUSINESS_COUNTS, insightMap.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .map(entry -> Map.of("category", entry.getKey(), "count", entry.getValue()))
                    .toList());
        }
    }

    private void addPaymentSumPerBusinessToModel(String accountID, Map<String, Object> model) throws SQLException {
        try (Connection connection = getConnection()) {
            Map<String, BigDecimal> insightMap = transactionRepository.getTransactionsPerBusinessBySum(connection,
                    accountID);
            logger.info("Sum businesses per category for account {} is {}", accountID, insightMap);
            model.put(BUSINESS_AMOUNT_SUMS, insightMap.entrySet().stream()
                    // Sort in descending order
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .map(entry -> Map.of("category", entry.getKey(),
                            "totalAmount", formatCurrency(entry.getValue())))
                    .toList());
        }
    }
}
