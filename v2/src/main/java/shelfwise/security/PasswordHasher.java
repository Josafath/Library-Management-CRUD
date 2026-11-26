package shelfwise.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashes and checks passwords with PBKDF2-HMAC-SHA256 (built into the JDK,
 * so no extra library is needed).
 *
 * v1 stored passwords in plain text: anyone who could read the users table
 * could read every password. Now the table only holds a hash like
 *
 * <pre>pbkdf2_sha256$600000$&lt;salt&gt;$&lt;hash&gt;</pre>
 *
 * - The random salt makes two equal passwords produce different hashes.
 * - The iteration count makes each guess slow for an attacker (600,000 is
 *   the OWASP recommendation for PBKDF2-SHA256). It is stored in the hash,
 *   so it can be raised later without breaking existing accounts.
 */
public final class PasswordHasher {

    /** OWASP's recommended iteration count for PBKDF2-HMAC-SHA256. */
    public static final int DEFAULT_ITERATIONS = 600_000;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2_sha256";
    private static final int SALT_BYTES = 16;   // 128-bit salt
    private static final int HASH_BITS = 256;   // 32-byte derived key

    private final int iterations;
    private final SecureRandom random = new SecureRandom();

    public PasswordHasher() {
        this(DEFAULT_ITERATIONS);
    }

    /**
     * @param iterations PBKDF2 rounds for new hashes (tests use a small
     *                   number so they run fast)
     */
    public PasswordHasher(int iterations) {
        if (iterations < 1) {
            throw new IllegalArgumentException("iterations must be positive");
        }
        this.iterations = iterations;
    }

    /** Returns the hash to store for this password. */
    public String hash(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);                          // new random salt per password
        byte[] hash = derive(password, salt, iterations);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIX + "$" + iterations + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);
    }

    /**
     * Checks a password against a stored hash.
     *
     * @return false for a wrong password or a malformed hash (never throws for bad input)
     */
    public boolean verify(char[] password, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !parts[0].equals(PREFIX)) {
            return false;
        }
        try {
            int storedIterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            if (storedIterations < 1) {
                return false;
            }
            // Recalculate with the same salt and rounds, then compare
            byte[] actual = derive(password, salt, storedIterations);
            // isEqual takes the same time wherever the bytes differ, so the
            // response time doesn't leak how much of the hash matched
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {   // bad number or bad Base64
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int rounds) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, rounds, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            // Every Java 21 runtime ships this algorithm, so this means a broken JDK
            throw new IllegalStateException(ALGORITHM + " is not available", ex);
        } finally {
            spec.clearPassword();   // wipe the copy of the password the spec keeps
        }
    }

    /** Overwrites a password array so it doesn't stay in memory. */
    public static void wipe(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
