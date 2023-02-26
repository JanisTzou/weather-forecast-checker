package com.google.weatherforecastchecker.repos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaLocationRepositoryImpl implements JpaLocationRepositorySavingNewOnly {

    @Autowired
    private ApplicationContext context;

    @Override
    public JpaLocation saveIfNewAndGet(JpaLocation entity) {
        if (entity == null || entity.getName() == null) {
            return null;
        }
        JpaLocationRepository repository = context.getBean(JpaLocationRepository.class);
        Optional<JpaLocation> opt = repository.findByName(entity.getName());
        return opt.orElseGet(() -> repository.save(entity));
    }

}
