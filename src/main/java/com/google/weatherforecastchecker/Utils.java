package com.google.weatherforecastchecker;

import org.apache.commons.text.StringSubstitutor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Utils {

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

}
