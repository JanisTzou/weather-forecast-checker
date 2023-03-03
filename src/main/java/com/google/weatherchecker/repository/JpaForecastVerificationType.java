package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.ForecastVerificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "forecast_verification_type_tbl")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JpaForecastVerificationType extends JpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private ForecastVerificationType name;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof JpaForecastVerificationType other))
            return false;

        return id != null &&
                id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }

}
