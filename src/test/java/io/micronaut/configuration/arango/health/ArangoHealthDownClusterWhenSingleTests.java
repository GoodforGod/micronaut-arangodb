package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthResult;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;

/**
 * Tests when health is UP
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArangoHealthDownClusterWhenSingleTests extends ArangoRunner {

    @Container
    private static final ArangoContainer<?> CONTAINER_3_11 = new ArangoContainer<>(IMAGE_3_11).withoutAuth();

    @Test
    void healthUpWhenDatabaseUp() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("endpoints.health.arangodb.cluster.enabled", "true");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoHealthIndicator healthIndicator = context.getBean(ArangoHealthIndicator.class);
            final HealthResult result = Flux.from(healthIndicator.getResult()).blockFirst(Duration.ofSeconds(10));
            assertNotNull(result);

            assertEquals(HealthStatus.UP, result.getStatus());
            assertEquals("arangodb", result.getName());
            assertTrue(result.getDetails() instanceof Map);
            assertFalse(((Map) result.getDetails()).isEmpty());
        }
    }

    @Test
    void healthClusterDownWhenDatabaseIsSingle() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("endpoints.health.arangodb.cluster.enabled", "true");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoClusterHealthIndicator clusterHealthIndicator = context.getBean(ArangoClusterHealthIndicator.class);
            final HealthResult result = Flux.from(clusterHealthIndicator.getResult()).blockFirst(Duration.ofSeconds(10));
            assertNotNull(result);

            assertEquals(HealthStatus.DOWN, result.getStatus());
            assertEquals("arangodb-cluster", result.getName());
            assertNotNull(result.getDetails());
        }
    }
}
