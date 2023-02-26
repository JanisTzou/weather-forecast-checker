package com.google.weatherchecker.repository;


import com.google.weatherchecker.model.Provider;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "provider_tbl")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JpaProvider extends JpaEntityBase {

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private Provider name;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof JpaProvider))
            return false;

        JpaProvider other = (JpaProvider) o;

        return id != null &&
                id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }

}
