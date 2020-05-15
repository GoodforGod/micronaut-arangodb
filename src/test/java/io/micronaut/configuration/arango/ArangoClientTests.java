package io.micronaut.configuration.arango;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MicronautTest;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

/**
 * Description in progress
 *
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
    private ArangoClient client;

    @Inject
    private ArangoClientSync syncClient;

    @Test
    void createDatabaseSuccess() {
        assertEquals("custom", client.getDatabase());

        final Boolean created = client.db().create().join();
        assertTrue(created);
    }

    @Test
    void createDatabaseSyncSuccess() {
        assertEquals("custom", syncClient.getDatabase());

        final Boolean created = syncClient.accessor().db("sync-custom").create();
        assertTrue(created);
    }
}
