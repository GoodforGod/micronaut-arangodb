package io.micronaut.configuration.arango;

import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@Testcontainers
class ArangoClientSyncConfigurationTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withoutAuth();

    @Order(1)
    @Test
    void createConnectionWithCustomDatabaseAndDatabaseNotExistByDefault() {
        final ApplicationContext context = ApplicationContext.run(Collections.singletonMap("arangodb.database", "custom"));

        final ArangoClientSync client = context.getBean(ArangoClientSync.class);
        assertEquals("custom", client.getDatabase());

        final boolean databaseExists = client.db().exists();
        assertFalse(databaseExists);
    }

    @Test
    void createConnectionWithCreateDatabaseIfNotExistOnStartup() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.createDatabaseIfNotExist", true);
        properties.put("arangodb.loadBalancingStrategy", "ONE_RANDOM");

        final ApplicationContext context = ApplicationContext.run(properties);

        final ArangoClientSync client = context.getBean(ArangoClientSync.class);
        assertEquals("custom", client.getDatabase());

        final boolean databaseCreated = client.db().exists();
        assertTrue(databaseCreated);
    }
}
