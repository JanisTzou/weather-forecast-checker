package com.google.weatherforecastchecker.repos;

import com.google.weatherforecastchecker.scraper.Source;
import org.springframework.stereotype.Component;

@Component
public class JpaSourceMapper {

    public Source toDomain(JpaSource jpaSource) {
        return jpaSource.getName();
    }

    public JpaSource toEntity(Source source) {
        return new JpaSource(source);
    }
}