package shelfwise.ui;

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTextField;
import shelfwise.model.Member;
import shelfwise.service.MemberService;
import static shelfwise.ui.ListTableModel.Column;

/**
 * "Members" tab: people who can borrow books (new in v2).
 */
public final class MembersPanel extends CrudPanel<Member> {

    private final MemberService members;

    private final JTextField fullName = new JTextField(20);
    private final JTextField email = new JTextField(20);
    private final JTextField phone = new JTextField(20);

    public MembersPanel(MemberService members) {
        super("member", List.of(
                Column.<Member>number("ID", Member::id),
                Column.<Member>text("Name", Member::fullName),
                Column.<Member>text("Email", Member::email),
                Column.<Member>text("Phone", Member::phone)));
        this.members = members;
        initUi();
    }

    @Override
    protected List<Member> load(String search) {
        return members.search(search);
    }

    @Override
    protected JComponent buildForm() {
        FormPanel form = new FormPanel();
        form.addRow("Name:", fullName);
        form.addRow("Email:", email);
        form.addRow("Phone (optional):", phone);
        form.addFiller();
        return form;
    }

    @Override
    protected void fillForm(Member member) {
        fullName.setText(member.fullName());
        email.setText(member.email());
        phone.setText(member.phone());
    }

    @Override
    protected void clearForm() {
        fullName.setText("");
        email.setText("");
        phone.setText("");
        fullName.requestFocusInWindow();
    }

    @Override
    protected Member save(Member current) {
        long id = current == null ? 0 : current.id();   // 0 = insert, otherwise update
        return members.save(new Member(id, fullName.getText(), email.getText(), phone.getText()));
    }

    @Override
    protected void delete(Member member) {
        members.delete(member.id());
    }

    @Override
    protected long idOf(Member member) {
        return member.id();
    }

    @Override
    protected String describe(Member member) {
        return member.fullName();
    }

    @Override
    protected String searchHint() {
        return "Name, email or phone";
    }
}
