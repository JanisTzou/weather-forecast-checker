package com.google.weatherchecker.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class PastHoursDto {

    private final Integer pastHours;
    private final List<VerificationDto> verifications;

}
