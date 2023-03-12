package com.google.weatherchecker.api;

import com.google.weatherchecker.model.ForecastVerification;
import com.google.weatherchecker.repository.ForecastVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.weatherchecker.repository.VerificationCriteria;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Log4j2
public class CalculationApiController {

    private final ForecastVerificationRepository repository;

    @GetMapping(value = "/comparison", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VerificationDto>> getComparisons(@RequestParam(value = "pastHours", required = false) Integer pastHours,
                                                                @RequestParam(value = "county", required = false) String county,
                                                                @RequestParam(value = "date", required = false) LocalDate date) { // TODO add date range?

        VerificationCriteria criteria = VerificationCriteria.builder().setPastHours(pastHours).addCounty(county).setDate(date).build();

        List<ForecastVerification> forecastVerifications = repository.findVerifications(criteria);
        if (forecastVerifications.isEmpty()) {
            forecastVerifications = repository.calculateVerifications(criteria);
        }

        List<VerificationDto> list = forecastVerifications.stream()
                .sorted(Comparator.comparingInt(fv -> Math.abs(fv.getAvgDiff())))
                .map(c ->
                        new VerificationDto(c.getSource().name(),
                                c.getAvgForecastCloudTotal(),
                                c.getAvgMeasuredCloudTotal(),
                                c.getAvgDiffAbs(),
                                c.getAvgDiff(),
                                c.getRecordCount(),
                                "TODO",
                                "TODO"))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.ok(list);
        }
    }

}
