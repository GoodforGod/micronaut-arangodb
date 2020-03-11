package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.annotation.Property;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.Single;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

/**
 * Tests when health is DOWN
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Property(name = "arangodb.port", value = "8529")
@MicronautTest
@Testcontainers
public class ArangoHealthDownTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withoutAuthentication().setPort(8528);

    @Inject
    private ArangoHealthIndicator healthIndicator;

    @Test
    void healthDownWhenNoConnectionDueToWrongPort() {
        final HealthResult result = Single.fromPublisher(healthIndicator.getResult()).blockingGet();
        assertNotNull(result);

        assertEquals(HealthStatus.DOWN, result.getStatus());
        assertEquals("arangodb", result.getName());
        assertNotNull(result.getDetails());
    }
}
