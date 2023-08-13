package io.micronaut.configuration.arango;

import com.arangodb.ArangoDBException;
import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 29.3.2020
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArangoDatabaseInitializerTests extends ArangoRunner {

    @Container
    private static final ArangoContainer<?> CONTAINER_3_11 = new ArangoContainer<>(IMAGE_3_11).withoutAuth();

    @Order(1)
    @Test
    void createConnectionWithCreateDatabaseIfNotExistOnStartup() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.create-database-if-not-exist", true);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("custom", client.db().name());
            assertTrue(client.db().exists());
        }
    }

    @Order(2)
    @Test
    void createdDatabaseInitIsSkipped() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.create-database-if-not-exist", true);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("custom", client.db().name());
            assertTrue(client.db().exists());
        }
    }

    @Order(3)
    @Test
    void defaultDatabaseInitializationIsSkipped() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", ArangoSettings.SYSTEM_DATABASE);
        properties.put("arangodb.create-database-if-not-exist", true);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals(ArangoSettings.SYSTEM_DATABASE, client.db().name());
            assertTrue(client.db().exists());
        }
    }

    @Order(4)
    @Test
    void databaseCreationIsOff() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", "nodata");
        properties.put("arangodb.create-database-if-not-exist", false);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals("nodata", client.db().name());
            assertFalse(client.db().exists());
        }
    }

    @Order(5)
    @Test
    void startUpForContextFailsOnTimeout() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.hosts", List.of("localhost:" + 8566));
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.create-database-timeout", Duration.ofMillis(1));

        try (ApplicationContext context = ApplicationContext.run(properties)) {
            fail("Should not happen!");
        } catch (Exception e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause().getCause() instanceof ArangoDBException);
        }
    }

    @Order(6)
    @Test
    void startUpForContextFailsOnConnectionError() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.hosts", List.of("localhost:" + 8566));
        properties.put("arangodb.timeout", Duration.ofSeconds(1));
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.create-database-timeout", Duration.ofSeconds(10));

        try (ApplicationContext context = ApplicationContext.run(properties)) {
            fail("Should not happen!");
        } catch (Exception e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause().getCause() instanceof ArangoDBException);
        }
    }

    @Order(7)
    @Test
    void databaseCreateAsync() {
        final Map<String, Object> properties = new HashMap<>();
        final String database = "asyncdb";
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.database", database);
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.create-database-async", true);
        properties.put("arangodb.create-database-timeout", Duration.ofSeconds(10));

        try (ApplicationContext context = ApplicationContext.run(properties)) {
            Thread.sleep(2000);

            final ArangoClient client = context.getBean(ArangoClient.class);
            assertEquals(database, client.db().name());
            assertTrue(client.db().exists());

            final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);
            assertTrue(configuration.isCreateDatabaseIfNotExist());
            assertTrue(configuration.isCreateDatabaseAsync());
        } catch (InterruptedException e) {
            fail(e);
        }
    }
}
