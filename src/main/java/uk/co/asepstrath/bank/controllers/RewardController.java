package uk.co.asepstrath.bank.controllers;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.Path;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.repository.RewardRepository;
import uk.co.asepstrath.bank.services.reward.RewardSpinService;
import uk.co.asepstrath.bank.services.reward.RewardViewService;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

import static uk.co.asepstrath.bank.Constants.ROUTE_PROCESS;
import static uk.co.asepstrath.bank.Constants.ROUTE_REWARD;

@Path(ROUTE_REWARD)
public class RewardController extends BaseController {

    private final RewardViewService rewardViewService;
    private final RewardSpinService rewardSpinService;

    public RewardController(DataSource dataSource, Logger logger) {
        super(logger);
        RewardRepository rewardRepository = new RewardRepository(logger);
        this.rewardViewService = new RewardViewService(dataSource, logger, rewardRepository);
        this.rewardSpinService = new RewardSpinService(dataSource, logger, rewardRepository);
    }

    /**
     * Display the rewards page
     */
    @GET
    public ModelAndView<Map<String, Object>> viewRewardPage(Context ctx) throws SQLException {
        return rewardViewService.viewRewardPage(ctx);
    }

    /**
     * Handle the spin logic
     */
    @POST
    @Path(ROUTE_PROCESS)
    public void processRewardSpin(Context ctx) throws SQLException {
        rewardSpinService.processSpin(ctx);
    }
    /**
     * Handle the spin logic for frontend animation (returns JSON)
     */
    @POST
    @Path("/api")
    public void processRewardSpinAPI(Context ctx) throws SQLException {
        rewardSpinService.processSpinAPI(ctx);
    }
}
