package uk.co.asepstrath.bank.parsers;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import uk.co.asepstrath.bank.Transaction;

import java.util.List;

public class XmlParser {

    @JacksonXmlProperty(localName = "hasNext")
    private boolean hasNext;

    @JacksonXmlProperty(localName = "hasPrevious")
    private boolean hasPrevious;

    @JacksonXmlProperty(localName = "page")
    private int page;

    @JacksonXmlProperty(localName = "size")
    private int size;

    @JacksonXmlProperty(localName = "totalPages")
    private int totalPages;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "results")
    private List<Transaction> transactions;

    public boolean isHasNext() {
        return hasNext;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
