package io.micronaut.configuration.arango;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.annotation.MicronautTest;
import io.testcontainers.arangodb.containers.ArangoContainer;
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
}
