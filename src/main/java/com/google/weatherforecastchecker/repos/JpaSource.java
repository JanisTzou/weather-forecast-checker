package com.google.weatherforecastchecker.repos;


import com.google.weatherforecastchecker.scraper.Source;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "source_tbl")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JpaSource extends JpaEntityBase {

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private Source name;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof JpaSource))
            return false;

        JpaSource other = (JpaSource) o;

        return id != null &&
                id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }

}
