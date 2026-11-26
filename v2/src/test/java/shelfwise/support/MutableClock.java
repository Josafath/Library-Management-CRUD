package shelfwise.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * A clock the test can move forward, to check lock timeouts and overdue
 * loans without waiting.
 */
public final class MutableClock extends Clock {

    private Instant now;

    /** Starts at midday (UTC) of the given date. */
    public MutableClock(LocalDate start) {
        this.now = start.atTime(12, 0).toInstant(ZoneOffset.UTC);
    }

    public void advance(Duration amount) {
        now = now.plus(amount);
    }

    public void advanceDays(long days) {
        advance(Duration.ofDays(days));
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException("not needed in tests");
    }

    @Override
    public Instant instant() {
        return now;
    }
}
