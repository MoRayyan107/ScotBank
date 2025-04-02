package uk.co.asepstrath.bank;


/**
 * The Business class
 */
public class Business {

    private final String id;
    private final String name;
    private final String category;
    private final boolean sanctioned;

    public Business(String id, String name, String category, boolean sanctioned) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.sanctioned = sanctioned;
    }

    /**
     * Gets the Unique ID of the Business
     *
     * @return The Business ID
     */
    public String getID() {
        return id;
    }

    /**
     * Gets the name of the Business
     *
     * @return The Business name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the category of the Business
     *
     * @return The business category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the boolean value of sanctioned
     *
     * @return Sanctioned
     */
    public boolean isSanctioned() {
        return sanctioned;
    }
}
