package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@Testcontainers
class ArangoClientAsyncTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = getContainer()
            .withoutAuth()
            .withFixedPort(ArangoContainer.DEFAULT_PORT);

    @Test
    void createConnectionWithCustomDatabaseAndDatabaseNotExistByDefault() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
            assertEquals("custom", client.db().dbName().get());

            final Boolean databaseExists = client.db().exists().join();
            assertFalse(databaseExists);
        }
    }

    @Test
    void createConnectionWithCreateDatabaseIfNotExistOnStartup() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.load-balancing-strategy", "ONE_RANDOM");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
            assertEquals("custom", client.db().dbName().get());
            assertNotNull(client.db());
            assertNotNull(client.properties());
            assertNotNull(client.accessor());
            assertNotNull(client.toString());

            final Boolean databaseCreated = client.db().exists().join();
            assertTrue(databaseCreated);
        }
    }

    @Test
    void asyncAccessorIsAvailable() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoDBAsync accessor = context.getBean(ArangoDBAsync.class);
            assertNotNull(accessor);
            assertTrue(accessor.db().exists().join());
        }
    }
}
