package com.google.weatherchecker.repository;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "forecast_verification_tbl")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JpaForecastVerification extends JpaEntity {

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private JpaForecastVerificationType type;

    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private JpaSource source;

    @Column(name = "avg_forecast_cloud_total")
    private int avgForecastCloudTotal;

    @Column(name = "avg_measured_cloud_total")
    private int avgMeasuredCloudTotal;

    @Column(name = "avg_diff_abs")
    private int avgDiffAbs;

    @Column(name = "avg_diff")
    private int avgDiff;

    @Column(name = "record_count")
    private int recordCount;

    @Column(name = "past_hours", nullable = true)
    private Integer pastHours;

    @Column(name = "day", nullable = true)
    private LocalDate day;

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = true)
    private JpaRegion region;

    @ManyToOne
    @JoinColumn(name = "county_id", nullable = true)
    private JpaCounty county;


    public Optional<JpaRegion> getRegion() {
        return Optional.ofNullable(region);
    }

    public Optional<JpaCounty> getCounty() {
        return Optional.ofNullable(county);
    }

}
