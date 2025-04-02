package uk.co.asepstrath.bank.services.reward;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jooby.Context;
import io.jooby.Session;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Reward;
import uk.co.asepstrath.bank.services.data.RewardDataService;
import uk.co.asepstrath.bank.services.data.UnirestWrapper;
import uk.co.asepstrath.bank.services.repository.RewardRepository;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;

import static uk.co.asepstrath.bank.Constants.*;

public class RewardSpinService extends RewardService {

    private final RewardDataService rewardDataService;
    private final SecureRandom secureRandom;

    public RewardSpinService(DataSource datasource, Logger logger, RewardRepository rewardRepository) {
        super(datasource, logger, rewardRepository);
        this.rewardDataService = new RewardDataService(logger, new UnirestWrapper(), new ObjectMapper(), datasource);
        this.secureRandom = new SecureRandom();
    }

    /**
     * Process the user spinning the wheel (user MUST win a reward)
     */
    public void processSpin(Context ctx) throws SQLException {
        List<Reward> rewards = rewardRepository.getAllRewards(getConnection());
        Reward selected = selectWeightedRandomReward(rewards);
        assert selected != null; // This might fail on a flop error???

        Session session = getSession(ctx);
        String accountId = String.valueOf(session.get(SESSION_ACCOUNT_ID));
        try {
            rewardDataService.postReward(selected, accountId);
            addMessageToSession(ctx, SESSION_SUCCESS_MESSAGE,
                    "A " + selected.getName() + " has been added to your account");
        }
        catch (Exception e) {
            addMessageToSession(ctx, SESSION_ERROR_MESSAGE,
                    "An upstream error occurred while adding a reward to your account");
        }
        redirect(ctx, ROUTE_REWARD);
    }
    /**
     * Process the user spinning the wheel returning JSON (user MUST win a reward)
     */
    public void processSpinAPI(Context ctx) throws SQLException {
        List<Reward> rewards = rewardRepository.getAllRewards(getConnection());
        Reward selected = selectWeightedRandomReward(rewards);

        Session session = getSession(ctx);
        String accountId = String.valueOf(session.get(SESSION_ACCOUNT_ID));

        try {
            rewardDataService.postReward(selected, accountId);
            ctx.setResponseType("application/json");
            ctx.send("{\"reward\":\"" + selected.getName() + "\"}");
        } catch (Exception e) {
            ctx.setResponseCode(500).send("{\"error\": \"Failed to assign reward.\"}");
        }
    }

    /**
     * Selects a reward based on weighted probability
     */
    Reward selectWeightedRandomReward(List<Reward> rewards) {
        double totalWeight = 0.0;
        for (Reward reward : rewards) {
            totalWeight += reward.getChance();
        }

        double random = secureRandom.nextDouble() * totalWeight;
        double weightSum = 0.0;
        for (Reward reward : rewards) {
            weightSum += reward.getChance();
            if (random < weightSum) {
                return reward;
            }
        }

        return rewards.getLast();
    }
}
