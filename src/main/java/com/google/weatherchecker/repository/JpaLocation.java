package com.google.weatherchecker.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Table(name = "location_tbl")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JpaLocation extends JpaEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "municipality_id", nullable = true)
    private JpaMunicipality municipality;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "county_id", nullable = true)
    private JpaCounty county;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "region_id", nullable = true)
    private JpaRegion region;

    @Column(name = "complete", nullable = false)
    private boolean complete;

    public void updateWith(JpaLocation update) {
        this.name = update.getName();
        this.latitude = update.getLatitude();
        this.longitude = update.getLongitude();
        this.municipality = update.getMunicipality().orElse(null);
        this.county = update.getCounty().orElse(null);
        this.region = update.getRegion().orElse(null);
        this.complete = update.isComplete();
    }

    public Optional<JpaMunicipality> getMunicipality() {
        return Optional.ofNullable(municipality);
    }

    public Optional<JpaCounty> getCounty() {
        return Optional.ofNullable(county);
    }

    public Optional<JpaRegion> getRegion() {
        return Optional.ofNullable(region);
    }


}
