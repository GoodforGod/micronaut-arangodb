package io.micronaut.configuration.arango;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.jackson.Key;
import com.arangodb.serde.jackson.internal.JacksonSerdeImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.serde.annotation.Serdeable;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArangoClientTests extends ArangoRunner {

    @Container
    private static final ArangoContainer<?> CONTAINER_3_11 = new ArangoContainer<>(IMAGE_3_11).withoutAuth();

    @Test
    void createConnectionWithCustomDatabaseAndDatabaseNotExistByDefault() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", "custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("custom", client.db().name());

            final boolean databaseExists = client.db().exists();
            assertFalse(databaseExists);
        }
    }

    static final class CustomSerializationExample {

        @Key
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void createWithCustomSerialization() {
        final Map<String, Object> properties = new HashMap<>();
        final String database = "custom-serialization";
        final String collection = "custom-serialization";
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", database);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final AtomicInteger counter = new AtomicInteger(0);
            context.registerSingleton(new ArangoSerde() {

                private final ArangoSerde serde = new JacksonSerdeImpl(new ObjectMapper());

                @Override
                public byte[] serialize(Object value) {
                    counter.incrementAndGet();
                    return serde.serialize(value);
                }

                @Override
                public <T> T deserialize(byte[] content, Class<T> clazz) {
                    counter.incrementAndGet();
                    return serde.deserialize(content, clazz);
                }
            });

            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals(database, client.db().name());
            assertNotNull(client.toString());

            final Boolean dbCreated = client.db().create();
            assertTrue(dbCreated);

            final CollectionEntity custom = client.db().collection(collection).create();
            assertNotNull(custom);

            final CustomSerializationExample created = new CustomSerializationExample();
            created.setId("12345");
            created.setName("bob");

            final DocumentCreateEntity<Void> createdEntity = client.db().collection(collection).insertDocument(created);
            assertNotNull(createdEntity);

            final CustomSerializationExample found = client.db().collection(collection).getDocument(created.getId(),
                    CustomSerializationExample.class);
            assertEquals(created.getId(), found.getId());
            assertEquals(created.getName(), found.getName());

            assertNotEquals(0, counter.get());
        }
    }

    @Serdeable
    static class MicronautSerializationExample {

        @Key
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void createWithDefaultMicronautSerialization() {
        final Map<String, Object> properties = new HashMap<>();
        final String database = "micronaut-serialization";
        final String collection = "micronaut-serialization";
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", database);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals(database, client.db().name());
            assertNotNull(client.toString());

            final Boolean dbCreated = client.db().create();
            assertTrue(dbCreated);

            final CollectionEntity custom = client.db().collection(collection).create();
            assertNotNull(custom);

            final MicronautSerializationExample created = new MicronautSerializationExample();
            created.setId("12345");
            created.setName("bob");

            final DocumentCreateEntity<Void> createdEntity = client.db().collection(collection).insertDocument(created);
            assertNotNull(createdEntity);

            final MicronautSerializationExample found = client.db().collection(collection).getDocument(created.getId(),
                    MicronautSerializationExample.class);
            assertEquals(created.getId(), found.getId());
            assertEquals(created.getName(), found.getName());
        }
    }

    @Test
    void createDatabaseSuccess() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", "async-custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient clientAsync = context.getBean(ArangoClient.class);
            assertEquals("async-custom", clientAsync.db().name());
            assertNotNull(clientAsync.toString());

            final Boolean created = clientAsync.db().create();
            assertTrue(created);
        }
    }

    @Test
    void createDatabaseSimpleQuerySuccess() {
        final String database = "custom123456";
        final String collection = "custom123456";

        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", database);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient clientAsync = context.getBean(ArangoClient.class);
            assertEquals(database, clientAsync.db().name());
            assertNotNull(clientAsync.toString());

            final Boolean dbCreated = clientAsync.db().create();
            assertTrue(dbCreated);

            final CollectionEntity custom = clientAsync.db().collection(collection).create();
            assertNotNull(custom);

            final BaseDocument document = new BaseDocument();
            document.setKey("1");

            final DocumentCreateEntity<Void> created = clientAsync.db().collection(collection).insertDocument(document);
            assertNotNull(created);

            final BaseDocument found = clientAsync.db().collection(collection).getDocument("1", BaseDocument.class);
            assertEquals(created.getKey(), found.getKey());
        }
    }

    @Test
    void createDatabaseForProtocolHttpSimpleQuerySuccess() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.protocol", "HTTP_JSON");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient clientAsync = context.getBean(ArangoClient.class);
            assertEquals("custom", clientAsync.db().name());
            assertNotNull(clientAsync.toString());

            final Boolean dbCreated = clientAsync.db().create();
            assertTrue(dbCreated);

            final CollectionEntity custom = clientAsync.db().collection("custom").create();
            assertNotNull(custom);

            final BaseDocument document = new BaseDocument();
            document.setKey("1");

            final DocumentCreateEntity<Void> created = clientAsync.db().collection("custom").insertDocument(document);
            assertNotNull(created);

            final BaseDocument found = clientAsync.db().collection("custom").getDocument("1", BaseDocument.class);
            assertEquals(created.getKey(), found.getKey());
        }
    }

    @Test
    void createDatabaseSyncSuccess() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", "sync-custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("sync-custom", client.db().name());
            assertNotNull(client.toString());
            assertNotNull(client.db());
            assertNotNull(client.accessor());
            assertNotNull(client.toString());

            final Boolean created = client.db().create();
            assertTrue(created);
        }
    }

    @Test
    void createConfigurationForHostsAsListValidClient() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertTrue(client.db().exists());
        }
    }

    @Test
    void createConfigurationForHostsAsStringValidClient() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts",
                String.format("localhost:%s,localhost:%s", CONTAINER_3_11.getPort(), CONTAINER_3_11.getPort()));

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertTrue(client.db().exists());
        }
    }
}
