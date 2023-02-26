package com.google.weatherchecker.scraper.meteoblue;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.weatherchecker.ApplicationConfig;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
class MeteoblueForecastApiScraperTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void testDeserialisation() throws IOException {

        String file = "/data/meteoblue/hourly-cloud-coverage-response.json";
        String json = IOUtils.toString(this.getClass().getResourceAsStream(file), "UTF-8");

        MeteoblueForecastApiScraper.ForecastDto forecast = jsonMapper.readValue(json, MeteoblueForecastApiScraper.ForecastDto.class);

        assertNotNull(forecast.getMetadata());
        assertNotNull(forecast.getMetadata().getModelRunUtc());
        assertNotNull(forecast.getMetadata().getModelRunUpdateTimeUtc());

        assertNotNull(forecast);
        assertNotNull(forecast.getData1Hr());
        assertNotNull(forecast.getData1Hr().getTotalCloudCover());
        assertEquals(182,forecast.getData1Hr().getTotalCloudCover().size());
        assertNotNull(forecast.getData1Hr().getTime());
        assertEquals(182, forecast.getData1Hr().getTime().size());
    }

}
