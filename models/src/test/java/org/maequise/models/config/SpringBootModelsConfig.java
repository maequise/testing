package org.maequise.models.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SpringBootApplication(scanBasePackages = {"org.maequise.models"})
public class SpringBootModelsConfig {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootModelsConfig.class);
    }
}
