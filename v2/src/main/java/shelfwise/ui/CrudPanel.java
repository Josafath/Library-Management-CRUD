package shelfwise.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The screen shared by Books, Members and Users:
 *
 * <pre>
 * Search: [__________]
 * +-------------------------+  +-- Details ---------+
 * |  table of items         |  |  form              |
 * |                         |  |  [New] [Save] [Del]|
 * +-------------------------+  +--------------------+
 * </pre>
 *
 * Selecting a row loads it into the form (to edit it); "New" clears the form.
 * "Save" inserts or updates depending on that. Subclasses only describe
 * their columns, their form and which service methods to call.
 *
 * @param <T> type of item managed by the panel
 */
public abstract class CrudPanel<T> extends JPanel implements Refreshable {

    protected final ListTableModel<T> model;
    protected final JTable table;
    private final JTextField searchField = new JTextField(24);
    private final JLabel status = new JLabel(" ");
    private final JButton deleteButton = new JButton("Delete");
    private final String noun;   // "book", "member"... used in messages

    // The item being edited, or null while the form is for a new item
    private T editing;

    // Waits 250 ms after the last key before searching, instead of querying on every keystroke
    private final Timer searchDelay = new Timer(250, e -> refresh());

    protected CrudPanel(String noun, List<ListTableModel.Column<T>> columns) {
        super(new BorderLayout(8, 8));
        this.noun = noun;
        this.model = new ListTableModel<>(columns);
        this.table = new JTable(model);
        searchDelay.setRepeats(false);
    }

    /**
     * Builds the screen. Called by the subclass constructor once its own
     * fields exist (calling buildForm() from this constructor would run it
     * before the subclass fields are initialized).
     */
    protected final void initUi() {
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // --- Top: search box ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        top.add(new JLabel("Search:"));
        top.add(searchField);
        searchField.setToolTipText(searchHint());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchDelay.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchDelay.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDelay.restart(); }
        });
        add(top, BorderLayout.NORTH);

        // --- Left: the table ---
        Tables.setUp(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {          // ignore the intermediate events while dragging
                onSelectionChanged();
            }
        });

        // --- Right: the form and its buttons ---
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder(capitalize(noun) + " details"));
        right.add(buildForm(), BorderLayout.CENTER);

        JButton newButton = new JButton("New");
        JButton saveButton = new JButton("Save");
        newButton.addActionListener(e -> startNew());
        saveButton.addActionListener(e -> saveCurrent());
        deleteButton.addActionListener(e -> deleteCurrent());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        for (JComponent extra : extraButtons()) {
            buttons.add(extra);
        }
        buttons.add(newButton);
        buttons.add(saveButton);
        buttons.add(deleteButton);
        right.add(buttons, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), right);
        split.setResizeWeight(0.65);                 // the table gets most of the extra width
        add(split, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        startNew();
    }

    // ---------------------------------------------------------------
    // What each subclass provides
    // ---------------------------------------------------------------

    /** Loads the items that match the search text (blank = all). */
    protected abstract List<T> load(String search);

    /** Builds the form shown on the right. */
    protected abstract JComponent buildForm();

    /** Shows the item in the form. */
    protected abstract void fillForm(T item);

    /** Empties the form for a new item. */
    protected abstract void clearForm();

    /**
     * Saves what is in the form.
     *
     * @param current the item being edited, or null for a new one
     * @return the saved item
     */
    protected abstract T save(T current);

    /** Deletes the item. */
    protected abstract void delete(T item);

    /** Database id of the item (used to keep the selection after a refresh). */
    protected abstract long idOf(T item);

    /** Short text that names the item in messages, e.g. the book title. */
    protected abstract String describe(T item);

    /** Tooltip of the search box. */
    protected abstract String searchHint();

    /** Extra buttons placed before New/Save/Delete (none by default). */
    protected List<JComponent> extraButtons() {
        return List.of();
    }

    /** Called after the edited item changes (null = new); lets subclasses enable/disable fields. */
    protected void onEditingChanged(T current) {
        // nothing by default
    }

    // ---------------------------------------------------------------
    // Shared behavior
    // ---------------------------------------------------------------

    /** The item being edited, or null when the form is for a new item. */
    protected final T editing() {
        return editing;
    }

    /** Reloads the table and keeps the same item selected if it still exists. */
    @Override
    public final void refresh() {
        Long keep = editing == null ? null : idOf(editing);
        Dialogs.run(this, () -> {
            model.setItems(load(searchField.getText()));
            status.setText(model.getRowCount() + " " + noun + (model.getRowCount() == 1 ? "" : "s"));
        });
        if (keep != null && !select(keep)) {
            startNew();   // it was deleted elsewhere (or hidden by the search)
        }
    }

    private void onSelectionChanged() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        // The view row can differ from the model row once the user sorts the table
        editing = model.getItem(table.convertRowIndexToModel(viewRow));
        fillForm(editing);
        deleteButton.setEnabled(true);
        onEditingChanged(editing);
    }

    private void startNew() {
        table.clearSelection();
        editing = null;
        clearForm();
        deleteButton.setEnabled(false);    // nothing to delete yet
        onEditingChanged(null);
    }

    private void saveCurrent() {
        boolean isNew = editing == null;
        // Lets the lambda hand back the saved item (lambdas can't assign local variables)
        AtomicReference<T> saved = new AtomicReference<>();
        if (Dialogs.run(this, () -> saved.set(save(editing)))) {
            editing = saved.get();
            refresh();
            status.setText(capitalize(noun) + " \"" + describe(saved.get()) + "\" " + (isNew ? "added." : "updated."));
        }
    }

    private void deleteCurrent() {
        T item = editing;
        if (item == null || !Dialogs.confirm(this, "Delete " + noun + " \"" + describe(item) + "\"?")) {
            return;
        }
        if (Dialogs.run(this, () -> delete(item))) {
            startNew();
            refresh();
            status.setText(capitalize(noun) + " \"" + describe(item) + "\" deleted.");
        }
    }

    // Selects the row of the item with this id; false if it isn't in the table
    private boolean select(long id) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (idOf(model.getItem(i)) == id) {
                int viewRow = table.convertRowIndexToView(i);
                if (viewRow < 0) {
                    return false;
                }
                table.setRowSelectionInterval(viewRow, viewRow);
                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                return true;
            }
        }
        return false;
    }

    private static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
