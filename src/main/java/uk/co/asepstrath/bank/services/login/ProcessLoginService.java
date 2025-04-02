package uk.co.asepstrath.bank.services.login;

import io.jooby.Context;
import io.jooby.Session;
import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Card;
import uk.co.asepstrath.bank.services.BaseService;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static uk.co.asepstrath.bank.Constants.*;

/**
 * The login process service
 */
public class ProcessLoginService extends BaseService {

    public ProcessLoginService(DataSource dataSource, Logger logger) {
        super(dataSource, logger);
    }

    public void processLogin(Context ctx) {
        String formID = getFormValue(ctx, "accountid");
        String password = getFormValue(ctx, "password");

        // Check for empty accountID
        if (formID == null || formID.trim().isEmpty()) {
            addMessageToSession(ctx, SESSION_ERROR_MESSAGE, "Account ID cannot be empty.");
            redirect(ctx, ROUTE_LOGIN);
            return;
        }

        // Check for empty password
        if (password == null || password.trim().isEmpty()) {
            addMessageToSession(ctx, SESSION_ERROR_MESSAGE, "Password cannot be empty.");
            redirect(ctx, ROUTE_LOGIN);
            return;
        }

        try {
            Account account = authenticateUser(formID, password);

            if (account != null) {
                // Authentication successful - set up session and redirect
                createUserSession(ctx, account);
                redirect(ctx, ROUTE_ACCOUNT);
            }
            else {
                // Authentication failed
                addMessageToSession(ctx, SESSION_ERROR_MESSAGE, "Incorrect username or password.");
                redirect(ctx, ROUTE_LOGIN);
            }
        }
        catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Database error: {}", e.getMessage(), e);
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Failed to reach database");
        }
    }

    /**
     * Authenticates a user by ID and password
     *
     * @return Account if authenticated, null otherwise
     */
    private Account authenticateUser(String accountId, String password) throws SQLException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT AccountID, Password, Name, Balance, RoundUpEnabled, CardNumber, CardCVV FROM Accounts WHERE " +
                        "AccountID=?")) {

            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                logger.info("Processing account login");

                if (rs.next()) {
                    String hashedPassword = rs.getString("Password");
                    if (HashingPasswordService.verifyPassword(password, hashedPassword)) {
                        logger.info("Found account");
                        return new Account(rs.getString("AccountID"), rs.getString("Name"), rs.getBigDecimal("Balance"),
                                rs.getBoolean("RoundUpEnabled"), new Card(
                                rs.getString("CardNumber"),
                                rs.getString("CardCVV")
                        ));
                    }
                }
                return null;
            }
        }
    }

    /**
     * Creates a session for an authenticated user
     */
    private void createUserSession(Context ctx, Account account) {
        Session session = ctx.session();
        session.put(SESSION_ACCOUNT_ID, account.getAccountID());
        session.put(SESSION_ACCOUNT_NAME, account.getName());
    }
}