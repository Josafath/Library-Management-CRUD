package shelfwise.service;

import java.util.regex.Pattern;

/**
 * Checks shared by the services. Each one returns the cleaned value or
 * throws a ValidationException with a message for the user.
 */
final class Validation {

    // Something@something.something, without spaces: enough to catch typos
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    // Letters, digits, dot, dash and underscore; 3 to 40 characters
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,40}$");

    /** Minimum password length for staff accounts. */
    static final int MIN_PASSWORD_LENGTH = 8;

    private Validation() {
        // Utility class: no instances
    }

    /** Trims the text and checks it is not empty nor longer than the column. */
    static String requireText(String value, String field, int maxLength) {
        String clean = value == null ? "" : value.strip();
        if (clean.isEmpty()) {
            throw new ValidationException(field + " is required.");
        }
        if (clean.length() > maxLength) {
            throw new ValidationException(field + " can't be longer than " + maxLength + " characters.");
        }
        return clean;
    }

    /** Like requireText, but an empty value is allowed. */
    static String optionalText(String value, String field, int maxLength) {
        String clean = value == null ? "" : value.strip();
        if (clean.length() > maxLength) {
            throw new ValidationException(field + " can't be longer than " + maxLength + " characters.");
        }
        return clean;
    }

    static String requireEmail(String value) {
        String clean = requireText(value, "Email", 120);
        if (!EMAIL.matcher(clean).matches()) {
            throw new ValidationException("Email doesn't look valid (example: ana@example.com).");
        }
        return clean;
    }

    static String requireUsername(String value) {
        String clean = value == null ? "" : value.strip();
        if (!USERNAME.matcher(clean).matches()) {
            throw new ValidationException("Username must be 3-40 characters: letters, digits, '.', '-' or '_'.");
        }
        return clean;
    }

    static int requireRange(int value, String field, int min, int max) {
        if (value < min || value > max) {
            throw new ValidationException(field + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    static void requirePassword(char[] password) {
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password must have at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }
}
