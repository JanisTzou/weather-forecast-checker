package com.google.weatherchecker.api;

import com.google.weatherchecker.model.ForecastVerification;
import com.google.weatherchecker.model.GroupedForecastVerification;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.repository.VerificationCriteria;
import com.google.weatherchecker.repository.ForecastVerificationRepository;
import com.google.weatherchecker.verification.PastHoursProps;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.weatherchecker.model.GroupedForecastVerification.*;

@Component
@Log4j2
public class MainPageApiService {

    private final ForecastVerificationRepository repository;
    private final PastHoursProps pastHoursProps;
    private final VerificationDtoMapper verificationDtoMapper;
    private final int dailyChartDays;

    public MainPageApiService(ForecastVerificationRepository repository,
                              PastHoursProps pastHoursProps,
                              VerificationDtoMapper verificationDtoMapper,
                              @Value("${verifications.chart.daily.days}") int dailyChartDays) {
        this.repository = repository;
        this.pastHoursProps = pastHoursProps;
        this.verificationDtoMapper = verificationDtoMapper;
        this.dailyChartDays = dailyChartDays;
    }

    public Optional<MainPageDto> getMainPage(@Nullable List<String> counties) {
        List<PastHoursDto> pastHours = pastHoursProps.getPastHours().stream()
                .map(ph -> getVerificationsByPastHours(counties, ph))
                .collect(Collectors.toList());

        List<ForecastVerification> dailyVerifications = getDailyVerifications(counties);

        Optional<DailyChartDto> chart = getDailyVerificationChart(dailyVerifications);

        Optional<LongtermSourceComparisonChartDto> longtermComparison = getLongtermComparison(dailyVerifications);

        if (!pastHours.isEmpty() || chart.isPresent() || longtermComparison.isPresent()) {
            return Optional.of(new MainPageDto(
                    getTitle(counties),
                    counties,
                    pastHours,
                    chart.orElse(null),
                    longtermComparison.orElse(null)
            ));
        }
        return Optional.empty();
    }

    private PastHoursDto getVerificationsByPastHours(List<String> counties, Integer ph) {
        VerificationCriteria criteria = VerificationCriteria.builder().addCounties(counties).setPastHours(ph).build();
        List<ForecastVerification> verifications = getForecastVerifications(criteria);
        List<GroupedForecastVerification<SourceKey>> groupedVerifications = groupBySource(verifications);

        List<VerificationDto> verificationDtos = groupedVerifications.stream()
                .sorted(Comparator.comparingInt(fv -> Math.abs(fv.getAvgDiff())))
                .map(verificationDtoMapper::toDto)
                .collect(Collectors.toList());

        return new PastHoursDto(ph, verificationDtos);
    }

    private List<ForecastVerification> getForecastVerifications(VerificationCriteria criteria) {
        List<ForecastVerification> forecastVerifications = repository.findVerifications(criteria);
        if (forecastVerifications.isEmpty()) {
            log.warn("Missing precalculated verifications for criteria: {}", criteria);
        }
        return forecastVerifications;
    }

    private Optional<DailyChartDto> getDailyVerificationChart(List<ForecastVerification> dailyVerifications) {
        List<GroupedForecastVerification<ValueKey>> groupedVerifications = group(dailyVerifications,
                v -> new ValueKey(v.getSource(), v.getDay()),
                Comparator.comparing(g -> g.getGroupingKey().getDay())
        );

        Map<Source, List<GroupedForecastVerification<ValueKey>>> sourceVerifications = groupedVerifications.stream()
                .collect(Collectors.groupingBy(gv -> gv.getGroupingKey().getSource(), Collectors.toList()));

        if (!sourceVerifications.isEmpty()) {
            List<DailyChartSeriesDto> seriesDtos = toChartSeries(sourceVerifications);
            return Optional.of(new DailyChartDto("TODO title", seriesDtos));
        }
        return Optional.empty();
    }

    private Optional<LongtermSourceComparisonChartDto> getLongtermComparison(List<ForecastVerification> dailyVerifications) {
        List<GroupedForecastVerification<SourceKey>> groupedVerifications = group(dailyVerifications,
                v -> new SourceKey(v.getSource()),
                Comparator.comparing(v -> v.getGroupingKey().getSource())
        );

        if (!groupedVerifications.isEmpty()) {
            List<LongtermComparisonValueDto> valueDtos = toLongtermComparisonValues(groupedVerifications);
            return Optional.of(new LongtermSourceComparisonChartDto("TODO title", valueDtos));
        }
        return Optional.empty();
    }

    private List<ForecastVerification> getDailyVerifications(List<String> counties) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(dailyChartDays);
        LocalDate to = today.minusDays(1);
        // TODO pass sources as criteria that we do not want to include ...
        VerificationCriteria criteria = VerificationCriteria.builder().addCounties(counties).setFromToDate(from, to).build();
        List<ForecastVerification> verifications = getForecastVerifications(criteria);
        return verifications;
    }

    // TODO provide a mapper ?
    private List<DailyChartSeriesDto> toChartSeries(Map<Source, List<GroupedForecastVerification<ValueKey>>> verifications) {
        return verifications.entrySet().stream()
                .map(e -> {
                    List<GroupedForecastVerification<ValueKey>> vs = e.getValue();
                    List<DailyChartValueDto> values = vs.stream().map(v -> new DailyChartValueDto(v.getGroupingKey().getDay(), v.getAvgDiffAbs(), v.getAvgDiff(), v.getRecordCount())).collect(Collectors.toList());
                    Source source = e.getKey();
                    String title = source.getAdminName().orElse(source.name());
                    return new DailyChartSeriesDto(source.name(), title, values);
                })
                .collect(Collectors.toList());
    }

    // TODO basically we need 2 version with different sorting ...
    private List<LongtermComparisonValueDto> toLongtermComparisonValues(List<GroupedForecastVerification<SourceKey>> verifications) {
        return verifications.stream()
                .sorted(Comparator.comparingInt(v -> Math.abs(v.getAvgDiff())))
                .map(v -> {
                    Source source = v.getGroupingKey().getSource();
                    String title = source.getAdminName().orElse(source.name());
                    return new LongtermComparisonValueDto(source, title,v.getAvgDiffAbs(), v.getAvgDiff(), v.getRecordCount());
                })
                .collect(Collectors.toList());
    }

    private String getTitle(List<String> counties) {
        if (counties != null && !counties.isEmpty()) {
            if (counties.size() == 1) {
                return counties.get(0);
            } else {
                return "Více vybraných krajů";
            }
        } else {
            return "Česká repulika";
        }
    }

    @ToString
    @Getter
    public static class ValueKey extends SourceKey {

        private final LocalDate day;

        public ValueKey(Source source, LocalDate day) {
            super(source);
            this.day = day;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ValueKey)) return false;
            if (!super.equals(o)) return false;
            ValueKey valueKey = (ValueKey) o;
            return Objects.equals(day, valueKey.day);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), day);
        }
    }

}
