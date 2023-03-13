package com.google.weatherchecker.api;

import com.google.weatherchecker.model.ForecastVerification;
import com.google.weatherchecker.model.GroupedForecastVerification;
import com.google.weatherchecker.model.Source;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import static com.google.weatherchecker.model.GroupedForecastVerification.*;

@Data
@AllArgsConstructor
@Component
public class VerificationDtoMapper {

    public VerificationDto toDto(ForecastVerification c) {
        Source source = c.getSource();
        return new VerificationDto(source.getAdminName().orElse(source.name()),
                source,
                c.getAvgForecastCloudTotal(),
                c.getAvgMeasuredCloudTotal(),
                c.getAvgDiffAbs(),
                c.getAvgDiff(),
                c.getRecordCount(),
                "TODO",
                "TODO"
        );
    }

    public VerificationDto toDto(GroupedForecastVerification<SourceKey> c) {
        Source source = c.getGroupingKey().getSource();
        return new VerificationDto(source.getAdminName().orElse(source.name()),
                source,
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
