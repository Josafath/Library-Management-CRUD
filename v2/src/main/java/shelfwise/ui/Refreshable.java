package shelfwise.ui;

/**
 * A tab that can reload its data from the database (called when the tab is
 * shown, so a loan made in one tab is reflected in the Books tab).
 */
public interface Refreshable {

    void refresh();
}
