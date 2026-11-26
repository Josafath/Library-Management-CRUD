package shelfwise.model;

/**
 * A library member: someone who can borrow books.
 *
 * @param id       database id; 0 means "not saved yet"
 * @param fullName member's name
 * @param email    unique contact email
 * @param phone    optional phone number ("" when unknown)
 */
public record Member(long id, String fullName, String email, String phone) {

    /** A member that hasn't been saved yet. */
    public static Member draft(String fullName, String email, String phone) {
        return new Member(0, fullName, email, phone);
    }

    /** Copy of this member with a database id (used after an insert). */
    public Member withId(long newId) {
        return new Member(newId, fullName, email, phone);
    }

    // Used by the "New loan" combo box
    @Override
    public String toString() {
        return fullName + " <" + email + ">";
    }
}
