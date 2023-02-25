package com.google.weatherforecastchecker.util;

import com.google.weatherforecastchecker.util.DateTimeUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTimeUtilsTest {

    @Test
    public void untilNextTime() {

        long hours;

        hours = DateTimeUtils.untilNextTime(LocalTime.parse("12:00"), LocalTime.parse("12:00"), ChronoUnit.HOURS).toHours();
        assertEquals(0L, hours);
        hours = DateTimeUtils.untilNextTime(LocalTime.parse("22:00"), LocalTime.parse("00:00"), ChronoUnit.HOURS).toHours();
        assertEquals(2L, hours);
        hours = DateTimeUtils.untilNextTime(LocalTime.parse("00:00"), LocalTime.parse("22:00"), ChronoUnit.HOURS).toHours();
        assertEquals(22L, hours);
        hours = DateTimeUtils.untilNextTime(LocalTime.parse("10:00"), LocalTime.parse("12:00"), ChronoUnit.HOURS).toHours();
        assertEquals(2L, hours);
        hours = DateTimeUtils.untilNextTime(LocalTime.parse("22:00"), LocalTime.parse("12:00"), ChronoUnit.HOURS).toHours();
        assertEquals(14L, hours);

    }

}
