package com.google.weatherchecker.repository;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "forecast_tbl")
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JpaForecast extends JpaEntityBase {

    @Column(name = "scraped", nullable = false)
    private LocalDateTime scraped;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "source_id", nullable = false)
    private JpaSource source;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", nullable = false)
    private JpaLocation location;

//    @OneToMany(mappedBy = "forecast", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private volatile List<JpaHourlyForecast> hourlyForecasts = new ArrayList<>();

}
