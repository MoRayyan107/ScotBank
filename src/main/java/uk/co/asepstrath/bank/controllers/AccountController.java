package uk.co.asepstrath.bank.controllers;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.Path;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.account.AccountDepositService;
import uk.co.asepstrath.bank.services.account.AccountRoundUpService;
import uk.co.asepstrath.bank.services.account.AccountViewService;
import uk.co.asepstrath.bank.services.account.AccountWithdrawService;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

import static uk.co.asepstrath.bank.Constants.*;

/**
 * The Account endpoint controller
 */
@Path(ROUTE_ACCOUNT)
public class AccountController extends BaseController {

    private final AccountViewService viewAccountService;
    private final AccountDepositService depositService;
    private final AccountWithdrawService withdrawService;
    private final AccountRoundUpService roundUpService;

    public AccountController(DataSource datasource, Logger logger) {
        super(logger);
        viewAccountService = new AccountViewService(datasource, logger);
        depositService = new AccountDepositService(datasource, logger);
        withdrawService = new AccountWithdrawService(datasource, logger);
        roundUpService = new AccountRoundUpService(datasource, logger);
    }

    /**
     * Renders the account page
     *
     * @param ctx Session context
     * @return The "/account" endpoint
     * @throws SQLException Failed to find an account with the session account identifier
     */
    @GET
    public ModelAndView<Map<String, Object>> viewAccount(Context ctx) throws SQLException {
        return viewAccountService.viewAccount(ctx);
    }

    /**
     * Renders the deposit page
     *
     * @param ctx Session context
     * @return The "/deposit" endpoint
     */
    @GET
    @Path(ROUTE_DEPOSIT)
    public ModelAndView<Map<String, Object>> deposit(Context ctx) {
        return depositService.renderDeposit(ctx);
    }

    /**
     * Renders the withdrawal page
     *
     * @param ctx Session context
     * @return The "/account/withdraw" endpoint
     */
    @GET
    @Path(ROUTE_WITHDRAW)
    public ModelAndView<Map<String, Object>> withdraw(Context ctx) {
        return withdrawService.renderWithdraw(ctx);
    }

    /**
     * Engages the withdrawal process
     *
     * @param ctx Session Context
     *            Redirects to "/account" on success
     *            Redirects to "/withdraw" on failure
     * @throws SQLException on withdrawal failure
     */
    @POST
    @Path(ROUTE_WITHDRAW + ROUTE_PROCESS)
    public void withdrawProcess(Context ctx) throws SQLException {
        withdrawService.processWithdraw(ctx);
    }

    /**
     * Engages the deposit process
     *
     * @param ctx Session Context
     *            Redirects to "/account" on success
     *            Redirects to "/deposit" on failure
     * @throws SQLException on deposit failure
     */
    @POST
    @Path(ROUTE_DEPOSIT + ROUTE_PROCESS)
    public void depositProcess(Context ctx) throws SQLException {
        depositService.processDeposit(ctx);
    }

    // Enable Round Up for the account
    @POST
    @Path(ROUTE_ROUND_UP_ON)
    public void enableRoundUp(Context ctx) throws SQLException {
        roundUpService.enableDatabaseRoundUp(ctx);
    }

    // Disable Round Up for the account
    @POST
    @Path(ROUTE_ROUND_UP_OFF)
    public void disableDatabaseRoundUp(Context ctx) throws SQLException {
        roundUpService.disableDatabaseRoundUp(ctx);
    }

    @POST
    @Path(ROUTE_RECLAIM)
    public void reclaimSavings(Context ctx) throws SQLException{
        roundUpService.reclaimSavings(ctx);
    }
}
