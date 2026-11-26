package shelfwise.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    // Few rounds so the tests are fast; the format and logic are the same
    private final PasswordHasher hasher = new PasswordHasher(1_000);

    @Test
    void correctPasswordVerifies() {
        String hash = hasher.hash("correct horse".toCharArray());
        assertTrue(hasher.verify("correct horse".toCharArray(), hash));
    }

    @Test
    void wrongPasswordFails() {
        String hash = hasher.hash("correct horse".toCharArray());
        assertFalse(hasher.verify("correct horsE".toCharArray(), hash));
        assertFalse(hasher.verify(new char[0], hash));
    }

    @Test
    void hashNeverContainsThePassword() {
        String hash = hasher.hash("SuperSecret123".toCharArray());
        assertFalse(hash.contains("SuperSecret123"));
    }

    @Test
    void samePasswordGivesDifferentHashesBecauseOfTheSalt() {
        String a = hasher.hash("same-password".toCharArray());
        String b = hasher.hash("same-password".toCharArray());
        assertNotEquals(a, b);
        assertTrue(hasher.verify("same-password".toCharArray(), a));
        assertTrue(hasher.verify("same-password".toCharArray(), b));
    }

    @Test
    void hashHasTheExpectedFormat() {
        String[] parts = hasher.hash("x".toCharArray()).split("\\$");
        assertEquals(4, parts.length);
        assertEquals("pbkdf2_sha256", parts[0]);
        assertEquals("1000", parts[1]);
    }

    @Test
    void verifiesHashesMadeWithOtherIterationCounts() {
        // The rounds are read from the stored hash, so raising them later doesn't break old accounts
        String oldHash = new PasswordHasher(500).hash("pw".toCharArray());
        assertTrue(hasher.verify("pw".toCharArray(), oldHash));
    }

    @Test
    void worksWithNonAsciiPasswords() {
        String hash = hasher.hash("contraseña-ñandú".toCharArray());
        assertTrue(hasher.verify("contraseña-ñandú".toCharArray(), hash));
        assertFalse(hasher.verify("contrasena-nandu".toCharArray(), hash));
    }

    @Test
    void malformedHashesAreRejectedWithoutThrowing() {
        char[] pw = "pw".toCharArray();
        assertFalse(hasher.verify(pw, null));
        assertFalse(hasher.verify(pw, ""));
        assertFalse(hasher.verify(pw, "pw"));                          // a v1 plain-text password
        assertFalse(hasher.verify(pw, "md5$1000$abc$def"));
        assertFalse(hasher.verify(pw, "pbkdf2_sha256$notanumber$abc$def"));
        assertFalse(hasher.verify(pw, "pbkdf2_sha256$1000$%%%$def"));
        assertFalse(hasher.verify(pw, "pbkdf2_sha256$0$AAAA$AAAA"));
    }

    @Test
    void rejectsNonPositiveIterations() {
        assertThrows(IllegalArgumentException.class, () -> new PasswordHasher(0));
    }

    @Test
    void wipeClearsTheArray() {
        char[] pw = "secret".toCharArray();
        PasswordHasher.wipe(pw);
        assertArrayEquals(new char[6], pw);
        PasswordHasher.wipe(null);   // must not throw
    }
}
