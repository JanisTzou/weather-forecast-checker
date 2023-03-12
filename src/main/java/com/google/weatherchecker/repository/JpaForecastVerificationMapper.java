package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.ForecastVerification;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class JpaForecastVerificationMapper {

    private final JpaSourceMapper jpaSourceMapper;

    public ForecastVerification toDomain(JpaForecastVerification jpaVerification) {
        return new ForecastVerification(
                jpaVerification.getCreated(),
                jpaVerification.getType().getName(),
                jpaVerification.getSource().getName(),
                jpaVerification.getAvgForecastCloudTotal(),
                jpaVerification.getAvgMeasuredCloudTotal(),
                jpaVerification.getAvgDiffAbs(),
                jpaVerification.getAvgDiff(),
                jpaVerification.getRecordCount(),
                jpaVerification.getPastHours(),
                jpaVerification.getDay(),
                jpaVerification.getCounty().map(JpaCounty::getName).orElse(null)
        );
    }

    public JpaForecastVerification toEntity(ForecastVerification verification) {
        return new JpaForecastVerification(
                verification.getCreated(),
                new JpaForecastVerificationType(verification.getType()),
                jpaSourceMapper.toEntity(verification.getSource()),
                verification.getAvgForecastCloudTotal(),
                verification.getAvgMeasuredCloudTotal(),
                verification.getAvgDiffAbs(),
                verification.getAvgDiff(),
                verification.getRecordCount(),
                verification.getPastHours(),
                verification.getDay(),
                new JpaCounty(verification.getCounty())
        );
    }

}
