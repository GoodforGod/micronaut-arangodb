package io.micronaut.configuration.arango;

import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@Testcontainers
class ArangoClientTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = getContainer();

    @Test
    void createDatabaseSuccess() {
        try (final ApplicationContext context = ApplicationContext.run(Collections.singletonMap("arangodb.database", "async-custom"))) {
            final ArangoClientAsync clientAsync = context.getBean(ArangoClientAsync.class);
            assertEquals("async-custom", clientAsync.database());
            assertNotNull(clientAsync.toString());

            final Boolean created = clientAsync.db().create().join();
            assertTrue(created);
        }
    }

    @Test
    void createDatabaseSyncSuccess() {
        try (final ApplicationContext context = ApplicationContext.run(Collections.singletonMap("arangodb.database", "sync-custom"))) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("sync-custom", client.database());
            assertNotNull(client.toString());

            final Boolean created = client.db().create();
            assertTrue(created);
        }
    }
}
