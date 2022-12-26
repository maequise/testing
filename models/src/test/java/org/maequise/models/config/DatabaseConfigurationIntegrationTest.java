package org.maequise.models.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

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
