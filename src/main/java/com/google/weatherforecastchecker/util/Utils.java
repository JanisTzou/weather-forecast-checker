package com.google.weatherforecastchecker.util;

import com.google.weatherforecastchecker.scraper.LocationConfigRepository;
import org.apache.commons.text.StringSubstitutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Utils {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    public static Optional<String> getFirstMatch(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(0));
        }
        return Optional.empty();
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String fillTemplate(String template, Map<String, Object> valuesMap) {
        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        return sub.replace(template.replace("|", "")); // hack so that Spring does not try to fill in our own placeholders during ctx initialisation
    }

    public static <T> Stream<T> toStream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

    public static List<String> readResourcesFileLines(String resourcesFile) throws IOException {
        InputStream inputStream = LocationConfigRepository.class.getClassLoader().getResourceAsStream(resourcesFile);
        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            List<String> lines = new ArrayList<>();

            while (reader.ready()) {
                lines.add(reader.readLine());
            }
            return lines;
        } else {
            throw new IllegalArgumentException("file " + resourcesFile + " not found! ");
        }
    }

    public static List<LocalTime> parseCommaSeparatedTimes(String times) {
        if (times != null && !times.isEmpty()) {
            return Arrays.stream(times.split(","))
                    .map(text -> LocalTime.parse(text, TIME_FORMATTER))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
