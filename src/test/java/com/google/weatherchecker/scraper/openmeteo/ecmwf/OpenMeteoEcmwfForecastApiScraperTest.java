package com.google.weatherchecker.scraper.openmeteo.ecmwf;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.weatherchecker.ApplicationConfig;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

// TODO to make this work .... https://www.baeldung.com/spring-testing-separate-data-source
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
class OpenMeteoEcmwfForecastApiScraperTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void testDeserialisation() throws IOException {

        String file = "/data/openmeteo/ecmwf/open-meteo-ecmwf-response.json";
        String json = IOUtils.toString(this.getClass().getResourceAsStream(file), "UTF-8");

        OpenMeteoEcmwfForecastApiScraper.ForecastDto forecast = jsonMapper.readValue(json, OpenMeteoEcmwfForecastApiScraper.ForecastDto.class);

        assertNotNull(forecast);
        assertNotNull(forecast.getHourly());
        assertNotNull(forecast.getHourly().getTotalCloudCover());
        assertEquals(80,forecast.getHourly().getTotalCloudCover().size());
        assertNotNull(forecast.getHourly().getTime());
        assertEquals(80, forecast.getHourly().getTime().size());
    }

}

