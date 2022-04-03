package io.micronaut.configuration.arango.health;

import static io.micronaut.core.util.StringUtils.isEmpty;
import static io.micronaut.health.HealthStatus.*;

import com.arangodb.ArangoDB;
import com.arangodb.DbName;
import com.arangodb.velocystream.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.arango.ArangoConfiguration;
import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.health.HealthStatus;
import io.micronaut.http.HttpStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

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

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_NODES = "nodes";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The name to expose details with.
     */
    private static final String NAME = "arangodb-cluster";
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
        return Mono.fromCallable(() -> accessor.db(DbName.of(database)).route("/_admin/cluster/health").get())
                .timeout(healthConfiguration.getTimeout())
                .retry(healthConfiguration.getRetry())
                .map(this::buildHealthResponse)
                .onErrorResume(e -> Mono.just(buildReport(DOWN, e)));
    }

    private HealthResult buildHealthResponse(Response response) {
        return convertToClusterHealth(response).map(health -> {
            final Map<String, Object> details = buildDetails(health);
            final List<String> downNodes = streamCriticalNodes(health)
                    .filter(n -> DOWN.equals(n.getHealthStatus()))
                    .map(h -> isEmpty(h.getShortName())
                            ? h.getRoleWithNodeId()
                            : h.getShortName())
                    .collect(Collectors.toList());

            if (!downNodes.isEmpty()) {
                logger.debug("Health '{}' reported DOWN cause nodes were DOWN: {}", NAME, downNodes);
                return buildReport(DOWN, details);
            } else if (streamCriticalNodes(health).allMatch(n -> UP.equals(n.getHealthStatus()))) {
                return buildReport(UP, details);
            } else {
                return buildReport(UNKNOWN, details);
            }
        }).orElseGet(() -> buildReport(UNKNOWN, response.getBody().toString()));
    }

    private Map<String, Object> buildDetails(ClusterHealthResponse clusterHealthResponse) {
        final List<String> upNodes = getNodeNames(clusterHealthResponse, UP);
        final List<String> downNodes = getNodeNames(clusterHealthResponse, DOWN);
        final List<String> unknownNodes = getNodeNames(clusterHealthResponse, UNKNOWN);

        final List<Map<String, Object>> clusterDetails = new ArrayList<>(3);
        if (CollectionUtils.isNotEmpty(upNodes))
            clusterDetails.add(Map.of(FIELD_STATUS, UP, FIELD_NODES, upNodes));
        if (CollectionUtils.isNotEmpty(downNodes))
            clusterDetails.add(Map.of(FIELD_STATUS, DOWN, FIELD_NODES, downNodes));
        if (CollectionUtils.isNotEmpty(unknownNodes))
            clusterDetails.add(Map.of(FIELD_STATUS, UNKNOWN, FIELD_NODES, unknownNodes));

        final String version = clusterHealthResponse.streamNodes()
                .map(ClusterHealthNode::getVersion)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse("unknown");

        return Map.of("clusterId", clusterHealthResponse.getClusterId(),
                "version", version,
                "database", database,
                "cluster", clusterDetails);
    }

    private List<String> getNodeNames(ClusterHealthResponse clusterHealthResponse, HealthStatus status) {
        return clusterHealthResponse.streamNodes()
                .filter(n -> status.equals(n.getHealthStatus()))
                .map(n -> isEmpty(n.getShortName())
                        ? n.getRole()
                        : n.getShortName())
                .collect(Collectors.toList());
    }

    private Stream<ClusterHealthNode> streamCriticalNodes(ClusterHealthResponse clusterHealthResponse) {
        return clusterHealthResponse.streamNodes().filter(node -> !node.isCanBeDeleted());
    }

    private HealthResult buildReport(HealthStatus status, Object details) {
        if (DOWN.equals(status)) {
            logger.warn("Health '{}' reported {} with details: {}", NAME, status, details);
        } else {
            logger.debug("Health '{}' reported {} with details: {}", NAME, status, details);
        }

        return getBuilder()
                .status(status)
                .details(details)
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
