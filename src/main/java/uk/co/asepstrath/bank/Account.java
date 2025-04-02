package uk.co.asepstrath.bank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static uk.co.asepstrath.bank.Constants.ACCOUNT_OBJECT_MAX_BALANCE;

/**
 * The Account class
 */
public class Account {

    private static final Logger log = LoggerFactory.getLogger(Account.class);
    private final String accountID;
    private final boolean roundUpEnabled;
    private final String name;
    private final Card card;
    private BigDecimal balance;
    private BigDecimal roundUpBalance;
    private String postcode;

    @JsonCreator
    public Account(
            @JsonProperty("id") String accountID,
            @JsonProperty("name") String name,
            @JsonProperty("startingBalance") BigDecimal balance,
            @JsonProperty("roundUpEnabled") boolean roundUpEnabled,
            @JsonProperty("postcode") String postcode,
            @JsonProperty("card") Card card
    ) {
        this.accountID = accountID;
        this.balance = balance;
        this.name = name;
        this.roundUpEnabled = roundUpEnabled;
        this.roundUpBalance = BigDecimal.ZERO;
        this.postcode = postcode;
        this.card = card;
    }

    public Account(String accountID, String name, BigDecimal balance, boolean roundUpEnabled, Card card) {
        this.accountID = accountID;
        this.balance = balance;
        this.name = name;
        this.roundUpEnabled = roundUpEnabled;
        this.roundUpBalance = roundUpBalance != null ? roundUpBalance : BigDecimal.ZERO;
        this.card = card;
    }


    public Card getCard() {
        return card;
    }

    /**
     * Deposits money into an account
     *
     * @param amount The deposit value
     * @throws ArithmeticException Bad user input when depositing
     */
    public void deposit(BigDecimal amount) throws ArithmeticException {

        // Amount <= 0
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArithmeticException("Deposit amount needs to be greater than 0");
        }

        // Amount would lead to overflow in SQL database
        if (balance.add(amount).compareTo(ACCOUNT_OBJECT_MAX_BALANCE) > 0) {
            throw new ArithmeticException("Deposit would exceed maximum account balance");
        }

        balance = balance.add(amount);
    }

    /**
     * Withdraws money from an account
     *
     * @param amount The Withdrawal value
     * @throws ArithmeticException Bad user input when withdrawing
     */
    public void withdraw(BigDecimal amount) throws ArithmeticException {
        if (amount == null) {
            throw new ArithmeticException("Withdrawal amount cannot be null");
        }
        if (amount.compareTo(balance) > 0) { // if amount is greater than current Balance throw exception
            throw new ArithmeticException("Insufficient Funds: cannot withdrawal amount more than available balance");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) { // if withdraw amount is less or equal to 0
            throw new ArithmeticException("Withdraw amount must be greater than 0");
        }
        balance = balance.subtract(amount);
    }

    public void overdraftWithdraw(BigDecimal amount) throws ArithmeticException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) { // if withdraw amount is less or equal to 0
            throw new ArithmeticException("Withdraw amount must be greater than 0");
        }
        if(roundUpEnabled){
            BigDecimal difference = amount.setScale(0, RoundingMode.UP).subtract(amount);
            roundUpBalance = roundUpBalance.add(difference);
            amount = amount.add(difference);
        }
        balance = balance.subtract(amount); 
    }

    /**
     * Gets balance from the account
     *
     * @return Account balance
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Gets the account ID from the account
     *
     * @return Account ID
     */
    public String getAccountID() {
        return accountID;
    }

    /**
     * Gets the name of the account
     *
     * @return Account name
     */
    public String getName() {
        return name;
    }

    public BigDecimal getRoundUpBalance() {
        return roundUpBalance;
    }

    public void addToRoundUpBalance(BigDecimal amount) {
        if (roundUpEnabled && amount.compareTo(BigDecimal.ZERO) > 0) {
            roundUpBalance = roundUpBalance.add(amount);
        }
        log.info("Round up balance: {}", roundUpBalance);
    }
    
    public void reclaimSavings() throws ArithmeticException {
        if (roundUpBalance == null){
            log.error("Cannot reclaim savings: roundUpBalance is null");
            throw new ArithmeticException("No savings to reclaim");
        }
        if (roundUpBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArithmeticException("No savings to reclaim");
        }

        balance = balance.add(roundUpBalance);
        roundUpBalance = BigDecimal.ZERO;
        log.info("RECLAIMING SAVINGS");
    }

    /**
     * Gets the boolean value of roundUpEnabled
     *
     * @return roundUpEnabled
     */
    public boolean isRoundUpEnabled() {
        return roundUpEnabled;
    }

    public void updateRoundUpBalance(BigDecimal amount) {
        if (roundUpEnabled) {
            this.roundUpBalance = amount;
        }
    }

    /**
     * Returns a String interpretation of an Account
     *
     * @return A String of the Account
     */
    public String toString() {
        return String.format("id: %s%nname: %s%nbalance: %s%nroundUpEnabled: %s",
                getAccountID(), getName(), getBalance(), isRoundUpEnabled());
    }

    /**
     * Updates balance of an account
     *
     * @param balance new balance
     */
    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getPostcode() {
        return postcode;
    }
}
