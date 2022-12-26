package org.maequise.models.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigurationIntegrationTest {
    @Autowired
    private DataSource dataSource;

    @Test
    void testDatasource() {
        assertNotNull(dataSource);
    }
}
