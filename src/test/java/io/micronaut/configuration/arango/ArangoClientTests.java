package io.micronaut.configuration.arango;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MicronautTest;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@Property(name = "arangodb.database", value = "custom")
@MicronautTest
@Testcontainers
class ArangoClientTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withoutAuth();

    @Inject
    private ArangoClientAsync clientAsync;

    @Inject
    private ArangoClient client;

    @Test
    void createDatabaseSuccess() {
        assertEquals("custom", clientAsync.database());

        final Boolean created = clientAsync.db().create().join();
        assertTrue(created);

        assertNotNull(clientAsync.toString());
    }

    @Test
    void createDatabaseSyncSuccess() {
        assertEquals("custom", client.database());

        final Boolean created = client.accessor().db("sync-custom").create();
        assertTrue(created);

        assertNotNull(client.toString());
    }
}
