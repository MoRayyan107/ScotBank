package uk.co.asepstrath.bank.services.data;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.core.HttpResponse;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetches Account data from external API
 */
public class AccountDataService extends DataService implements DataServiceFetcher<Account> {

    public AccountDataService(Logger logger, UnirestWrapper unirestWrapper, ObjectMapper objectMapper,
                              DataSource dataSource) {
        super(logger, unirestWrapper, objectMapper, dataSource);
    }

    /**
     * Gets account data from the API
     *
     * @return List of Account objects
     * @throws IOException Failed to fetch data from API
     */
    @Override
    public List<Account> fetchData() throws IOException {
        try {

            // Get OAuth token
            Map<String, String> tokenHeaders = new HashMap<>();
            tokenHeaders.put("Content-Type", "application/x-www-form-urlencoded");

            // This is username:password encrypted in Base64
            tokenHeaders.put("Authorization", "Basic c2NvdGJhbms6dGhpczFwYXNzd29yZDJpczNub3Q0c2VjdXJl");
            HttpResponse<String> tokenResponse = unirestWrapper.post(
                    "https://api.asep-strath.co.uk/oauth2/token",
                    "grant_type=client_credentials",
                    tokenHeaders
            );

            // Parse response
            JsonNode jsonNode = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = jsonNode.get("access_token").asText();

            // Use token to get accounts
            Map<String, String> accountHeaders = new HashMap<>();
            accountHeaders.put("Authorization", "Bearer " + accessToken);

            // List wanted parameters
            Map<String, Object> params = new HashMap<>();
            params.put("include", "postcode,cardDetails");

            // Make GET request
            HttpResponse<String> response = unirestWrapper.get("https://api.asep-strath.co.uk/api/accounts", params,
                    accountHeaders);
            if (response.isSuccess()) {
                logger.info("Successfully retrieved account data");
                return objectMapper.readValue(response.getBody(), new TypeReference<>() {
                });
            }
            else {
                throw new JsonParseException(null, "Failed to fetch accounts: " + response.getStatus());
            }
        }
        catch (IOException e) {
            throw new JsonParseException(null, "Failed to parse account data");
        }
    }
}