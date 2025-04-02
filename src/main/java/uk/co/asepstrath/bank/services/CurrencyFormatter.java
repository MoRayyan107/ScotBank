package uk.co.asepstrath.bank.services;

import java.math.BigDecimal;

/**
 * Interface for formatting currency values
 */
public interface CurrencyFormatter {
    /**
     * Formats a BigDecimal amount as a currency string
     *
     * @param amount The amount to format
     * @return The formatted currency string
     */
    String formatCurrency(BigDecimal amount);
}
