package org.maequise.models.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigurationTest {
    private DatabaseConfiguration databaseConfiguration;

    private Environment env;

    @BeforeEach
    void init() {
        this.databaseConfiguration = mock(DatabaseConfiguration.class, CALLS_REAL_METHODS);
        this.env = mock(Environment.class);

        ReflectionTestUtils.setField(databaseConfiguration, "env", env);
    }

    @Test
    void testDataSource() {
        when(env.getProperty("datasource.url"))
                .thenReturn("jdbc:h2:mem:test");

        when(env.getProperty("datasource.username"))
                .thenReturn("username");

        when(env.getProperty("datasource.password"))
                .thenReturn("password");

        assertNotNull(databaseConfiguration.dataSource());
    }

/*    @Test
    void testDataSourceWithSchemaFilled() {
        when(env.getProperty("datasource.url"))
                .thenReturn("jdbc:h2:mem:test");

        when(env.getProperty("datasource.username"))
                .thenReturn("username");

        when(env.getProperty("datasource.password"))
                .thenReturn("password");

        when(env.getProperty("datasource.schema"))
                .thenReturn("s");

        assertNotNull(databaseConfiguration.dataSource());
    }*/

    @Test
    void testCreationHibernateProperties() {
        when(databaseConfiguration.hibernateProperties())
                .thenReturn(new HibernateProperties());

        assertNotNull(databaseConfiguration.hibernateProperties());
    }

    @Test
    void testCreationJpaProperties() {
        when(databaseConfiguration.jpaProperties())
                .thenReturn(new JpaProperties());

        assertNotNull(databaseConfiguration.jpaProperties());
    }
}
