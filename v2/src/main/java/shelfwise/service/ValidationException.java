package shelfwise.service;

/**
 * A rule was broken by what the user typed or asked for ("Title is
 * required", "This book is already on loan", ...). The message is written
 * for the user and the UI shows it as is.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
