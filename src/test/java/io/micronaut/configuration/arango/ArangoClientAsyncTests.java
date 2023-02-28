package io.micronaut.configuration.arango;

import com.arangodb.ArangoDBException;
import com.arangodb.async.ArangoDBAsync;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.VPackSlice;
import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final ArangoContainer CONTAINER = getContainer()
            .withoutAuth()
            .withFixedPort(ArangoContainer.DEFAULT_PORT);

    @Test
    void createConnectionWithCustomDatabaseAndDatabaseNotExistByDefault() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", CONTAINER.getPort());
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
        properties.put("arangodb.port", CONTAINER.getPort());
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
    void createWithCustomSerialization() {
        final Map<String, Object> properties = new HashMap<>();
        final String database = "custom-serialization";
        final String collection = "custom-serialization";
        properties.put("arangodb.port", CONTAINER.getPort());
        properties.put("arangodb.database", database);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final AtomicInteger counter = new AtomicInteger();

            context.registerSingleton(new ArangoSerialization() {

                private final ArangoJack jack = new ArangoJack();

                @Override
                public <T> T deserialize(VPackSlice vpack, Type type) throws ArangoDBException {
                    counter.incrementAndGet();
                    return jack.deserialize(vpack, type);
                }

                @Override
                public VPackSlice serialize(Object entity) throws ArangoDBException {
                    counter.incrementAndGet();
                    return jack.serialize(entity);
                }

                @Override
                public VPackSlice serialize(Object entity, Options options) throws ArangoDBException {
                    counter.incrementAndGet();
                    return jack.serialize(entity, options);
                }
            });

            final ArangoClientAsync clientAsync = context.getBean(ArangoClientAsync.class);
            assertEquals(database, clientAsync.db().dbName().get());
            assertNotNull(clientAsync.toString());

            final Boolean dbCreated = clientAsync.db().create().join();
            assertTrue(dbCreated);

            final CollectionEntity custom = clientAsync.db().collection(collection).create().join();
            assertNotNull(custom);

            final BaseDocument document = new BaseDocument();
            document.setKey("1");

            final DocumentCreateEntity<BaseDocument> created = clientAsync.db().collection(collection).insertDocument(document)
                    .join();
            assertNotNull(created);

            final BaseDocument found = clientAsync.db().collection(collection).getDocument("1", BaseDocument.class).join();
            assertEquals(created.getKey(), found.getKey());

            assertNotEquals(0, counter.get());
        }
    }

    @Test
    void createDatabaseSimpleQuerySuccess() {
        final String database = "custom12";
        final String collection = "custom12";

        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", CONTAINER.getPort());
        properties.put("arangodb.database", database);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync clientAsync = context.getBean(ArangoClientAsync.class);
            assertEquals(database, clientAsync.db().dbName().get());
            assertNotNull(clientAsync.toString());

            final Boolean dbCreated = clientAsync.db().create().join();
            assertTrue(dbCreated);

            final CollectionEntity custom = clientAsync.db().collection(collection).create().join();
            assertNotNull(custom);

            final BaseDocument document = new BaseDocument();
            document.setKey("1");

            final DocumentCreateEntity<BaseDocument> created = clientAsync.db().collection(collection).insertDocument(document)
                    .join();
            assertNotNull(created);

            final BaseDocument found = clientAsync.db().collection(collection).getDocument("1", BaseDocument.class).join();
            assertEquals(created.getKey(), found.getKey());
        }
    }

    @Test
    void createDatabaseForProtocolHttpSimpleQuerySuccess() {
        final String database = "custom1234";
        final String collection = "custom1234";

        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", CONTAINER.getPort());
        properties.put("arangodb.database", database);
        properties.put("arangodb.protocol", "HTTP_JSON");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync clientAsync = context.getBean(ArangoClientAsync.class);
            assertEquals(database, clientAsync.db().dbName().get());
            assertNotNull(clientAsync.toString());

            final Boolean dbCreated = clientAsync.db().create().join();
            assertTrue(dbCreated);

            final CollectionEntity custom = clientAsync.db().collection(collection).create().join();
            assertNotNull(custom);

            final BaseDocument document = new BaseDocument();
            document.setKey("1");

            final DocumentCreateEntity<BaseDocument> created = clientAsync.db().collection(collection).insertDocument(document)
                    .join();
            assertNotNull(created);

            final BaseDocument found = clientAsync.db().collection(collection).getDocument("1", BaseDocument.class).join();
            assertEquals(created.getKey(), found.getKey());
        }
    }

    @Test
    void asyncAccessorIsAvailable() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", CONTAINER.getPort());
        properties.put("arangodb.database", "custom");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoDBAsync accessor = context.getBean(ArangoDBAsync.class);
            assertNotNull(accessor);
            assertTrue(accessor.db().exists().join());
        }
    }
}
