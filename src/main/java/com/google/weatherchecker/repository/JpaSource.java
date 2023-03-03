package com.google.weatherchecker.repository;


import com.google.weatherchecker.model.Source;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "source_tbl")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JpaSource extends JpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private Source name;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof JpaSource other))
            return false;

        return id != null &&
                id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }

}
