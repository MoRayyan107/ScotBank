package uk.co.asepstrath.bank.services.login;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Manager;
import uk.co.asepstrath.bank.services.BaseService;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static uk.co.asepstrath.bank.Constants.*;

/**
 * The manager login process service
 */
public class ProcessManagerLoginService extends BaseService {

    public ProcessManagerLoginService(DataSource dataSource, Logger logger) {
        super(dataSource, logger);
    }

    /**
     * Processes the manager login
     *
     * @param ctx Session context
     * @return the "/manager/login" endpoint on failure
     * Redirects to "/manager/dashboard" on success
     */
    public ModelAndView<Map<String, Object>> processManagerLogin(Context ctx) {

        // Validate manager form input
        String formManagerID = getFormValue(ctx, "managerid");
        String password = getFormValue(ctx, "password");
        if (formManagerID == null || formManagerID.trim().isEmpty()) {
            addMessageToSession(ctx, SESSION_ERROR_MESSAGE, "Account ID cannot be empty.");
            redirect(ctx, ROUTE_MANAGER + ROUTE_LOGIN);
            return null;
        }

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT ManagerID, Name, Password FROM Managers WHERE ManagerID = ?")
        ) {
            stmt.setString(1, formManagerID);

            Manager manager = null;
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("Password");
                    // Compare hashed database password to hashed user input password
                    if (HashingPasswordService.verifyPassword(password, hashedPassword)) {

                        // Create an instance of Manager if successful
                        manager = new Manager(
                                rs.getString("ManagerID"),
                                rs.getString("Name")
                        );

                        createManagerSession(ctx, manager);
                        logger.info("Process Manager Login Success");
                        redirect(ctx, ROUTE_MANAGER + ROUTE_DASHBOARD);
                        return null;
                    }
                }
                else {
                    // Details wrong
                    handleLoginFailure(ctx, "Invalid account ID.");
                    return null;
                }
            }
        }
        catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Database error during manager login", e);
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "A database error occurred");
        }
        return null;
    }

    private void handleLoginFailure(Context ctx, String message) {
        addMessageToSession(ctx, SESSION_ERROR_MESSAGE, message);
        redirect(ctx, ROUTE_MANAGER + ROUTE_LOGIN);
    }

    private void createManagerSession(Context ctx, Manager manager) {
        Session session = ctx.session();
        session.put(SESSION_MANAGER_ID, manager.getManagerID());
        session.put(SESSION_MANAGER_NAME, manager.getName());
    }
}
