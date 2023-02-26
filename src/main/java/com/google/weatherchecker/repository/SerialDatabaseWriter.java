package com.google.weatherchecker.repository;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SerialDatabaseWriter {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void execute(Runnable runnable) {
        executor.submit(runnable);
    }

}
