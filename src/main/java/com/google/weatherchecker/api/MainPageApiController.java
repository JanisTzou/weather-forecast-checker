package com.google.weatherchecker.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Log4j2
public class MainPageApiController {

    private final MainPageApiService mainPageApiService;

    @GetMapping(value = "/main-page", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MainPageDto> getComparisons(@RequestParam(value = "county", required = false) String county,
                                                      @RequestParam(value = "region", required = false) String region) {
        Optional<MainPageDto> mainPage = mainPageApiService.getMainPage(region, county);
        if (mainPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.ok(mainPage.get());
        }
    }

}
