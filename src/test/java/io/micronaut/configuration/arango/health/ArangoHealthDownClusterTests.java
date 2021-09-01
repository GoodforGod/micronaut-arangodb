package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.annotation.Property;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.testcontainers.arangodb.containers.ArangoContainer;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;

/**
 * Tests when health is UP
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Property(name = "arangodb.health.cluster.enabled", value = "true")
@MicronautTest
@Testcontainers
class ArangoHealthDownClusterTests extends ArangoRunner {

    @Container
    private static final ArangoContainer ARANGO_CONTAINER = getContainer();

    @Inject
    private ArangoHealthIndicator healthIndicator;

    @Inject
    private ArangoClusterHealthIndicator clusterHealthIndicator;

    @Test
    void healthUpWhenDatabaseUp() {
        final HealthResult result = Flux.from(healthIndicator.getResult()).blockFirst(Duration.ofSeconds(10));
        assertNotNull(result);

        assertEquals(HealthStatus.UP, result.getStatus());
        assertEquals("arangodb", result.getName());
        assertTrue(result.getDetails() instanceof Map);
        assertFalse(((Map) result.getDetails()).isEmpty());
    }

    @Test
    void healthClusterDownWhenDatabaseIsSingle() {
        final HealthResult result = Flux.from(clusterHealthIndicator.getResult()).blockFirst(Duration.ofSeconds(10));
        assertNotNull(result);

        assertEquals(HealthStatus.DOWN, result.getStatus());
        assertEquals("arangodb (cluster)", result.getName());
        assertNotNull(result.getDetails());
    }
}
