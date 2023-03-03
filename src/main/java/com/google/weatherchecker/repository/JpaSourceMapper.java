package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Source;
import org.springframework.stereotype.Component;

@Deprecated // TODO maybe not needed so much ?
@Component
public class JpaSourceMapper {

    public Source toDomain(JpaSource jpaSource) {
        return jpaSource.getName();
    }

    public JpaSource toEntity(Source source) {
        return new JpaSource(source);
    }
}
