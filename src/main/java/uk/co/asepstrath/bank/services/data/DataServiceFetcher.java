package uk.co.asepstrath.bank.services.data;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;

/**
 * Fetches data from an external API
 *
 * @param <T> the object that is being generated
 */
public interface DataServiceFetcher<T> {
    List<T> fetchData() throws IOException, XMLStreamException;
}
