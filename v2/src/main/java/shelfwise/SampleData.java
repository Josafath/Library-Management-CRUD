package shelfwise;

import java.util.List;
import shelfwise.model.Book;
import shelfwise.model.Member;

/**
 * Demo books and members, offered by the first-run wizard so the app isn't
 * empty the first time you open it.
 */
public final class SampleData {

    private SampleData() {
        // Utility class: no instances
    }

    static final List<Book> BOOKS = List.of(
            Book.draft("Cien años de soledad", "Gabriel García Márquez", "Novel", "Sudamericana"),
            Book.draft("Pedro Páramo", "Juan Rulfo", "Novel", "Fondo de Cultura Económica"),
            Book.draft("El laberinto de la soledad", "Octavio Paz", "Essay", "Cuadernos Americanos"),
            Book.draft("El Principito", "Antoine de Saint-Exupéry", "Fable", "Reynal & Hitchcock"),
            Book.draft("Clean Code", "Robert C. Martin", "Programming", "Prentice Hall"),
            Book.draft("Effective Java", "Joshua Bloch", "Programming", "Addison-Wesley"),
            Book.draft("The Pragmatic Programmer", "Andrew Hunt, David Thomas", "Programming", "Addison-Wesley"),
            Book.draft("1984", "George Orwell", "Dystopia", "Secker & Warburg"));

    static final List<Member> MEMBERS = List.of(
            Member.draft("Ana López", "ana.lopez@example.com", "555-0101"),
            Member.draft("Carlos Ruiz", "carlos.ruiz@example.com", ""),
            Member.draft("María Fernández", "maria.fernandez@example.com", "555-0103"));

    /** Adds the sample books and members through the services (so they are validated too). */
    public static void load(Services services) {
        BOOKS.forEach(services.books()::save);
        MEMBERS.forEach(services.members()::save);
    }
}
