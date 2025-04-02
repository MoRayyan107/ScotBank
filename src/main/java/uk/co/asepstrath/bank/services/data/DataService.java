package uk.co.asepstrath.bank.services.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.BaseService;

import javax.sql.DataSource;

public class DataService extends BaseService {

    protected final ObjectMapper objectMapper;
    protected UnirestWrapper unirestWrapper;

    public DataService(Logger logger, UnirestWrapper unirestWrapper, ObjectMapper objectMapper, DataSource dataSource) {
        super(dataSource, logger);
        this.unirestWrapper = unirestWrapper;
        this.objectMapper = objectMapper;
    }
}