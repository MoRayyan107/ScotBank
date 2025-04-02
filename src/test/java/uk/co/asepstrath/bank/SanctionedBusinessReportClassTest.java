package uk.co.asepstrath.bank;

import org.joda.time.DateTime;
import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;

public class SanctionedBusinessReportClassTest {
    @Test
    public void testConstructorAndGetters() {
        // Arrange
        Business mockBusiness = new Business("MAC", "Macdonalds", "Eating out", true);
        DateTime firstDate = new DateTime(2023, 1, 1, 0, 0);
        DateTime lastDate = new DateTime(2023, 12, 31, 0, 0);
        BigDecimal totalAmount = new BigDecimal("10000.50");

        // Act
        SanctionedBusinessReport report = new SanctionedBusinessReport(
                mockBusiness,
                10,
                totalAmount,
                firstDate,
                lastDate,
                8,
                2
        );

        // Assert
        assertEquals(mockBusiness, report.getBusiness());
        assertEquals("MAC", report.getBusinessId());
        assertEquals("Macdonalds", report.getBusinessName());
        assertEquals("Eating out", report.getCategory());
        assertTrue(mockBusiness.isSanctioned());
        assertEquals(10, report.getTotalTransactions());
        assertEquals(totalAmount, report.getTotalAmount());
        assertEquals(firstDate, report.getFirstTransactionDate());
        assertEquals(lastDate, report.getLastTransactionDate());
        assertEquals(8, report.getAcceptedTransactions());
        assertEquals(2, report.getRejectedTransactions());
    }

    @Test
    public void testReportWithZeroTransactions() {
        // Arrange
        Business mockBusiness = new Business("MAC", "Macdonalds", "Eating out", true);
        DateTime singleDate = new DateTime(2023, 6, 15, 0, 0);

        // Act
        SanctionedBusinessReport report = new SanctionedBusinessReport(
                mockBusiness,
                0,
                BigDecimal.ZERO,
                singleDate,
                singleDate,
                0,
                0
        );

        // Assert
        assertEquals(0, report.getTotalTransactions());
        assertEquals(BigDecimal.ZERO, report.getTotalAmount());
        assertEquals(0, report.getAcceptedTransactions());
        assertEquals(0, report.getRejectedTransactions());
    }
}