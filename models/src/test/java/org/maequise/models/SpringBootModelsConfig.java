package org.maequise.models;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.maequise.models"})
public class SpringBootModelsConfig {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootModelsConfig.class);
    }
}
