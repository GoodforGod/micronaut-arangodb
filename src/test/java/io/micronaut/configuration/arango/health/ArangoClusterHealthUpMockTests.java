package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.annotation.Property;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.Single;
import io.testcontainers.arangodb.containers.ArangoClusterBuilder;
import io.testcontainers.arangodb.containers.ArangoClusterDefault;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests when health is UP for mocked ArangoDB cluster
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Property(name = "arangodb.health.cluster.enabled", value = "true")
@Property(name = "arangodb.health.enabled", value = "false")
@Property(name = "arangodb.host", value = "localhost")
@Property(name = "arangodb.port", value = "8530")
@MicronautTest
@Testcontainers
class ArangoClusterHealthUpMockTests extends ArangoRunner {

    private static List<ArangoContainer> cluster = ArangoClusterBuilder.getCluster();

    private static ArangoClusterDefault clusterDefault = ArangoClusterDefault.build();

    @Container
    private static final ArangoContainer agency1 = clusterDefault.getAgency1();
    @Container
    private static final ArangoContainer agency2 = clusterDefault.getAgency2();
    @Container
    private static final ArangoContainer agency3 = clusterDefault.getAgency3();
    @Container
    private static final ArangoContainer db1 = clusterDefault.getDatabase1();
    @Container
    private static final ArangoContainer db2 = clusterDefault.getDatabase2();
    @Container
    private static final ArangoContainer coordinator1 = clusterDefault.getCoordinator1();
    @Container
    private static final ArangoContainer coordinator2 = clusterDefault.getCoordinator2();

    @Inject
    private ArangoClusterHealthIndicator indicator;

    @Test
    void healthClusterDownWhenDatabaseIsSingle() {
        final HealthResult result = Single.fromPublisher(indicator.getResult()).timeout(10, TimeUnit.SECONDS).blockingGet();
        assertNotNull(result);

        assertEquals(HealthStatus.UP, result.getStatus());
        assertEquals("arangodb (cluster)", result.getName());
        assertNotNull(result.getDetails());
    }
}
