package com.google.weatherforecastchecker.util;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;


public class CsvLine {

    private static final String no_value = "-";

    private final List<String> headers;
    private final List<String> values;

    public CsvLine(List<String> headers, List<String> values) {
        if (headers.size() != values.size()) {
            throw new IllegalArgumentException("Number of headers and values must be equal");
        }
        this.headers = headers;
        this.values = values;
    }

    public String getString(String headerName) {
        int pos = pos(headerName);
        if (pos == -1) {
            throw new IllegalArgumentException("No header found for name = " + headerName);
        }
        return values.get(pos);
    }

    public boolean getBoolean(String headerName) {
        return Boolean.parseBoolean(values.get(pos(headerName)));
    }

    public int getInt(String headerName) {
        return Integer.parseInt(values.get(pos(headerName)));
    }

    public double getDouble(String headerName) {
        return Double.parseDouble(values.get(pos(headerName)));
    }

    public List<LocalTime> getTimes(String headerName) {
        return parseTimes(getString(headerName));
    }

    /**
     * @param timesStr comma separated list of times in 'H:mm' format
     */
    private static List<LocalTime> parseTimes(String timesStr) {
        if (no_value.equals(timesStr)) {
            return Collections.emptyList();
        } else {
            return Utils.parseCommaSeparatedTimes(timesStr);
        }
    }

    private int pos(String headerName) {
        return headers.indexOf(headerName);
    }
}
