package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
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
@Property(name = "endpoints.health.arangodb.cluster.enabled", value = "true")
@MicronautTest
@Testcontainers
class ArangoHealthDownClusterWhenSingleTests extends ArangoRunner implements TestPropertyProvider {

    @Container
    private static final ArangoContainer<?> CONTAINER_3_11 = new ArangoContainer<>(IMAGE_3_11).withoutAuth();

    @Inject
    private ArangoHealthIndicator healthIndicator;

    @Inject
    private ArangoClusterHealthIndicator clusterHealthIndicator;

    @Override
    public @NonNull Map<String, String> getProperties() {
        return Map.of("arangodb.hosts", CONTAINER_3_11.getHost() + ":" + CONTAINER_3_11.getPort());
    }

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
        assertEquals("arangodb-cluster", result.getName());
        assertNotNull(result.getDetails());
    }
}
