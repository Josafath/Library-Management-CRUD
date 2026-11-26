package shelfwise.service;

import java.util.List;
import shelfwise.model.Member;
import shelfwise.repository.DuplicateKeyException;
import shelfwise.repository.LoanRepository;
import shelfwise.repository.MemberRepository;

/**
 * Library members: search, add, edit, delete.
 */
public final class MemberService {

    private final MemberRepository members;
    private final LoanRepository loans;

    public MemberService(MemberRepository members, LoanRepository loans) {
        this.members = members;
        this.loans = loans;
    }

    /** Members matching the text (name, email or phone); blank = all. */
    public List<Member> search(String text) {
        return members.search(text);
    }

    /** Saves a member: inserts it when its id is 0, otherwise updates it. */
    public Member save(Member member) {
        Member clean = new Member(
                member.id(),
                Validation.requireText(member.fullName(), "Name", 120),
                Validation.requireEmail(member.email()),
                Validation.optionalText(member.phone(), "Phone", 30));
        try {
            if (clean.id() == 0) {
                return members.insert(clean);
            }
            members.findById(clean.id())
                    .orElseThrow(() -> new ValidationException("That member no longer exists. Refresh the list."));
            members.update(clean);
            return clean;
        } catch (DuplicateKeyException ex) {
            throw new ValidationException("Another member already uses " + clean.email() + ".");
        }
    }

    /** Deletes a member, unless they still have books. */
    public void delete(long id) {
        int active = loans.countActiveByMember(id);
        if (active > 0) {
            throw new ValidationException("This member still has " + active + " book(s). Register the returns first.");
        }
        members.delete(id);
    }
}
