package shelfwise;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import shelfwise.support.InMemoryRepositories;
import shelfwise.support.MutableClock;

class SampleDataTest {

    @Test
    void sampleDataPassesTheServiceValidation() {
        Services services = new InMemoryRepositories().services(new MutableClock(LocalDate.of(2026, 3, 1)));
        SampleData.load(services);   // would throw ValidationException if any sample were invalid
        assertEquals(SampleData.BOOKS.size(), services.books().search("").size());
        assertEquals(SampleData.MEMBERS.size(), services.members().search("").size());
    }
}
