package com.google.weatherchecker.util;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;


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

    public Optional<String> getString(String headerName) {
        return checkNoValueAndGet(headerName, v -> v);
    }

    public Optional<Boolean> getBoolean(String headerName) {
        return checkNoValueAndGet(headerName, Boolean::parseBoolean);
    }

    public Optional<Integer> getInt(String headerName) {
        return checkNoValueAndGet(headerName, Integer::parseInt);
    }

    public Optional<Double> getDouble(String headerName) {
        return checkNoValueAndGet(headerName, Double::parseDouble);
    }

    public List<LocalTime> getTimes(String headerName) {
        return parseTimes(getString(headerName).orElse(null));
    }

    /**
     * @param timesStr comma separated list of times in 'H:mm' format
     */
    private static List<LocalTime> parseTimes(String timesStr) {
        if (timesStr == null || no_value.equals(timesStr)) {
            return Collections.emptyList();
        } else {
            return Utils.parseCommaSeparatedTimes(timesStr);
        }
    }

    private int pos(String headerName) {
        int pos = headers.indexOf(headerName);
        if (pos == -1) {
            throw new IllegalArgumentException("No header found for name = " + headerName);
        }
        return pos;
    }

    private String getValueFor(String headerName) {
        return values.get(pos(headerName));
    }

    private <T> Optional<T> checkNoValueAndGet(String headerName, Function<String, T> parser) {
        String value = getValueFor(headerName);
        if (no_value.equals(value)) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(parser.apply(value));
        }
    }

}
