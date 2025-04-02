package uk.co.asepstrath.bank.controllers;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.Path;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.login.DisplayManagerLoginService;
import uk.co.asepstrath.bank.services.login.ProcessManagerLoginService;
import uk.co.asepstrath.bank.services.manager.ViewManagerDashboardService;

import javax.sql.DataSource;
import java.util.Map;

import static uk.co.asepstrath.bank.Constants.*;


/**
 * The Manager endpoint Controller
 */
@Path(ROUTE_MANAGER)
public class ManagerController extends BaseController {

    private final ViewManagerDashboardService viewManagerDashboardService;
    private final DisplayManagerLoginService displayManagerLoginService;
    private final ProcessManagerLoginService processManagerLoginService;

    public ManagerController(DataSource dataSource, Logger logger) {
        super(logger);
        viewManagerDashboardService = new ViewManagerDashboardService(dataSource, logger);
        displayManagerLoginService = new DisplayManagerLoginService(logger);
        processManagerLoginService = new ProcessManagerLoginService(dataSource, logger);
    }

    /**
     * Displays all accounts on the system
     *
     * @return The "/dashboard" endpoint
     */
    @GET
    @Path(ROUTE_DASHBOARD)
    public ModelAndView<Map<String, Object>> displayManagerDashboard(Context ctx) {
        return viewManagerDashboardService.renderDashboard(ctx);
    }

    /**
     * Displays the manager login endpoint
     *
     * @return The "/manager/login" endpoint
     */
    @GET
    @Path(ROUTE_LOGIN)
    public ModelAndView<Map<String, Object>> displayManagerLogin(Context ctx) {
        return displayManagerLoginService.displayManagerLogin(ctx);
    }

    /**
     * Engages the manager login process
     *
     * @param ctx Session context
     * @return The "/manager/login" endpoint on failure
     * Redirects to "/manager/dashboard" on success
     */
    @POST
    @Path(ROUTE_LOGIN + ROUTE_PROCESS)
    public ModelAndView<Map<String, Object>> processManagerLogin(Context ctx) {
        return processManagerLoginService.processManagerLogin(ctx);
    }

}
