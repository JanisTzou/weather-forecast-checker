package com.google.weatherchecker.api;

import com.google.weatherchecker.model.ForecastVerification;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.repository.VerificationCriteria;
import com.google.weatherchecker.repository.ForecastVerificationRepository;
import com.google.weatherchecker.verification.PastHoursProps;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<MainPageDto> getMainPage(String region, String county) {
        List<PastHoursDto> pastHours = pastHoursProps.getPastHours().stream()
                .map(ph -> getVerificationsByPastHours(region, county, ph))
                .collect(Collectors.toList());

        Optional<ChartDto> chart = getDailyVerificationChart(region, county);

        if (!pastHours.isEmpty() || chart.isPresent()) {
            return Optional.of(new MainPageDto(region, county, pastHours, chart.orElse(null)));
        }
        return Optional.empty();
    }

    private PastHoursDto getVerificationsByPastHours(String region, String county, Integer ph) {
        VerificationCriteria criteria = VerificationCriteria.builder().setRegion(region).setCounty(county).setPastHours(ph).build();
        List<ForecastVerification> forecastVerifications = getForecastVerifications(criteria);

        List<VerificationDto> verificationDtos = forecastVerifications.stream()
                .sorted(Comparator.comparingInt(fv -> Math.abs(fv.getAvgDiff())))
                .map(verificationDtoMapper::toDto)
                .collect(Collectors.toList());

        return new PastHoursDto(ph, verificationDtos);
    }

    private List<ForecastVerification> getForecastVerifications(VerificationCriteria criteria) {
        List<ForecastVerification> forecastVerifications = repository.findVerifications(criteria);
        if (forecastVerifications.isEmpty()) {
            forecastVerifications = repository.calculateVerifications(criteria);
            log.warn("Calculating verifications as they were not pre-calculated!");
        }
        return forecastVerifications;
    }

    private Optional<ChartDto> getDailyVerificationChart(String region, String county) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(dailyChartDays);
        LocalDate to = today.minusDays(1);
        // TODO pass sources as criteria that we do not want to include ...
        VerificationCriteria criteria = VerificationCriteria.builder().setRegion(region).setCounty(county).setFromToDate(from, to).build();
        List<ForecastVerification> forecastVerifications = getForecastVerifications(criteria);

        // should be sorted correctly by date ...
        Map<Source, List<ForecastVerification>> sourceVerifications = forecastVerifications.stream().collect(Collectors.groupingBy(ForecastVerification::getSource, Collectors.toList()));

        if (!sourceVerifications.isEmpty()) {
            List<ChartSeriesDto> chartSeriesDtos = toChartSeries(sourceVerifications);
            return Optional.of(new ChartDto("TODO title", chartSeriesDtos));
        }
        return Optional.empty();
    }

    // TODO provide a mapper ?
    private List<ChartSeriesDto> toChartSeries(Map<Source, List<ForecastVerification>> sourceVerifications) {
        return sourceVerifications.entrySet().stream()
                .map(e -> {
                    List<ForecastVerification> vs = e.getValue();
                    List<ChartValueDto> values = vs.stream().map(v -> new ChartValueDto(v.getDay(), v.getAvgDiffAbs(), v.getAvgDiff())).collect(Collectors.toList());
                    return new ChartSeriesDto(e.getKey().name(), "TODO", values);
                })
                .collect(Collectors.toList());
    }

}
