package shelfwise.ui;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

/**
 * Common JTable setup for every tab.
 */
final class Tables {

    private Tables() {
        // Utility class: no instances
    }

    /** Single selection, sortable headers, and a narrow ID column (the first one). */
    static void setUp(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);    // click a header to sort
        table.setFillsViewportHeight(true);    // clicking below the rows clears nothing odd
        TableColumn id = table.getColumnModel().getColumn(0);
        id.setPreferredWidth(45);              // ids are short; give the width to the text columns
        id.setMaxWidth(70);
    }
}
