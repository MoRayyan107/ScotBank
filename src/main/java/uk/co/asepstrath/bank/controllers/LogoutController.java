package uk.co.asepstrath.bank.controllers;

import io.jooby.Context;
import io.jooby.annotation.GET;
import io.jooby.annotation.Path;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.logout.LogoutService;

import static uk.co.asepstrath.bank.Constants.ROUTE_LOGOUT;

@Path(ROUTE_LOGOUT)
public class LogoutController extends BaseController {

    private final LogoutService logoutService;

    public LogoutController(Logger logger) {
        super(logger);
        logoutService = new LogoutService(logger);
    }

    @GET
    public void logout(Context ctx) {
        logoutService.logout(ctx);
    }
}
