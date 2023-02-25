package com.google.weatherforecastchecker.util;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

public class DateTimeUtils {

    public static Duration untilNextTime(Temporal startInclusive, Temporal endExclusive, TemporalUnit unit) {
        long until = startInclusive.until(endExclusive, unit);
        if (until >= 0L) {
            return Duration.of(until, unit);
        } else {
            long part1 = startInclusive.minus(Duration.ofNanos(1)).until(LocalTime.MIDNIGHT.minusNanos(1), unit);
            long part2 = LocalTime.MIDNIGHT.until(endExclusive, unit);
            return Duration.of(part1 + part2, unit);
        }
    }

}
