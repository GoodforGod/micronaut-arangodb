package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.annotation.Property;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;

/**
 * Tests when health is DOWN
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Property(name = "arangodb.hosts", value = "localhost:8529")
@Property(name = "endpoints.health.arangodb.cluster.enabled", value = "true")
@MicronautTest
@Testcontainers
class ArangoHealthDownTests extends ArangoRunner {

    @Inject
    private ArangoHealthIndicator healthIndicator;

    @Inject
    private ArangoClusterHealthIndicator clusterHealthIndicator;

    @Test
    void healthDownWhenNoConnectionDueToWrongPort() {
        final HealthResult result = Flux.from(healthIndicator.getResult()).blockFirst(Duration.ofSeconds(10));
        assertNotNull(result);

        assertEquals(HealthStatus.DOWN, result.getStatus());
        assertEquals("arangodb", result.getName());
        assertNotNull(result.getDetails());
    }

    @Test
    void healthClusterDownWhenNoConnectionDueToWrongPort() {
        final HealthResult result = Flux.from(clusterHealthIndicator.getResult()).blockFirst(Duration.ofSeconds(10));
        assertNotNull(result);

        assertEquals(HealthStatus.DOWN, result.getStatus());
        assertEquals("arangodb-cluster", result.getName());
        assertNotNull(result.getDetails());
    }
}
