package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.testcontainers.arangodb.cluster.ArangoClusterDefault;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Tests when health is UP for mocked ArangoDB cluster
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Testcontainers
class ArangoHealthUpClusterTests extends ArangoRunner {

    private static final ArangoClusterDefault CLUSTER_DEFAULT = ArangoClusterDefault.build(ArangoContainer.LATEST);

    @Container
    private static final ArangoContainer agent1 = CLUSTER_DEFAULT.getAgent1();
    @Container
    private static final ArangoContainer agent2 = CLUSTER_DEFAULT.getAgent2();
    @Container
    private static final ArangoContainer agent3 = CLUSTER_DEFAULT.getAgent3();
    @Container
    private static final ArangoContainer db1 = CLUSTER_DEFAULT.getDatabase1();
    @Container
    private static final ArangoContainer db2 = CLUSTER_DEFAULT.getDatabase2();
    @Container
    private static final ArangoContainer coordinator1 = CLUSTER_DEFAULT.getCoordinator1();
    @Container
    private static final ArangoContainer coordinator2 = CLUSTER_DEFAULT.getCoordinator2();

    @Test
    void healthSingleUpForSystemDatabase() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.create-database-if-not-exist", true);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final HealthIndicator healthIndicator = context.getBean(ArangoHealthIndicator.class);

            final HealthResult result = Flux.from(healthIndicator.getResult())
                    .blockFirst(Duration.ofSeconds(10));
            assertNotNull(result);

            assertEquals(HealthStatus.UP, result.getStatus());
            assertEquals("arangodb", result.getName());
            assertNotNull(result.getDetails());
            assertTrue(result.getDetails() instanceof Map);
        }
    }

    @Test
    void healthSingleUpForCustomDatabase() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.create-database-timeout", Duration.ofSeconds(50));

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final HealthIndicator healthIndicator = context.getBean(ArangoHealthIndicator.class);

            final HealthResult result = Flux.from(healthIndicator.getResult())
                    .blockFirst(Duration.ofSeconds(10));
            assertNotNull(result);

            assertEquals(HealthStatus.UP, result.getStatus());
            assertEquals("arangodb", result.getName());
            assertNotNull(result.getDetails());
            assertTrue(result.getDetails() instanceof Map);
        }
    }

    @Test
    void healthClusterUpForSystemDatabase() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.health.cluster.enabled", true);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final HealthIndicator clusterHealthIndicator = context.getBean(ArangoClusterHealthIndicator.class);

            final HealthResult result = Flux.from(clusterHealthIndicator.getResult())
                    .blockFirst(Duration.ofSeconds(10));
            assertNotNull(result);

            assertEquals(HealthStatus.UP, result.getStatus());
            assertEquals("arangodb-cluster", result.getName());
            assertNotNull(result.getDetails());
            assertTrue(result.getDetails() instanceof Map);
            assertTrue(((Map) result.getDetails()).get("cluster") instanceof Collection);
            assertEquals(1, ((Collection) ((Map) result.getDetails()).get("cluster")).size());
        }
    }

    @Test
    void healthClusterUpForCustomDatabase() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.create-database-timeout", Duration.ofSeconds(50));
        properties.put("arangodb.health.cluster.enabled", true);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final HealthIndicator clusterHealthIndicator = context.getBean(ArangoClusterHealthIndicator.class);

            final HealthResult result = Flux.from(clusterHealthIndicator.getResult())
                    .blockFirst(Duration.ofSeconds(10));
            assertNotNull(result);

            assertEquals(HealthStatus.UP, result.getStatus());
            assertEquals("arangodb-cluster", result.getName());
            assertNotNull(result.getDetails());
            assertTrue(result.getDetails() instanceof Map);
            assertTrue(((Map) result.getDetails()).get("cluster") instanceof Collection);
            assertEquals(1, ((Collection) ((Map) result.getDetails()).get("cluster")).size());

            final HealthEndpoint healthEndpoint = context.getBean(HealthEndpoint.class);
            HealthResult block = Mono.from(healthEndpoint.getHealth(null)).block(Duration.ofSeconds(10));
            assertNotNull(block);
        }
    }
}
