package com.google.weatherforecastchecker.repos;

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
public class JpaHourlyForecast extends JpaEntityBase {

    @Column(name = "hour", nullable = false)
    private LocalDateTime hour;

    @Column(name = "cloud_coverage")
    private Integer cloudCoverage;

    @Column(name = "description")
    private String description;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "forecast_id", nullable = false)
    private JpaForecast forecast;

}
