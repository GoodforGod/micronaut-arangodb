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
    private ArangoClientAsync client;

    @Inject
    private ArangoClient syncClient;

    @Test
    void createDatabaseSuccess() {
        assertEquals("custom", client.database());

        final Boolean created = client.db().create().join();
        assertTrue(created);
    }

    @Test
    void createDatabaseSyncSuccess() {
        assertEquals("custom", syncClient.database());

        final Boolean created = syncClient.accessor().db("sync-custom").create();
        assertTrue(created);
    }
}
