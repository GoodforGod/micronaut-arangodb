package io.micronaut.configuration.arango;

import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@Testcontainers
class ArangoClientTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = getContainer()
            .withFixedPort(8528);

    @Test
    void createConnectionWithCustomDatabaseAndDatabaseNotExistByDefault() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", 8528);
        properties.put("arangodb.database", "custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("custom", client.db().name());

            final boolean databaseExists = client.db().exists();
            assertFalse(databaseExists);
        }
    }

    @Test
    void createDatabaseSuccess() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", 8528);
        properties.put("arangodb.database", "async-custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync clientAsync = context.getBean(ArangoClientAsync.class);
            assertEquals("async-custom", clientAsync.db().name());
            assertNotNull(clientAsync.toString());

            final Boolean created = clientAsync.db().create().join();
            assertTrue(created);
        }
    }

    @Test
    void createDatabaseSyncSuccess() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", 8528);
        properties.put("arangodb.database", "sync-custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("sync-custom", client.db().name());
            assertNotNull(client.toString());
            assertNotNull(client.db());
            assertNotNull(client.properties());
            assertNotNull(client.accessor());

            final Boolean created = client.db().create();
            assertTrue(created);
        }
    }

    @Test
    void createConfigurationForHostsAsListValidClient() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:8528"));

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertTrue(client.db().exists());
        }
    }

    @Test
    void createConfigurationForHostsAsStringValidClient() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", "localhost:8528,localhost:8528");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertTrue(client.db().exists());
        }
    }
}
