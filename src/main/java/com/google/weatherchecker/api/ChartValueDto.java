package com.google.weatherchecker.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ChartValueDto {

    private LocalDate date;
    private int avgDiffAbs;
    private int avgDiff;

}
