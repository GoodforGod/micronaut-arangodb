package io.micronaut.configuration.arango;

import io.micronaut.configuration.arango.health.ArangoHealthConfiguration;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.exceptions.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@Testcontainers
class ArangoConfigurationTests extends ArangoRunner {

    @Test
    void createConnectionWithCreateDatabaseIfNotExistOnStartup() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", 8528);
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.load-balancing-strategy", "ONE_RANDOM");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);
            assertEquals(8528, configuration.getPort());
            assertEquals("custom", configuration.getDatabase());
            assertEquals("ONE_RANDOM", configuration.getLoadBalancingStrategy().name());

            final ArangoHealthConfiguration healthConfiguration = context.getBean(ArangoHealthConfiguration.class);
            assertNotNull(healthConfiguration);
            assertNotNull(healthConfiguration.toString());
            assertEquals(5000, healthConfiguration.getTimeoutInMillis());
            assertEquals(2, healthConfiguration.getRetry());
            assertTrue(healthConfiguration.isEnabled());
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
            assertEquals(1, configuration.getHosts().size());
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
            assertEquals(1, configuration.getHosts().size());
            assertTrue(configuration.getHosts().contains("localhost:8528"));
        }
    }

    @Test
    void healthConfigurationBuild() {
        final ArangoHealthConfiguration healthConfiguration = new ArangoHealthConfiguration();
        healthConfiguration.setRetry(2);
        healthConfiguration.setTimeoutInMillis(1000);
        assertEquals(2, healthConfiguration.getRetry());
        assertEquals(1000, healthConfiguration.getTimeoutInMillis());
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
            healthConfiguration.setTimeoutInMillis(-1);
            fail("Should not happen!");
        } catch (Exception e) {
            assertTrue(e instanceof ConfigurationException);
        }
    }
}
