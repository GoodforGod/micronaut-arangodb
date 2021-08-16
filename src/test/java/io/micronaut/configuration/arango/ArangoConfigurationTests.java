package io.micronaut.configuration.arango;

import io.micronaut.configuration.arango.health.ArangoHealthConfiguration;
import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@Testcontainers
class ArangoConfigurationTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = getContainer();

    @Order(1)
    @Test
    void createConnectionWithCustomDatabaseAndDatabaseNotExistByDefault() {
        try (final ApplicationContext context = ApplicationContext.run(Map.of("arangodb.database", "custom"))) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("custom", client.database());

            final boolean databaseExists = client.db().exists();
            assertFalse(databaseExists);
        }
    }

    @Test
    void createConnectionWithCreateDatabaseIfNotExistOnStartup() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.loadBalancingStrategy", "ONE_RANDOM");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("custom", client.database());

            final ArangoHealthConfiguration healthConfiguration = context.getBean(ArangoHealthConfiguration.class);
            assertNotNull(healthConfiguration);
            assertNotNull(healthConfiguration.toString());
            assertEquals(10000, healthConfiguration.getTimeoutInMillis());
            assertEquals(2, healthConfiguration.getRetry());
            assertTrue(healthConfiguration.isEnabled());

            final boolean databaseCreated = client.db().exists();
            assertTrue(databaseCreated);
        }
    }
}
