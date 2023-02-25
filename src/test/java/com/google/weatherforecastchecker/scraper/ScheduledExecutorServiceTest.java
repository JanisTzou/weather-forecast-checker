package com.google.weatherforecastchecker.scraper;

import com.google.weatherforecastchecker.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ScheduledExecutorServiceTest {

    private static final Logger log = LogManager.getLogger(ScheduledExecutorServiceTest.class);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        new ScheduledExecutorServiceTest().schedule();
    }

    public void schedule() {
        schedule(1);
        schedule(2);
    }

    private void schedule(int no) {
        executor.scheduleAtFixedRate(
                () -> {
                    log.info("Running ... " + no);
                    Utils.sleep(1000);
                },
                5,
                10,
                TimeUnit.SECONDS
        );
    }


}
