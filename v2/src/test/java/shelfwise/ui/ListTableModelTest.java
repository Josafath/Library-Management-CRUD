package shelfwise.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;
import shelfwise.model.Book;
import static shelfwise.ui.ListTableModel.Column;

class ListTableModelTest {

    private final ListTableModel<Book> model = new ListTableModel<>(List.of(
            Column.<Book>number("ID", Book::id),
            Column.<Book>text("Title", Book::title)));

    @Test
    void rowsAreTheItemsThemselves() {
        Book first = new Book(7, "Pedro Páramo", "Rulfo", "Novel", "FCE", true);
        Book second = new Book(12, "1984", "Orwell", "Dystopia", "Secker", false);
        model.setItems(List.of(first, second));

        assertEquals(2, model.getRowCount());
        assertEquals(2, model.getColumnCount());
        assertEquals("Title", model.getColumnName(1));
        assertEquals(12L, model.getValueAt(1, 0));
        // v1 used "row + 1" as the id; here row 1 gives back the book with id 12
        assertEquals(second, model.getItem(1));
    }

    @Test
    void columnTypesAllowNumericSorting() {
        assertEquals(Long.class, model.getColumnClass(0));
        assertEquals(String.class, model.getColumnClass(1));
    }

    @Test
    void cellsAreReadOnly() {
        model.setItems(List.of(new Book(1, "T", "A", "G", "P", true)));
        assertFalse(model.isCellEditable(0, 1));
    }
}
