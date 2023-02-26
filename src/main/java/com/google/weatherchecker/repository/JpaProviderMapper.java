package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Provider;
import org.springframework.stereotype.Component;

@Component
public class JpaProviderMapper {

    public Provider toDomain(JpaProvider jpaProvider) {
        return jpaProvider.getName();
    }

    public JpaProvider toEntity(Provider provider) {
        return new JpaProvider(provider);
    }

}
