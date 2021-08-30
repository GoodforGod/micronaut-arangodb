package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.annotation.Property;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.testcontainers.arangodb.containers.ArangoContainer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Tests when health is DOWN
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Property(name = "arangodb.port", value = "8529")
@Property(name = "arangodb.health.cluster.enabled", value = "true")
@MicronautTest
@Testcontainers
class ArangoHealthDownTests extends ArangoRunner {

    @Container
    private static final ArangoContainer ARANGO_CONTAINER = new ArangoContainer(ArangoContainer.LATEST).withoutAuth().withFixedPort(8528);

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
        assertEquals("arangodb (cluster)", result.getName());
        assertNotNull(result.getDetails());
    }
}
