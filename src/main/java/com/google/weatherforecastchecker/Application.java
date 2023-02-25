package com.google.weatherforecastchecker;

import com.google.weatherforecastchecker.scraper.ScrapingManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = Application.class)
@Log4j2
public class Application {

	private final ApplicationContext context;

	public Application(ApplicationContext context) {
		this.context = context;
	}


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startScraping() {
		ScrapingManager scrapingManager = context.getBean(ScrapingManager.class);
		scrapingManager.startScrapingForecasts();
	}

}
