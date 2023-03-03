package com.google.weatherchecker.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "county_tbl")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JpaCounty extends JpaEntity {

    @Column(name = "name", nullable = false)
    private String name;

}
