package uk.co.asepstrath.bank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {

    private final String cardNumber;
    private final String cvv;

    @JsonCreator
    public Card(
            @JsonProperty("number") String cardNumber,
            @JsonProperty("cvv") String cvv // Corrected JSON property name
    ) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
    }

    // Getters (optional, but useful for serialization)
    public String getCardNumber() {
        return cardNumber;
    }

    public String getCvv() {
        return cvv;
    }
}
