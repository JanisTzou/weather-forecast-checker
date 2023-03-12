package com.google.weatherchecker.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class GroupedForecastVerification<T> {

    private final List<ForecastVerification> sourceVerifications;
    private final T groupingKey;
    private final int avgForecastCloudTotal;
    private final int avgMeasuredCloudTotal;
    private final int avgDiffAbs;
    private final int avgDiff;
    private final int recordCount;

    public static List<GroupedForecastVerification<GroupingKey>> groupBySource(List<ForecastVerification> verifications) {
        return group(verifications, forecastVerification -> new GroupingKey(forecastVerification.getSource()));
    }

    public static <K extends GroupingKey> List<GroupedForecastVerification<K>> group(List<ForecastVerification> verifications,
                                                                                     Function<ForecastVerification, K> groupingByKey) {
        return group(verifications, groupingByKey, (v1, v2) -> 0);
    }

    public static <K extends GroupingKey> List<GroupedForecastVerification<K>> group(List<ForecastVerification> verifications,
                                                                                     Function<ForecastVerification, K> groupingByKey,
                                                                                     Comparator<GroupedForecastVerification<K>> sorting) {

        Map<K, List<ForecastVerification>> groupLists = verifications.stream()
                .collect(Collectors.groupingBy(groupingByKey, Collectors.toList()));

        return groupLists.entrySet().stream()
                .flatMap(e -> group(e.getKey(), e.getValue()).stream())
                .sorted(sorting)
                .collect(Collectors.toList());
    }

    private static <K> Optional<GroupedForecastVerification<K>> group(K groupingKey, List<ForecastVerification> verifications) {
        if (verifications.isEmpty()) {
            return Optional.empty();
        }
        int recordCount = 0;
        int forecastCloudTotalSum = 0;
        int measuredCloudTotalSum = 0;
        int diffAbsSum = 0;
        int diffSum = 0;

        for (ForecastVerification ver : verifications) {
            int records = ver.getRecordCount();
            recordCount += records;
            forecastCloudTotalSum += ver.getAvgForecastCloudTotal() * records;
            measuredCloudTotalSum += ver.getAvgMeasuredCloudTotal() * records;
            diffAbsSum += ver.getAvgDiffAbs() * records;
            diffSum += ver.getAvgDiff() * records;
        }

        return Optional.of(new GroupedForecastVerification<>(
                verifications,
                groupingKey,
                forecastCloudTotalSum / recordCount,
                measuredCloudTotalSum / recordCount,
                diffAbsSum / recordCount,
                diffSum / recordCount,
                recordCount
        ));
    }

    public List<String> getCounties() {
        return sourceVerifications.stream()
                .map(ForecastVerification::getCounty)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Subclasses myst implement equals() and hashCode()
     */
    @Data
    @RequiredArgsConstructor
    public static class GroupingKey {
        private final Source source;
    }

}
