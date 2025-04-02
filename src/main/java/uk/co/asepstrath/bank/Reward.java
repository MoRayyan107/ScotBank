package uk.co.asepstrath.bank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Reward {
    private final String name;
    private final String description;
    private final BigDecimal value;
    private final double chance;


    @JsonCreator
    public Reward(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("value") BigDecimal value,
            @JsonProperty("chance") double chance
    ) {
        this.name = name;
        this.description = description;
        this.value = value;
        this.chance = chance;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public double getChance() {
        return this.chance;
    }

    @Override
    public String toString() {
        return String.format("name: %s%ndescription: %s%nvalue: %s%nchance: %s%n",
                getName(), getDescription(), getValue(), getChance());

    }

    public String toJson(String accountId) {
        return String.format("{\"rewardType\": \"%s\", \"team\": \"%s\", \"account\": \"%s\"}",
                getName(), "Team05", accountId);
    }


}
