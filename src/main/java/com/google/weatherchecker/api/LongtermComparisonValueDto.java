package com.google.weatherchecker.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.weatherchecker.model.Source;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LongtermComparisonValueDto {

    private final Source source;
    private final String title;
    private final int avgDiffAbs;
    private final int avgDiff;
    private final int recordCount;

}
