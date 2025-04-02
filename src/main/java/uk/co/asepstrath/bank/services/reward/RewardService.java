package uk.co.asepstrath.bank.services.reward;

import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.BaseService;
import uk.co.asepstrath.bank.services.repository.RewardRepository;

import javax.sql.DataSource;

public class RewardService extends BaseService {
    protected RewardRepository rewardRepository;

    public RewardService(DataSource dataSource, Logger logger, RewardRepository rewardRepository) {
        super(dataSource, logger);
        this.rewardRepository = rewardRepository;
    }
}
