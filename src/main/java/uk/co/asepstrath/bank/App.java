package uk.co.asepstrath.bank;

import com.github.jknack.handlebars.Handlebars;
import io.jooby.Jooby;
import io.jooby.Session;
import io.jooby.StatusCode;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;
import io.jooby.jackson.JacksonModule;
import io.jooby.netty.NettyServer;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.controllers.*;
import uk.co.asepstrath.bank.services.login.DisplayLoginService;
import uk.co.asepstrath.bank.services.login.ProcessLoginService;
import uk.co.asepstrath.bank.services.repository.DatabaseManager;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.SQLException;

import static uk.co.asepstrath.bank.Constants.*;

public class App extends Jooby {

    public App() {

        // Redirect the landing page to /login, if the user is not logged in
        landingPageRedirect();

        // Apply authentication to certain routes, using session variables as keys
        authenticateRoute();

        // Handle status code errors
        handleRouteErrors();

        // Install the required extensions
        installExtensions();

        // Load all CSS / JS / media
        loadServerAssets();

        // Set up controllers and their dependencies
        setUpControllers();

        // Add helper functions for handlebars template to be able to paginate
        handleHandlebarsPagination();

        onStarted(this::onStart);
        onStop(this::onStop);
    }


    public static void main(final String[] args) {
        runApp(args, App::new);
    }

    private void setUpControllers() {
    /*
    Set up controllers and their dependencies
     */
        DataSource ds = require(DataSource.class);
        Logger logger = getLog();

        DisplayLoginService displayLoginService = new DisplayLoginService(logger);
        ProcessLoginService processLoginService = new ProcessLoginService(ds, logger);

        // Register controllers with their dependencies
        mvc(new AccountController_(ds, logger));
        mvc(new LoginController_(displayLoginService, processLoginService, logger));
        mvc(new ManagerController_(ds, logger));
        mvc(new LogoutController_(logger));
        mvc(new ErrorController_(logger));
        mvc(new RewardController_(ds, logger));
    }

    /*
    This will host any files in src/main/resources/assets on <host>/assets
     */
    private void loadServerAssets() {

        assets("/css/*", "/css");
        assets("/assets/*", "/assets");
        assets("/service_worker.js", "/service_worker.js");
        assets("/transaction-chart.js", "/transaction-chart.js");
        assets("/mapEmbed.js", "/mapEmbed.js");
        // Handle favicon inconsistencies between Windows and Linux
        assets("/favicon.svg", "/assets/images/favicon.svg");
        get("/favicon.ico", ctx -> ctx.sendRedirect("/favicon.svg"));
    }

    /*
    Serve static assets from src/main/resources
    */
    private void installExtensions() {

        install(new NettyServer());
        install(new UniRestExtension());
        install(new HandlebarsModule());
        install(new HikariModule("mem"));
        install(new JacksonModule()); // Handle JSON requests
    }

    private void authenticateRoute() {
        before(ctx -> {
            String path = ctx.getRequestPath();
            // Allow access to static resources (CSS, JS, favicon)
            if (path.startsWith("/css") || path.endsWith("/js") || path.endsWith(".ico")) {
                return;
            }

            // Retrieve session details
            Session session = ctx.sessionOrNull();
            boolean userLoggedIn = session != null && session.get(SESSION_ACCOUNT_NAME).isPresent() && session.get(
                    SESSION_ACCOUNT_ID).isPresent();
            boolean managerLoggedIn = session != null && session.get(SESSION_MANAGER_NAME).isPresent() && session.get(
                    SESSION_MANAGER_ID).isPresent();

            // If user is not logged in and attempting to access a restricted page, redirect to log in
            if (!userLoggedIn && !managerLoggedIn && !path.startsWith(ROUTE_LOGIN)
                    && !path.startsWith("/manager/login")) {
                ctx.setResponseCode(401).sendRedirect("/login");
            }


        });
    }

    private void landingPageRedirect() {
        // Landing page route - redirect based on user type
        get("/", ctx -> {
            // Retrieve session information (if available)
            Session session = ctx.sessionOrNull();
            boolean managerLoggedIn = session != null && session.get(SESSION_MANAGER_NAME).isPresent() && session.get(
                    SESSION_MANAGER_ID).isPresent();

            // Redirect managers to their dashboard, otherwise send users to the account page
            if (managerLoggedIn) {
                ctx.sendRedirect(ROUTE_MANAGER + ROUTE_DASHBOARD);
            }
            else {
                ctx.sendRedirect(ROUTE_ACCOUNT); // Regular user account page if manager is not logged in
            }
            return null;
        });
    }

    private void handleRouteErrors() {
        // Global error handling for various HTTP status codes
        // Unbelievable that I cannot switch on this
        error((ctx, cause, code) -> {
            getLog().error("Error occurred: {}, Status code: {}", cause.getMessage(), code, cause);
            // Redirect users to appropriate error pages based on status code
            if (code == StatusCode.FORBIDDEN) {
                ctx.setResponseCode(403).sendRedirect(ROUTE_ERROR + ROUTE_403_FORBIDDEN);
            }
            else if (code == StatusCode.NOT_FOUND) {
                ctx.setResponseCode(404).sendRedirect(ROUTE_ERROR + ROUTE_404_NOT_FOUND);
            }
            else if (code == StatusCode.METHOD_NOT_ALLOWED) {
                ctx.setResponseCode(405).sendRedirect(ROUTE_ERROR + ROUTE_405_METHOD_NOT_ALLOWED);
            }
            else if (code == StatusCode.SERVER_ERROR) {
                ctx.setResponseCode(505).sendRedirect(ROUTE_ERROR + ROUTE_505_SERVER_ERROR);
            }
            else {
                ctx.sendRedirect(ROUTE_ERROR + ROUTE_GENERIC_ERROR);
            }
        });
    }

    /*
    This function will be called when the application starts up,
     */
    public void onStart() throws SQLException, XMLStreamException, IOException {
        Logger logger = getLog();
        logger.info("Starting Up...");

        // Retrieve the database connection
        DataSource dataSource = require(DataSource.class);

        // Create Database and tables with initial data
        DatabaseManager databaseManager = new DatabaseManager(dataSource, logger);
        databaseManager.initialise();
    }

    public void onStop() {
        Logger logger = getLog();
        logger.info("Shutting Down...");
    }

    private void handleHandlebarsPagination() {
        Handlebars handlebars = require(Handlebars.class);

        handlebars.registerHelper("increment", (context, options) -> {
            if (context instanceof Number number) {
                return number.intValue() + 1;
            }
            return null;
        });

        handlebars.registerHelper("decrement", (context, options) -> {
            if (context instanceof Number number) {
                return number.intValue() - 1;
            }
            return null;
        });
    }
}
