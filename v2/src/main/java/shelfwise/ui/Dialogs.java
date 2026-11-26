package shelfwise.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import shelfwise.repository.DataAccessException;
import shelfwise.service.ValidationException;

/**
 * Message boxes, plus one place that turns exceptions into messages.
 *
 * v1 caught every SQLException and only logged it, so the user clicked a
 * button and nothing happened. Here every action goes through run(...).
 */
public final class Dialogs {

    private static final Logger LOG = Logger.getLogger(Dialogs.class.getName());

    private Dialogs() {
        // Utility class: no instances
    }

    /**
     * Runs a UI action and shows any problem to the user:
     * - ValidationException: a warning with the rule that was broken
     * - DataAccessException: a database error
     * - anything else: an unexpected error (also logged with its stack trace)
     *
     * @return true if the action finished without errors
     */
    public static boolean run(Component parent, Runnable action) {
        Component window = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        Component cursorOwner = window != null ? window : parent;
        Cursor before = cursorOwner == null ? null : cursorOwner.getCursor();
        if (cursorOwner != null) {
            cursorOwner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));   // hashing/DB calls can take a moment
        }
        try {
            action.run();
            return true;
        } catch (ValidationException ex) {
            warn(parent, ex.getMessage());
        } catch (DataAccessException ex) {
            LOG.log(Level.WARNING, "Database error", ex);
            error(parent, "Database error", ex.getMessage());
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Unexpected error", ex);
            error(parent, "Unexpected error", String.valueOf(ex));
        } finally {
            if (cursorOwner != null) {
                cursorOwner.setCursor(before);
            }
        }
        return false;
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Shelfwise", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Check this", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /** Yes/No question; returns true for Yes. */
    public static boolean confirm(Component parent, String question) {
        return JOptionPane.showConfirmDialog(parent, question, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
