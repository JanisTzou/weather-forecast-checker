package com.google.weatherforecastchecker.repos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "cloud_coverage_measurement_tbl")
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JpaCloudCoverageMeasurement extends JpaEntityBase {

    @Column(name = "scraped", nullable = false)
    private LocalDateTime scraped;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private JpaLocation location;

    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private JpaSource source;

    @Column(name = "cloud_coverage_total")
    private Integer cloudCoverageTotal;

    @Column(name = "description")
    private String description;

}
