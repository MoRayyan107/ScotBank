package uk.co.asepstrath.bank.services.manager;

import io.jooby.Context;
import io.jooby.Session;
import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.BaseService;

import javax.sql.DataSource;

import static uk.co.asepstrath.bank.Constants.SESSION_MANAGER_ID;
import static uk.co.asepstrath.bank.Constants.SESSION_MANAGER_NAME;

public class ManagerService extends BaseService {

    public ManagerService(DataSource dataSource, Logger logger) {
        super(dataSource, logger);
    }

    /**
     * Throws a 403 if a manager is not logged in
     *
     * @param ctx Session context
     */
    protected void ensureManagerIsLoggedIn(Context ctx) {
        Session session = getSession(ctx);
        if (session.get(SESSION_MANAGER_NAME).isMissing() || session.get(SESSION_MANAGER_ID).isMissing()) {
            throw new StatusCodeException(StatusCode.FORBIDDEN, "Manager access required");
        }
    }
}
