package org.maequise.models.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigurationTest {
    private DatabaseConfiguration databaseConfiguration;

    @BeforeEach
    void init() {
        this.databaseConfiguration = mock(DatabaseConfiguration.class);
    }

    @Test
    void testCreationBean() {
        when(this.databaseConfiguration.dataSource())
                .thenReturn(new HikariDataSource());

        assertNotNull(this.databaseConfiguration.dataSource());
    }
}
