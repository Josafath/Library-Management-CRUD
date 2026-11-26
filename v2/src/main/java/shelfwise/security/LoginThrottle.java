package shelfwise.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Locks a username for a while after too many wrong passwords, so nobody
 * can sit at the login screen trying passwords one after another.
 *
 * The state lives in memory: restarting the app clears it. That is enough
 * for a desktop app, where the attacker would be at the same computer.
 */
public final class LoginThrottle {

    private final int maxFailures;
    private final Duration lockTime;
    private final Clock clock;   // injected so tests can move time forward

    // Failed attempts per username (lower-cased, so "Admin" and "admin" share a counter)
    private final Map<String, Attempts> attempts = new HashMap<>();

    private static final class Attempts {
        int failures;
        Instant lockedUntil;   // null when not locked
    }

    /** 5 failures lock the account for 1 minute. */
    public LoginThrottle(Clock clock) {
        this(5, Duration.ofMinutes(1), clock);
    }

    public LoginThrottle(int maxFailures, Duration lockTime, Clock clock) {
        this.maxFailures = maxFailures;
        this.lockTime = lockTime;
        this.clock = clock;
    }

    /**
     * How long the username is still locked.
     *
     * @return Duration.ZERO when login attempts are allowed
     */
    public synchronized Duration remainingLock(String username) {
        Attempts a = attempts.get(key(username));
        if (a == null || a.lockedUntil == null) {
            return Duration.ZERO;
        }
        Duration left = Duration.between(clock.instant(), a.lockedUntil);
        if (left.isNegative() || left.isZero()) {
            attempts.remove(key(username));   // lock expired: start counting from zero
            return Duration.ZERO;
        }
        return left;
    }

    /** Records a wrong password; the Nth failure in a row starts the lock. */
    public synchronized void recordFailure(String username) {
        Attempts a = attempts.computeIfAbsent(key(username), k -> new Attempts());
        a.failures++;
        if (a.failures >= maxFailures) {
            a.lockedUntil = clock.instant().plus(lockTime);
        }
    }

    /** A correct login resets the counter. */
    public synchronized void recordSuccess(String username) {
        attempts.remove(key(username));
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
