package io.micronaut.configuration.arango;

import io.micronaut.configuration.arango.health.ArangoHealthConfiguration;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.exceptions.ConfigurationException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArangoConfigurationTests extends ArangoRunner {

    @Test
    void createConnectionWithCreateDatabaseIfNotExistOnStartup() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + 8528));
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.connection-max", 111);
        properties.put("arangodb.acquire-host-list", true);
        properties.put("arangodb.load-balancing-strategy", "ONE_RANDOM");
        properties.put("arangodb.jwt", "123");
        properties.put("arangodb.responseQueueTimeSamples", 123);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);
            assertEquals(1, configuration.getHosts().size());
            assertEquals("custom", configuration.getDatabase());
            assertEquals("ONE_RANDOM", configuration.getLoadBalancingStrategy().name());
            assertEquals(111, configuration.getConnectionMax());
            assertTrue(configuration.getAcquireHostList());
            assertEquals("123", configuration.getJwt());
            assertEquals(123, configuration.getResponseQueueTimeSamples());

            final ArangoHealthConfiguration healthConfiguration = context.getBean(ArangoHealthConfiguration.class);
            assertNotNull(healthConfiguration);
            assertNotNull(healthConfiguration.toString());
            assertEquals(Duration.ofSeconds(5), healthConfiguration.getTimeout());
            assertEquals(2, healthConfiguration.getRetry());
            assertTrue(healthConfiguration.isEnabled());
        }
    }

    @Test
    void configurationDriverTimeoutNegativeFail() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.timeout", Duration.ofSeconds(-1));

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            context.getBean(ArangoConfiguration.class);
            fail("Should not happen");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConfigurationException);
        }
    }

    @Test
    void configurationCreateTimeoutNegativeFail() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.create-database-timeout", Duration.ofSeconds(-1));

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            context.getBean(ArangoConfiguration.class);
            fail("Should not happen");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConfigurationException);
        }
    }

    @Test
    void createConfigurationForHostsAsString() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", "localhost:8528,localhost:8528");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);
            assertNotNull(configuration);
            assertNotNull(configuration.getHosts());
            assertEquals(2, configuration.getHosts().size());
            assertTrue(configuration.getHosts().contains("localhost:8528"));
        }
    }

    @Test
    void createConfigurationForHostsAsList() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:8528", "localhost:8528"));

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);
            assertNotNull(configuration);
            assertNotNull(configuration.getHosts());
            assertEquals(2, configuration.getHosts().size());
            assertTrue(configuration.getHosts().contains("localhost:8528"));
        }
    }

    @Test
    void healthConfigurationBuild() {
        final ArangoHealthConfiguration healthConfiguration = new ArangoHealthConfiguration();
        healthConfiguration.setRetry(2);
        healthConfiguration.setTimeout(Duration.ofSeconds(1));
        assertEquals(2, healthConfiguration.getRetry());
        assertEquals(Duration.ofSeconds(1), healthConfiguration.getTimeout());
    }

    @Test
    void healthConfigurationRetryFail() {
        try {
            final ArangoHealthConfiguration healthConfiguration = new ArangoHealthConfiguration();
            healthConfiguration.setRetry(-1);
            fail("Should not happen!");
        } catch (Exception e) {
            assertTrue(e instanceof ConfigurationException);
        }
    }

    @Test
    void healthConfigurationTimeoutFail() {
        try {
            final ArangoHealthConfiguration healthConfiguration = new ArangoHealthConfiguration();
            healthConfiguration.setTimeout(Duration.ofSeconds(-1));
            fail("Should not happen!");
        } catch (Exception e) {
            assertTrue(e instanceof ConfigurationException);
        }
    }
}
