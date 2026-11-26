package shelfwise.model;

/**
 * What a staff account is allowed to do.
 */
public enum Role {
    /** Manages staff accounts, plus everything a librarian can do. */
    ADMIN("Admin"),
    /** Manages books, members and loans. */
    LIBRARIAN("Librarian");

    // Text shown in the UI (the enum name is what goes to the database)
    private final String label;

    Role(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
