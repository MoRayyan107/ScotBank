package uk.co.asepstrath.bank;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public class SanctionedBusinessReport {
    private final Business business;
    private final int totalTransactions;
    private final BigDecimal totalAmount;
    private final DateTime firstTransactionDate;
    private final DateTime lastTransactionDate;
    private final int acceptedTransactions;
    private final int rejectedTransactions;

    public SanctionedBusinessReport(Business business, int totalTransactions,
                                    BigDecimal totalAmount, DateTime firstDate,
                                    DateTime lastDate, int accepted, int rejected) {
        this.business = business;
        this.totalTransactions = totalTransactions;
        this.totalAmount = totalAmount;
        this.firstTransactionDate = firstDate;
        this.lastTransactionDate = lastDate;
        this.acceptedTransactions = accepted;
        this.rejectedTransactions = rejected;
    }

    public Business getBusiness() {
        return business;
    }

    public String getBusinessId() {
        return business.getID();
    }
    public String getBusinessName() {
        return business.getName();
    }

    public String getCategory() {
        return business.getCategory();
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public DateTime getFirstTransactionDate() {
        return firstTransactionDate;
    }

    public DateTime getLastTransactionDate() {
        return lastTransactionDate;
    }

    public int getAcceptedTransactions() {
        return acceptedTransactions;
    }

    public int getRejectedTransactions() {
        return rejectedTransactions;
    }
}
