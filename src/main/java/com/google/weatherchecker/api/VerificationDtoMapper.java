package com.google.weatherchecker.api;

import com.google.weatherchecker.model.ForecastVerification;
import com.google.weatherchecker.model.GroupedForecastVerification;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import static com.google.weatherchecker.model.GroupedForecastVerification.*;

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

    public VerificationDto toDto(GroupedForecastVerification<GroupingKey> c) {
        return new VerificationDto(c.getGroupingKey().getSource().name(),
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
