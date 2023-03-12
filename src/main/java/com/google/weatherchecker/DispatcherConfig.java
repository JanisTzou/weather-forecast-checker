package com.google.weatherchecker;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DispatcherConfig implements WebMvcConfigurer {

//    @Bean
//    public CommonsMultipartResolver multipartResolver() {
//        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
//        resolver.setDefaultEncoding("utf-8");
//        return resolver;
//    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("GET")
                        .allowedHeaders("*")
                        .allowedOrigins(
                                "http://localhost:8080",
                                "http://localhost:8081",
                                "https://chytrekalkulacky.cz",
                                "https://www.chytrekalkulacky.cz"
                        );
            }


        };
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/Images/**").addResourceLocations("file:/Users/janis/Projects_Data/ProjectRET/Apps/RetApp/Images/");
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
        // https://www.baeldung.com/spring-mvc-static-resources
    }

}
