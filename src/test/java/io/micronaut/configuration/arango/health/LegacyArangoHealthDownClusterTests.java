package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.annotation.Property;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Flowable;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

/**
 * Tests when health is UP
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Property(name = "arangodb.health-cluster.enabled", value = "true")
@MicronautTest
@Testcontainers
class LegacyArangoHealthDownClusterTests extends ArangoRunner {

    @Container
    private static final ArangoContainer ARANGO_CONTAINER = getContainer();

    @Inject
    private LegacyArangoClusterHealthIndicator clusterHealthIndicator;

    @Test
    void healthClusterDownWhenDatabaseIsSingle() {
        final HealthResult result = Flowable.fromPublisher(clusterHealthIndicator.getResult()).firstElement().blockingGet();
        assertNotNull(result);

        assertEquals(HealthStatus.DOWN, result.getStatus());
        assertEquals("arangodb (cluster)", result.getName());
        assertNotNull(result.getDetails());
    }
}
