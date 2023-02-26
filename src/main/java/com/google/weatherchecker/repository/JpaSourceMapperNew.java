package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Source;
import org.springframework.stereotype.Component;

@Component
public class JpaSourceMapperNew {

    public Source toDomain(JpaSource jpaSource) {
        return jpaSource.getName();
    }

    public JpaSource toEntity(Source source) {
        return new JpaSource(source);
    }

}
