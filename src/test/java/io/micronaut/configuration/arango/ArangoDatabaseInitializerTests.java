package io.micronaut.configuration.arango;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.annotation.MicronautTest;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 29.3.2020
 */
@MicronautTest
@Testcontainers
class ArangoDatabaseInitializerTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withoutAuth();

    @Order(1)
    @Test
    void createConnectionWithCreateDatabaseIfNotExistOnStartup() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.createDatabaseIfNotExist", true);

        final ApplicationContext context = ApplicationContext.run(properties);

        final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
        assertEquals("custom", client.database());
        assertTrue(client.db().exists().join());
    }

    @Order(2)
    @Test
    void defaultDatabaseInitializationIsSkipped() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", ArangoSettings.DEFAULT_DATABASE);
        properties.put("arangodb.createDatabaseIfNotExist", true);

        final ApplicationContext context = ApplicationContext.run(properties);

        final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
        assertEquals(ArangoSettings.DEFAULT_DATABASE, client.database());
        assertTrue(client.db().exists().join());
    }

    @Order(3)
    @Test
    void createdDatabaseInitIsSkipped() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.createDatabaseIfNotExist", true);

        final ApplicationContext context = ApplicationContext.run(properties);

        final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
        assertEquals("custom", client.database());
        assertTrue(client.db().exists().join());
    }

    @Order(4)
    @Test
    void databaseCreationIsOff() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "nodata");
        properties.put("arangodb.createDatabaseIfNotExist", false);

        final ApplicationContext context = ApplicationContext.run(properties);

        final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
        assertEquals("nodata", client.database());
        assertFalse(client.db().exists().join());
    }
}
