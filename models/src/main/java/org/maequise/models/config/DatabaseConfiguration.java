package org.maequise.models.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
@EntityScan("org.maequise.models.entities")
@AllArgsConstructor
public class DatabaseConfiguration {
    private Environment env;

    @Bean
    public DataSource dataSource() {
        return new HikariDataSource(dataSourceConfig());
    }

    private HikariConfig dataSourceConfig() {
        var conf = new HikariConfig();

        conf.setJdbcUrl(env.getProperty("datasource.url"));
        conf.setUsername(env.getProperty("datasource.username"));
        conf.setPassword(env.getProperty("datasource.password"));

        var schema = env.getProperty("datasource.schema");

        if(schema != null){
            conf.setSchema(schema);
        }

        return conf;
    }
}
