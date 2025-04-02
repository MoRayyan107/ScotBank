package uk.co.asepstrath.bank.services.data;

import uk.co.asepstrath.bank.Manager;

import java.util.List;

/**
 * Fetches some hard-coded managers
 */
public class ManagerDataService implements DataServiceFetcher<Manager> {

    /**
     * Generates a list of manager objects
     *
     * @return List of manager objects
     */
    @Override
    public List<Manager> fetchData() {
        return List.of(new Manager("admin0", "Oles Vynnychuk"), new Manager("admin1", "Mohammad Rayyan Adhoni"),
                new Manager("admin2", "Zara Warne"), new Manager("admin3", "Peter Tasker"),
                new Manager("admin4", "Jack Allones"));
    }
}
