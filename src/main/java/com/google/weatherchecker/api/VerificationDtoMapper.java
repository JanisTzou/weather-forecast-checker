package com.google.weatherchecker.api;

import com.google.weatherchecker.model.ForecastVerification;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@Component
public class VerificationDtoMapper {

    public VerificationDto toDto(ForecastVerification c) {
        return new VerificationDto(c.getSource().name(),
                c.getAvgForecastCloudTotal(),
                c.getAvgMeasuredCloudTotal(),
                c.getAvgDiffAbs(),
                c.getAvgDiff(),
                c.getRecordCount(),
                "TODO",
                "TODO"
        );
    }

}
