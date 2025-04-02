package uk.co.asepstrath.bank;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.asepstrath.bank.services.repository.AccountRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The Transaction class
 */
public class Transaction {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)
    @JacksonXmlProperty(localName = "timestamp")
    private DateTime timestamp;
    @JacksonXmlProperty(localName = "amount")
    private BigDecimal amount;
    @JacksonXmlProperty(localName = "from")
    private String from;
    @JacksonXmlProperty(localName = "id")
    private String id;
    @JacksonXmlProperty(localName = "to")
    private String to;
    @JacksonXmlProperty(localName = "type")
    private String type;
    private boolean transactionStatus;

    private AccountRepository accountRepository;
    private Logger logger;


    public Transaction() {
        // For jackson mapper
    }

    // For transactions where the status is resolved
    public Transaction(DateTime timestamp, BigDecimal amount, String from, String id, String to, String type,
                       boolean transactionStatus) {
        this.timestamp = timestamp;
        this.amount = amount;
        this.from = from;
        this.id = id;
        this.to = to;
        this.type = type;
        this.transactionStatus = transactionStatus;

    }

    // For transactions where the status is unresolved
    public Transaction(Connection connection, DateTime timestamp, BigDecimal amount, String from, String id, String to,
                       String type) throws SQLException {
        this.timestamp = timestamp;
        this.amount = amount;
        this.from = from;
        this.id = id;
        this.to = to;
        this.type = type;
        logger = LoggerFactory.getLogger(this.getClass());
        accountRepository = new AccountRepository(logger);
        this.transactionStatus = processTransaction(connection);
    }

    /**
     * x
     * Gets the DateTime of the Transaction
     *
     * @return the "timestamp" of the Transaction
     */
    public DateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the amount value of the Transaction
     *
     * @return the "amount" of the Transaction
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Gets the sender of the Transaction
     *
     * @return the "from" of the Transaction
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets the unique ID of the Transaction
     *
     * @return the "id" of the Transaction
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the recipient of the Transaction
     *
     * @return the "to" of the Transaction
     */
    public String getTo() {
        return to;
    }

    /**
     * Gets the type of transaction (Transfer, Payment...)
     *
     * @return The "type" of Transaction
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the boolean value of if the Transaction was accepted or declined
     *
     * @return the status of the Transaction
     */
    public boolean getStatus() {
        return transactionStatus;
    }

    private boolean processTransaction(Connection connection) throws SQLException {
        // Ensure the transaction has valid account references before processing
        if (!validateAccountsForTransactionType()) {
            logger.info("Transaction validation failed: {}", this);
            return false;
        }

        try {
            return switch (getType()) {
                case "DEPOSIT" -> handleDeposit(connection);
                case "WITHDRAWAL" -> handleWithdrawal(connection);
                case "TRANSFER" -> handleTransfer(connection);
                case "PAYMENT" -> handlePayment(connection);
                default -> {
                    logger.warn("Unknown transaction type: {}", this);
                    yield false;
                }
            };
        }
        catch (ArithmeticException e) {
            logger.error("Arithmetic exception occurred during transaction: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates whether the necessary account fields are set for a given transaction type.
     */
    private boolean validateAccountsForTransactionType() {
        return switch (getType()) {
            case "DEPOSIT" -> {
                if (getTo() == null) {
                    logger.warn("Deposit transaction must have a 'to' account");
                    yield false;
                }
                yield true;
            }
            case "WITHDRAWAL" -> {
                if (getFrom() == null) {
                    logger.warn("Withdrawal transaction must have a 'from' account");
                    yield false;
                }
                yield true;
            }
            case "TRANSFER", "PAYMENT" -> {
                if (getFrom() == null || getTo() == null) {
                    logger.warn("Transfer/Payment transaction must have both 'from' and 'to' accounts");
                    yield false;
                }
                yield true;
            }
            default -> {
                logger.warn("Unknown transaction type: {}", getType());
                yield false;
            }
        };
    }

    /**
     * Handles deposit transactions by adding funds to the recipient's account.
     */
    private boolean handleDeposit(Connection connection) throws SQLException {
        Account account = accountRepository.getAccount(connection, getTo());
        if (account == null) {
            logger.warn("Deposit failed: Account {} not found", getTo());
            return false;
        }

        account.deposit(getAmount());
        accountRepository.updateBalance(connection, account);
        return true;
    }

    /**
     * Handles withdrawal transactions by deducting funds from the sender's account.
     */
    private boolean handleWithdrawal(Connection connection) throws SQLException {
        Account account = accountRepository.getAccount(connection, getFrom());
        if (account == null) {
            logger.warn("Withdrawal failed: Account {} not found", getFrom());
            return false;
        }

        account.withdraw(getAmount());
        accountRepository.updateBalance(connection, account);
        return true;
    }

    /**
     * Handles transfer transactions by moving funds between two accounts.
     */
    private boolean handleTransfer(Connection connection) throws SQLException {
        Account sender = accountRepository.getAccount(connection, getFrom());
        Account receiver = accountRepository.getAccount(connection, getTo());

        if (sender == null || receiver == null) {
            logger.warn("Transfer failed: Sender ({}) or Receiver ({}) account not found", getFrom(), getTo());
            return false;
        }

        sender.withdraw(getAmount());
        receiver.deposit(getAmount());
        accountRepository.updateBalance(connection, sender);
        accountRepository.updateBalance(connection, receiver);
        return true;
    }

    /**
     * Handles payment transactions by withdrawing funds from the payer's account.
     */
    private boolean handlePayment(Connection connection) throws SQLException {
        Account payer = accountRepository.getAccount(connection, getFrom());
        if (payer == null) {
            logger.warn("Payment failed: Payer account {} not found", getFrom());
            return false;
        }
        try {        
            payer.overdraftWithdraw(getAmount());
            accountRepository.updateBalance(connection, payer);
            return true;
        }
        catch (ArithmeticException e) {
            logger.error("Insufficient funds for payment transaction: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Returns a String interpretation of a Transaction
     *
     * @return A String of the Transaction
     */
    public String toString() {
        return "Transaction [timestamp=" + timestamp + ", amount=" + amount + ", from=" + from + ", id=" + id + ", " +
                "to=" + to + ", type=" + type + ", transactionStatus=" + transactionStatus + "]";
    }
}
