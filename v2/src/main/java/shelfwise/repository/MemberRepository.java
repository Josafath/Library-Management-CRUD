package shelfwise.repository;

import java.util.List;
import java.util.Optional;
import shelfwise.model.Member;

/**
 * Storage of library members.
 */
public interface MemberRepository {

    /**
     * Members whose name, email or phone contain the text (case-insensitive),
     * ordered by name. A blank text returns every member.
     */
    List<Member> search(String text);

    Optional<Member> findById(long id);

    /**
     * @return the same member with its new id
     * @throws DuplicateKeyException if the email is taken
     */
    Member insert(Member member);

    /** @throws DuplicateKeyException if the new email is taken */
    void update(Member member);

    /** Deletes the member and their returned-loan history. */
    void delete(long id);
}
