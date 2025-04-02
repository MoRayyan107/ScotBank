package uk.co.asepstrath.bank.services.manager;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.DataAccessException;
import uk.co.asepstrath.bank.SanctionedBusinessReport;
import uk.co.asepstrath.bank.services.repository.ManagerRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.co.asepstrath.bank.Constants.*;

/**
 * The manager dashboard viewing service
 */
public class ViewManagerDashboardService extends ManagerService {

    private final ManagerRepository managerRepository;

    public ViewManagerDashboardService(DataSource datasource, Logger logger) {
        super(datasource, logger);
        managerRepository = new ManagerRepository(logger);
    }

    /**
     * Renders the manager's dashboard
     *
     * @return The "/manager/dashboard" endpoint
     */
    public ModelAndView<Map<String, Object>> renderDashboard(Context ctx) {
        try {
            ensureManagerIsLoggedIn(ctx);
            Map<String, Object> model = createModel();
            Session session = getSession(ctx);

            int page = ctx.query("page").intValue(1);
            int limit = ctx.query("limit").intValue(10);
            logger.info("Requesting page: {}", page);

            // Get every account from the database
            loadAccountsIntoModel(model, page, limit);

            model.put(SESSION_MANAGER_NAME, session.get(SESSION_MANAGER_NAME));
            model.put(SESSION_MANAGER_ID, session.get(SESSION_MANAGER_ID));

            // Get details of the top ten accounts with the highest summed PAYMENT amounts
            List<Map<String, Object>> topSpenders = managerRepository.getTopTenSpenders(getConnection());
            model.put(BIG_SPENDERS_LIST, topSpenders);
            model.put(BIG_SPENDERS_LIST_EXISTS, !topSpenders.isEmpty());

            // Get sanctioned business reports
            List<SanctionedBusinessReport> reports =
                    managerRepository.getSanctionedBusinessReports(getConnection());
            model.put(SANCTIONED_BUSINESSES_LIST, reports);
            model.put(SANCTIONED_BUSINESSES_LIST_EXISTS, !reports.isEmpty());



            // Put Peter's API key into the map
            model.put("api-maps-key", MAPS_API_KEY);



            return render(TEMPLATE_MANAGER_DASHBOARD, model);
        }
        catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve accounts", e);
        }
    }

    private void loadAccountsIntoModel(Map<String, Object> model, int page, int limit) throws SQLException {
        int totalAccounts = managerRepository.getCountOfAccounts(getConnection());
        int totalPages = (int) Math.ceil((double) totalAccounts / (double) limit);
        int offset = Math.max(0, (page - 1) * limit);

        // Not proud of this
        List<Account> accounts = managerRepository.getPaginatedAccounts(getConnection(), offset, limit);
        formatAccountBalancesForDisplay(model, accounts);
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("totalPages", totalPages);
        pagination.put("hasNext", page < totalPages);
        pagination.put("hasPrev", page > 1);
        model.put("pagination", pagination);


        logger.info("Pagination data - Page: {}, Total Pages: {}", page, totalPages);
        logger.info("Pagination Model: {}", model.get("pagination"));
        logger.info("Setting pagination: currentPage={}, totalPages={}", page, totalPages);
    }

    /**
     * Formats account balances for display in the model (without modifying Account objects)
     */
    private void formatAccountBalancesForDisplay(Map<String, Object> model, List<Account> accounts) {
        List<Map<String, Object>> displayAccounts = new ArrayList<>();

        for (Account account : accounts) {

            Map<String, Object> displayAx = new HashMap<>();
            String formattedBalance = formatCurrency(account.getBalance());

            // Add the formatted balance to the model, not modifying the Account object
            displayAx.put("accountid", account.getAccountID());
            displayAx.put("name", account.getName());
            displayAx.put("balance", formattedBalance);
            displayAx.put(ACCOUNT_CARD_NUMBER, account.getCard().getCardNumber());
            displayAx.put(ACCOUNT_CARD_CVV, account.getCard().getCvv());
            displayAccounts.add(displayAx);
        }
        model.put(ACCOUNT_OBJECT_LIST_EXISTS, !displayAccounts.isEmpty());
        model.put(ACCOUNT_OBJECT_LIST, displayAccounts);
    }

}
