package org.maequise.models;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"org.maequise.models"})
@EntityScan("org.maequise.models.entities")
public class SpringBootModelsConfig {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootModelsConfig.class);
    }
}
