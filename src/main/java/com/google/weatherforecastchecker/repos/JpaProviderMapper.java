package com.google.weatherforecastchecker.repos;

import com.google.weatherforecastchecker.scraper.Provider;
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
