package com.google.weatherchecker.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "location_tbl")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JpaLocation extends JpaEntityBase {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "municipality", nullable = true)
    private String municipality;

    @Column(name = "county", nullable = true)
    private String county;

    @Column(name = "region", nullable = true)
    private String region;

    @Column(name = "complete", nullable = false)
    private boolean complete;

    public void updateWith(JpaLocation update) {
        this.name = update.getName();
        this.latitude = update.getLatitude();
        this.longitude = update.getLongitude();
        this.municipality = update.getMunicipality();
        this.county = update.getCounty();
        this.region = update.getRegion();
        this.complete = update.isComplete();
    }

}
