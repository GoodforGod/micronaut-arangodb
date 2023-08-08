package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.testcontainers.arangodb.cluster.ArangoCluster;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

    @Container
    private static final ArangoCluster CLUSTER_3_11 = ArangoCluster.builder(IMAGE_3_11).build();
    @Container
    private static final ArangoCluster CLUSTER_3_7 = ArangoCluster.builder(IMAGE_3_7).build();

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(CLUSTER_3_11),
                Arguments.of(CLUSTER_3_7));
    }

    @MethodSource("data")
    @ParameterizedTest
    void healthSingleUpForSystemDatabase(ArangoCluster cluster) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of(cluster.getHost() + ":" + cluster.getPort()));
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("endpoints.health.arangodb.cluster.enabled", true);

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

    @MethodSource("data")
    @ParameterizedTest
    void healthSingleUpForCustomDatabase(ArangoCluster cluster) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of(cluster.getHost() + ":" + cluster.getPort()));
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.create-database-timeout", Duration.ofSeconds(50));
        properties.put("endpoints.health.arangodb.cluster.enabled", true);

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

    @MethodSource("data")
    @ParameterizedTest
    void healthClusterUpForSystemDatabase(ArangoCluster cluster) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of(cluster.getHost() + ":" + cluster.getPort()));
        properties.put("endpoints.health.arangodb.cluster.enabled", true);

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

    @MethodSource("data")
    @ParameterizedTest
    void healthClusterUpForCustomDatabase(ArangoCluster cluster) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of(cluster.getHost() + ":" + cluster.getPort()));
        properties.put("arangodb.database", "custom");
        properties.put("arangodb.create-database-if-not-exist", true);
        properties.put("arangodb.create-database-timeout", Duration.ofSeconds(50));
        properties.put("endpoints.health.arangodb.cluster.enabled", true);

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
