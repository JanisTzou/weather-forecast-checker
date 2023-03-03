package com.google.weatherchecker.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "hourly_forecast_tbl")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JpaHourlyForecast extends JpaEntity {

    @Column(name = "hour", nullable = false)
    private LocalDateTime hour;

    @Column(name = "cloud_coverage_total")
    private Integer cloudCoverageTotal;

    @Column(name = "description")
    private String description;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "forecast_id", nullable = false)
    private JpaForecast forecast;

}
