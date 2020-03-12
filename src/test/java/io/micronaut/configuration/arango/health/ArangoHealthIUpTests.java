package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.Single;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.util.Map;

/**
 * Tests when health is UP
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@MicronautTest
@Testcontainers
public class ArangoHealthIUpTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withoutAuthentication();

    @Inject
    private ArangoHealthIndicator healthIndicator;

    @Inject
    private ArangoClusterHealthIndicator clusterHealthIndicator;

    @Test
    void healthUpWhenDatabaseUp() {
        final HealthResult result = Single.fromPublisher(healthIndicator.getResult()).blockingGet();
        assertNotNull(result);

        assertEquals(HealthStatus.UP, result.getStatus());
        assertEquals("arangodb", result.getName());
        assertTrue(result.getDetails() instanceof Map);
        assertFalse(((Map) result.getDetails()).isEmpty());
    }

    @Test
    void healthClusterDownWhenDatabaseIsSingle() {
        final HealthResult result = Single.fromPublisher(clusterHealthIndicator.getResult()).blockingGet();
        assertNotNull(result);

        assertEquals(HealthStatus.UNKNOWN, result.getStatus());
        assertEquals("arangodb (cluster)", result.getName());
        assertTrue(result.getDetails() instanceof Map);
        assertFalse(((Map) result.getDetails()).isEmpty());
    }
}
