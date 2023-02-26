package com.google.weatherchecker.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CsvFile {

    public static final String DEFAULT_SEPARATOR ="\\|";

    private final List<CsvLine> csvLines;

    private CsvFile(List<String> lines, String separatorRegex) {
        if (!lines.isEmpty()) {
            List<String> headers = splitLine(lines.get(0), separatorRegex);
            this.csvLines = splitLines(lines, headers, separatorRegex);
        } else {
            csvLines = Collections.emptyList();
        }
    }

    public static CsvFile fromResourceFile(String resourceFile) {
        return fromResourceFile(resourceFile, DEFAULT_SEPARATOR);
    }

    public static CsvFile fromResourceFile(String resourceFile, String separatorRegex) {
        try {
            return new CsvFile(Utils.readResourcesFileLines(resourceFile), separatorRegex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<CsvLine> splitLines(List<String> lines, List<String> headers, String separatorRegex) {
        return lines.stream()
                .skip(1L)
                .map(line -> new CsvLine(headers, splitLine(line, separatorRegex)))
                .collect(Collectors.toList());
    }

    private List<String> splitLine(String line, String separatorRegex) {
        String[] values = line.trim().split(separatorRegex);
        return Arrays.asList(values);
    }

    public List<CsvLine> getLines() {
        return Collections.unmodifiableList(csvLines);
    }

}
