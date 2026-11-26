package shelfwise.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import shelfwise.support.MutableClock;

class LoginThrottleTest {

    private final MutableClock clock = new MutableClock(LocalDate.of(2026, 1, 1));
    private final LoginThrottle throttle = new LoginThrottle(clock);   // 5 failures -> 1 minute

    @Test
    void fourFailuresDoNotLock() {
        for (int i = 0; i < 4; i++) {
            throttle.recordFailure("ana");
        }
        assertEquals(Duration.ZERO, throttle.remainingLock("ana"));
    }

    @Test
    void fifthFailureLocksForOneMinute() {
        for (int i = 0; i < 5; i++) {
            throttle.recordFailure("ana");
        }
        assertEquals(Duration.ofMinutes(1), throttle.remainingLock("ana"));

        clock.advance(Duration.ofSeconds(45));
        assertEquals(Duration.ofSeconds(15), throttle.remainingLock("ana"));
    }

    @Test
    void lockExpiresAndCounterStartsAgain() {
        for (int i = 0; i < 5; i++) {
            throttle.recordFailure("ana");
        }
        clock.advance(Duration.ofMinutes(1));
        assertEquals(Duration.ZERO, throttle.remainingLock("ana"));

        throttle.recordFailure("ana");   // one new failure after the lock: not locked again
        assertEquals(Duration.ZERO, throttle.remainingLock("ana"));
    }

    @Test
    void successResetsTheCounter() {
        for (int i = 0; i < 4; i++) {
            throttle.recordFailure("ana");
        }
        throttle.recordSuccess("ana");
        throttle.recordFailure("ana");
        assertEquals(Duration.ZERO, throttle.remainingLock("ana"));
    }

    @Test
    void usernamesAreCaseInsensitiveAndIndependent() {
        for (int i = 0; i < 5; i++) {
            throttle.recordFailure(i % 2 == 0 ? "Ana" : " ana ");
        }
        assertTrue(throttle.remainingLock("ANA").toSeconds() > 0);
        assertEquals(Duration.ZERO, throttle.remainingLock("carlos"));
    }
}
