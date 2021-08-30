package io.micronaut.configuration.arango;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.exceptions.ApplicationStartupException;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 29.3.2020
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class ArangoDatabaseInitializerTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = getContainer();

    @Order(1)
    @Test
    void createConnectionWithCreateDatabaseIfNotExistOnStartup() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.create-database-if-not-exist", true);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
            assertEquals("custom", client.db().name());
            assertTrue(client.db().exists().join());
        }
    }

    @Order(2)
    @Test
    void createdDatabaseInitIsSkipped() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.create-database-if-not-exist", true);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
            assertEquals("custom", client.db().name());
            assertTrue(client.db().exists().join());
        }
    }

    @Order(3)
    @Test
    void defaultDatabaseInitializationIsSkipped() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", ArangoSettings.SYSTEM_DATABASE);
        properties.put("arangodb.create-database-if-not-exist", true);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
            assertEquals(ArangoSettings.SYSTEM_DATABASE, client.db().name());
            assertTrue(client.db().exists().join());
        }
    }

    @Order(4)
    @Test
    void databaseCreationIsOff() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "nodata");
        properties.put("arangodb.create-database-if-not-exist", false);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
            assertEquals("nodata", client.db().name());
            assertFalse(client.db().exists().join());
        }
    }

    @Order(5)
    @Test
    void startUpForContextFailsOnTimeout() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.port", 8566);
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.create-database-timeout-in-millis", 1);

        try (ApplicationContext context = ApplicationContext.run(properties)) {
            fail("Should not happen!");
        } catch (Exception e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause().getCause() instanceof ApplicationStartupException);
        }
    }

    @Order(6)
    @Test
    void startUpForContextFailsOnConnectionError() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.port", 8566);
        properties.put("arangodb.timeout", 1000);
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.create-database-timeout-in-millis", 10000);

        try (ApplicationContext context = ApplicationContext.run(properties)) {
            fail("Should not happen!");
        } catch (Exception e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause().getCause() instanceof ApplicationStartupException);
        }
    }

    @Order(7)
    @Test
    void databaseCreateAsync() {
        final Map<String, Object> properties = new HashMap<>();
        final String database = "asyncdb";
        properties.put("arangodb.database", database);
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.create-database-async", true);
        properties.put("arangodb.create-database-timeout-in-millis", 10000);

        try (ApplicationContext context = ApplicationContext.run(properties)) {
            Thread.sleep(2000);

            final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
            assertEquals(database, client.db().name());
            assertTrue(client.db().exists().join());

            final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);
            assertTrue(configuration.isCreateDatabaseIfNotExist());
            assertTrue(configuration.isCreateDatabaseAsync());
        } catch (InterruptedException e) {
            fail(e);
        }
    }
}
