package uk.co.asepstrath.bank.services.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.*;
import uk.co.asepstrath.bank.services.data.*;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import static uk.co.asepstrath.bank.Constants.DEFAULT_MANAGER_PASSWORD;
import static uk.co.asepstrath.bank.Constants.DEFAULT_PASSWORD;

/**
 * The manager for all repository interactions
 */
public class DatabaseManager implements DatabaseOperations {

    private final DataSource dataSource;
    private final Logger logger;

    private final AccountRepository accountRepository;
    private final BusinessRepository businessRepository;
    private final TransactionRepository transactionRepository;
    private final ManagerRepository managerRepository;
    private final RewardRepository rewardRepository;

    private final DataServiceFetcher<Account> accountDataService;
    private final DataServiceFetcher<Business> businessDataService;
    private final DataServiceFetcher<Transaction> transactionDataService;
    private final DataServiceFetcher<Manager> managerDataService;
    private final DataServiceFetcher<Reward> rewardDataService;

    public DatabaseManager(DataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
        ObjectMapper objectMapper = new ObjectMapper();
        UnirestWrapper unirestWrapper = new UnirestWrapper();


        // Instantiate repository services
        this.accountRepository = new AccountRepository(logger);
        this.businessRepository = new BusinessRepository(logger);
        this.transactionRepository = new TransactionRepository(logger);
        this.managerRepository = new ManagerRepository(logger);
        this.rewardRepository = new RewardRepository(logger);

        this.accountDataService = new AccountDataService(logger, unirestWrapper, objectMapper, dataSource);
        this.businessDataService = new BusinessDataService(logger, unirestWrapper, objectMapper, dataSource);
        this.transactionDataService = new TransactionDataService(logger, unirestWrapper, objectMapper, dataSource);
        this.managerDataService = new ManagerDataService();
        this.rewardDataService = new RewardDataService(logger, unirestWrapper, objectMapper, dataSource);

    }

    public static String generatePassword(String accountID) throws NoSuchAlgorithmException {
        String plainTextPassword = accountID + DEFAULT_PASSWORD;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(plainTextPassword.getBytes(StandardCharsets.UTF_8));

        String encodedHash = Base64.getEncoder().withoutPadding().encodeToString(hash);

        return "Psw@" + encodedHash.substring(0, 8) + "$$";
    }

    public static String generateManagerPassword(String managerID) throws NoSuchAlgorithmException {
        String plainTextPassword = managerID + DEFAULT_MANAGER_PASSWORD;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(plainTextPassword.getBytes(StandardCharsets.UTF_8));

        String encodedHash = Base64.getEncoder().withoutPadding().encodeToString(hash);

        return "Manager@" + encodedHash.substring(0, 8) + "$$";
    }

    /**
     * Instantiates the creation and insertion of API data
     *
     * @throws SQLException       If database connection or queries fail
     * @throws IOException        If API parsing fails
     * @throws XMLStreamException If XML parsing fails
     */
    public void initialise() throws SQLException, IOException, XMLStreamException {
        try (Connection connection = dataSource.getConnection()) {
            createTables(connection);
            insertData(connection);
        }
    }

    /**
     * Creates all the tables used in the database
     *
     * @param connection Database connection
     * @throws SQLException Database connection failure
     */
    @Override
    public void createTables(Connection connection) throws SQLException {
        accountRepository.createTable(connection);
        logger.info("Account table created");

        businessRepository.createTable(connection);
        logger.info("Business table created");

        transactionRepository.createTable(connection);
        logger.info("Transaction table created");

        managerRepository.createTable(connection);
        logger.info("Manager table created");

        rewardRepository.createTable(connection);
        logger.info("Rewards table created");
    }

    /**
     * Inserts initial API data into the database
     *
     * @param connection Database connection
     * @throws SQLException       Database connection failure
     * @throws IOException        API parsing failure
     * @throws XMLStreamException API parsing failure
     */
    @Override
    public void insertData(Connection connection) throws SQLException, IOException, XMLStreamException {

        // Insert Accounts
        List<Account> accounts = accountDataService.fetchData();
        for (Account account : accounts) {
            try {
                String password = generatePassword(account.getAccountID());
                accountRepository.insert(connection, account, password);
            }
            catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new SQLException("Unable to insert account");
            }
        }
        logger.info("Accounts inserted");

        // Insert Businesses
        List<Business> businesses = businessDataService.fetchData();
        for (Business business : businesses) {
            businessRepository.insert(connection, business);
        }
        logger.info("Businesses inserted");

        // Insert Transactions
        List<Transaction> transactions = transactionDataService.fetchData();
        for (Transaction transaction : transactions) {
            transactionRepository.insert(connection, transaction);
        }
        logger.info("Transactions inserted");

        // Insert some hard coded managers
        List<Manager> managers = managerDataService.fetchData();
        for (Manager manager : managers) {
            try {
                String managerPassword = generateManagerPassword(manager.getManagerID());
                managerRepository.insert(connection, manager, managerPassword);
            }
            catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new SQLException("Unable to insert manager");
            }
        }
        logger.info("Managers inserted");

        // Insert Rewards
        List<Reward> rewards = rewardDataService.fetchData();
        for (Reward reward : rewards) {
            rewardRepository.insert(connection, reward);
        }
        logger.info("Rewards inserted");
    }
}
