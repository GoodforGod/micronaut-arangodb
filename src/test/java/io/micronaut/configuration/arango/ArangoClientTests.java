package io.micronaut.configuration.arango;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DocumentCreateEntity;
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
            .withoutAuth()
            .withFixedPort(8528);

    @Test
    void createConnectionWithCustomDatabaseAndDatabaseNotExistByDefault() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", 8528);
        properties.put("arangodb.database", "custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("custom", client.db().dbName().get());

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
            assertEquals("async-custom", clientAsync.db().dbName().get());
            assertNotNull(clientAsync.toString());

            final Boolean created = clientAsync.db().create().join();
            assertTrue(created);
        }
    }

    @Test
    void createDatabaseSimpleQuerySuccess() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", 8528);
        properties.put("arangodb.database", "custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync clientAsync = context.getBean(ArangoClientAsync.class);
            assertEquals("custom", clientAsync.db().dbName().get());
            assertNotNull(clientAsync.toString());

            final Boolean dbCreated = clientAsync.db().create().join();
            assertTrue(dbCreated);

            final CollectionEntity custom = clientAsync.db().collection("custom").create().join();
            assertNotNull(custom);

            final BaseDocument document = new BaseDocument();
            document.setKey("1");

            final DocumentCreateEntity<BaseDocument> created = clientAsync.db().collection("custom").insertDocument(document)
                    .join();
            assertNotNull(created);

            final BaseDocument found = clientAsync.db().collection("custom").getDocument("1", BaseDocument.class).join();
            assertEquals(created.getKey(), found.getKey());
        }
    }

    @Test
    void createDatabaseForProtocolHttpSimpleQuerySuccess() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", 8528);
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.protocol", "HTTP_JSON");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync clientAsync = context.getBean(ArangoClientAsync.class);
            assertEquals("custom", clientAsync.db().dbName().get());
            assertNotNull(clientAsync.toString());

            final Boolean dbCreated = clientAsync.db().create().join();
            assertTrue(dbCreated);

            final CollectionEntity custom = clientAsync.db().collection("custom").create().join();
            assertNotNull(custom);

            final BaseDocument document = new BaseDocument();
            document.setKey("1");

            final DocumentCreateEntity<BaseDocument> created = clientAsync.db().collection("custom").insertDocument(document)
                    .join();
            assertNotNull(created);

            final BaseDocument found = clientAsync.db().collection("custom").getDocument("1", BaseDocument.class).join();
            assertEquals(created.getKey(), found.getKey());
        }
    }

    @Test
    void createDatabaseSyncSuccess() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", 8528);
        properties.put("arangodb.database", "sync-custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("sync-custom", client.db().dbName().get());
            assertNotNull(client.toString());
            assertNotNull(client.db());
            assertNotNull(client.properties());
            assertNotNull(client.accessor());
            assertNotNull(client.toString());

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
