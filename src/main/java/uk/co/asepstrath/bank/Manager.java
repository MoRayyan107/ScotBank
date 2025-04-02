package uk.co.asepstrath.bank;

/**
 * The Manager class
 */
public class Manager {

    private final String managerID;
    private final String name;

    public Manager(String managerID, String name) {
        this.managerID = managerID;
        this.name = name;
    }

    /**
     * Gets the unique ID of the Manager
     *
     * @return ManagerID
     */
    public String getManagerID() {
        return managerID;
    }

    /**
     * Gets the name of the Manager
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a String interpretation of a Manager
     *
     * @return A String of the Manager
     */
    public String toString() {
        return String.format("id: %s%nname: %s",
                getManagerID(), getName());
    }
}
