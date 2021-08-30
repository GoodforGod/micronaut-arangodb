package io.micronaut.configuration.arango.health;

import com.arangodb.ArangoDB;
import com.arangodb.velocystream.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.arango.ArangoConfiguration;
import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.http.HttpStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.micronaut.core.util.StringUtils.isEmpty;
import static io.micronaut.health.HealthStatus.*;

/**
 * A {@link HealthIndicator} for ArangoDB cluster. Indicates health of the
 * ArangoDB cluster itself and if it is critical for database availability.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(property = ArangoSettings.PREFIX + ".health.cluster.enabled", value = "true", defaultValue = "false")
@Requires(beans = ArangoDB.class, classes = HealthIndicator.class)
@Singleton
public class ArangoClusterHealthIndicator implements HealthIndicator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The name to expose details with.
     */
    private static final String NAME = "arangodb (cluster)";
    private final ArangoDB accessor;
    private final ObjectMapper mapper;
    private final String database;
    private final ArangoClusterHealthConfiguration healthConfiguration;

    @Inject
    public ArangoClusterHealthIndicator(ArangoDB accessor,
                                        ArangoConfiguration configuration,
                                        ObjectMapper mapper,
                                        ArangoClusterHealthConfiguration healthConfiguration) {
        this.accessor = accessor;
        this.mapper = mapper;
        this.database = configuration.getDatabase();
        this.healthConfiguration = healthConfiguration;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        return Mono.fromCallable(() -> accessor.db(database).route("/_admin/cluster/health").get())
                .timeout(healthConfiguration.getTimeout())
                .retry(healthConfiguration.getRetry())
                .map(this::buildHealthResponse)
                .onErrorResume(e -> Mono.just(buildDownReport(e)));
    }

    private HealthResult buildHealthResponse(Response response) {
        return convertToClusterHealth(response).map(health -> {
            final Map<String, Object> details = buildDetails(health);
            final List<String> down = streamCriticalNodes(health)
                    .filter(n -> DOWN.equals(n.getHealthStatus()))
                    .map(h -> isEmpty(h.getShortName()) ? h.getRoleWithNodeId() : h.getShortName())
                    .collect(Collectors.toList());

            if (!down.isEmpty()) {
                logger.debug("Health '{}' reported DOWN for cause nodes named '{}' were DOWN", NAME, down);
                return buildReport(DOWN, details);
            } else if (streamCriticalNodes(health).allMatch(n -> UP.equals(n.getHealthStatus()))) {
                return buildReport(UP, details);
            } else {
                return buildReport(UNKNOWN, details);
            }
        }).orElseGet(() -> buildReport(UNKNOWN, response.getBody().toString()));
    }

    private Map<String, Object> buildDetails(ClusterHealthResponse clusterHealthResponse) {
        final List<Map<String, ?>> formattedNodesHealth = streamCriticalNodes(clusterHealthResponse)
                .map(n -> {
                    final String name = isEmpty(n.getShortName()) ? n.getRoleWithNodeId() : n.getShortName();
                    return n.isLeading()
                            ? Map.of("leading", true, "name", name, "status", n.getStatus())
                            : Map.of("name", name, "status", n.getStatus());
                })
                .collect(Collectors.toList());

        return Map.of("clusterId", clusterHealthResponse.getClusterId(),
                "nodes", formattedNodesHealth);
    }

    private Stream<ClusterHealthNode> streamCriticalNodes(ClusterHealthResponse clusterHealthResponse) {
        return clusterHealthResponse.streamNodes().filter(node -> !node.isCanBeDeleted());
    }

    private HealthResult buildReport(HealthStatus status, Object details) {
        logger.debug("Health '{}' reported {} with details: {}", NAME, status, details);
        return getBuilder()
                .status(status)
                .details(details)
                .build();
    }

    private HealthResult buildDownReport(Throwable e) {
        logger.debug("Health '{}' reported DOWN with error: {}", NAME, e.getMessage());
        return getBuilder()
                .status(DOWN)
                .exception(e)
                .build();
    }

    private static HealthResult.Builder getBuilder() {
        return HealthResult.builder(NAME);
    }

    private Optional<ClusterHealthResponse> convertToClusterHealth(Response response) {
        if (HttpStatus.OK.getCode() != response.getResponseCode())
            return Optional.empty();

        try {
            return Optional.ofNullable(mapper.readValue(response.getBody().toString(), ClusterHealthResponse.class));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }
}
