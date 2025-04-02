package uk.co.asepstrath.bank.services.logout;

import io.jooby.Context;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.BaseService;

import static uk.co.asepstrath.bank.Constants.ROUTE_LOGIN;
import static uk.co.asepstrath.bank.Constants.SESSION_SUCCESS_MESSAGE;

/**
 * The log-out service
 */
public class LogoutService extends BaseService {
    public LogoutService(Logger logger) {
        super(logger);
    }

    /**
     * Logs the user out
     *
     * @param ctx Session context
     */
    public void logout(Context ctx) {
        ctx.session().destroy();
        addMessageToSession(ctx, SESSION_SUCCESS_MESSAGE, "Successfully logged out!");
        redirect(ctx, ROUTE_LOGIN);
    }
}
