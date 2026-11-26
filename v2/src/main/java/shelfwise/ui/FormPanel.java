package shelfwise.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A two-column form: labels on the left, fields on the right.
 *
 * v1 used NetBeans' GroupLayout with hundreds of generated lines and fixed
 * pixel gaps. GridBagLayout with one helper method keeps each form short
 * and lets the fields grow with the window.
 */
public final class FormPanel extends JPanel {

    private int row = 0;   // next free row

    public FormPanel() {
        super(new GridBagLayout());
    }

    /** Adds "label: field" as a new row; returns the field for chaining. */
    public <C extends JComponent> C addRow(String label, C field) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = row;
        left.anchor = GridBagConstraints.LINE_END;     // labels aligned to the right
        left.insets = new Insets(4, 4, 4, 8);
        JLabel text = new JLabel(label);
        text.setLabelFor(field);                       // screen readers read the label with the field
        add(text, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = row;
        right.weightx = 1.0;                           // the field column takes the extra width
        right.fill = GridBagConstraints.HORIZONTAL;
        right.insets = new Insets(4, 0, 4, 4);
        add(field, right);

        row++;
        return field;
    }

    /** Adds a component that spans both columns (buttons, notes...). */
    public void addFullRow(JComponent component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 4, 4, 4);
        add(component, c);
    }

    /** Pushes every row to the top when the panel is taller than the form. */
    public void addFiller() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.weighty = 1.0;
        add(new JLabel(), c);
    }
}
