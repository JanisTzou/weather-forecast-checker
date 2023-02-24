package com.google.weatherforecastchecker.scraper.forecast;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.weatherforecastchecker.Config;
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
@ContextConfiguration(classes = { Config.class })
class MeteoblueApiScraperTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void testDeserialisation() throws IOException {

        String file = "/data/meteoblue/hourly-cloud-coverage-response.json";
        String json = IOUtils.toString(this.getClass().getResourceAsStream(file), "UTF-8");

        MeteoblueApiScraper.ForecastDto forecast = jsonMapper.readValue(json, MeteoblueApiScraper.ForecastDto.class);

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
