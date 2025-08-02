package com.example.farm4u.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiPrefixConfig implements WebMvcConfigurer{

    @Override
    public void configurePathMatch(PathMatchConfigurer conf){
        conf.addPathPrefix("/api/v0", HandlerTypePredicate.forAnnotation(RestController.class));
    }

}
