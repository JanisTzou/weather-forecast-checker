package com.google.weatherchecker.api;

import com.google.weatherchecker.repository.ForecastAnalysisRepository;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.google.weatherchecker.repository.ForecastAnalysisRepository.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Log4j2
public class AnalysisController {

    private final ForecastAnalysisRepository repository;

    @GetMapping(value = "/comparison", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ComparisonsDto> getComparisons(@RequestParam(value = "pastHours", required = false) Integer pastHours,
                                                         @RequestParam(value = "county", required = false) String county,
                                                         @RequestParam(value = "region", required = false) String region,
                                                         @RequestParam(value = "date", required = false) LocalDate date) {
        List<Comparison> comparisons = repository.query(Criteria.from(pastHours, region, county, date));
        List<ComparisonDto> list = comparisons.stream().map(c ->
                        new ComparisonDto(c.getSource(),
                                c.getAvgDiffAbs(),
                                c.getAvgDiff(),
                                c.getRecords(),
                                c.getForecastDescription(),
                                c.getForecastErrorDescription()))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.ok(new ComparisonsDto(pastHours, region, county, date, list));
        }
    }

}
