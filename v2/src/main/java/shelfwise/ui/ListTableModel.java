package shelfwise.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.table.AbstractTableModel;

/**
 * A read-only JTable model backed by a list of objects.
 *
 * v1 copied every row into a String[][] and then guessed the database id
 * from the row number. Here each row *is* the object (Book, Member, ...),
 * so the selected row gives back the real item with its real id.
 *
 * @param <T> type of the items shown, one per row
 */
public final class ListTableModel<T> extends AbstractTableModel {

    /**
     * One column: header text, value type (for sorting) and how to read the value.
     */
    public record Column<T>(String header, Class<?> type, Function<T, Object> value) {

        /** A text column. */
        public static <T> Column<T> text(String header, Function<T, Object> value) {
            return new Column<>(header, String.class, value);
        }

        /** A numeric column (sorts as numbers, not as text). */
        public static <T> Column<T> number(String header, Function<T, Object> value) {
            return new Column<>(header, Long.class, value);
        }
    }

    private final List<Column<T>> columns;
    private List<T> items = new ArrayList<>();

    public ListTableModel(List<Column<T>> columns) {
        this.columns = List.copyOf(columns);
    }

    /** Replaces every row and repaints the table. */
    public void setItems(List<T> newItems) {
        this.items = new ArrayList<>(newItems);
        fireTableDataChanged();
    }

    /** The item shown in the given model row. */
    public T getItem(int modelRow) {
        return items.get(modelRow);
    }

    /** Every item currently shown. */
    public List<T> getItems() {
        return List.copyOf(items);
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).header();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columns.get(column).type();
    }

    @Override
    public Object getValueAt(int row, int column) {
        return columns.get(column).value().apply(items.get(row));
    }

    // Cells are never edited in place; changes go through the form next to the table
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
