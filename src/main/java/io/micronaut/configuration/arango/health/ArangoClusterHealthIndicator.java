package io.micronaut.configuration.arango.health;

import com.arangodb.velocystream.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.arango.ArangoClient;
import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.http.HttpStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.micronaut.core.util.StringUtils.isEmpty;
import static io.micronaut.health.HealthStatus.*;

/**
 * A {@link HealthIndicator} for ArangoDB cluster. Indicates health of the
 * ArangoDB cluster itself and if it is critical for database availability.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(property = "arangodb.health.cluster.enabled", value = "true", defaultValue = "false")
@Requires(beans = ArangoClient.class, classes = HealthIndicator.class)
@Singleton
public class ArangoClusterHealthIndicator implements HealthIndicator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The name to expose details with.
     */
    private static final String NAME = "arangodb (cluster)";
    private final ArangoClient client;
    private final ObjectMapper mapper;

    @Inject
    public ArangoClusterHealthIndicator(ArangoClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        return Flowable.fromFuture(client.accessor().db(ArangoSettings.DEFAULT_DATABASE).route("/_admin/cluster/health").get())
                .timeout(10, TimeUnit.SECONDS)
                .retry(3)
                .map(this::buildReport)
                .onErrorReturn(this::buildErrorReport);
    }

    private HealthResult buildReport(Response response) {
        if (HttpStatus.OK.getCode() == response.getResponseCode()) {
            return buildHealthResponse(response);
        } else {
            return buildUnknownReport(response);
        }
    }

    private HealthResult buildHealthResponse(Response response) {
        return convertToClusterHealth(response).map(health -> {
            final Map<String, Object> details = buildDetails(health);
            if (health.getNodes().values().stream().anyMatch(n -> "FAILED".equals(n.getStatus()))) {
                return getBuilder().status(DOWN).details(details).build();
            } else if (health.getNodes().values().stream().allMatch(n -> "GOOD".equals(n.getStatus()))) {
                return getBuilder().status(UP).details(details).build();
            } else {
                return getBuilder().status(UNKNOWN).details(details).build();
            }
        }).orElseGet(() -> buildUnknownReport(response));
    }

    private Map<String, Object> buildDetails(HealthCluster healthCluster) {
        final List<Map<String, Object>> healths = healthCluster.getNodes().values().stream()
                .filter(node -> !node.isCanBeDeleted())
                .filter(node -> !"Agent".equals(node.getRole()))
                .map(healthNode -> {
                    final Map<String, Object> nodeHealth = new HashMap<>(2);
                    nodeHealth.put("name", healthNode.getShortName());
                    nodeHealth.put("status", convertNodeStatusToHealthStatus(healthNode.getStatus()));
                    return nodeHealth;
                }).collect(Collectors.toList());

        final Map<String, Object> details = new HashMap<>(2);
        details.put("clusterId", healthCluster.getClusterId());
        details.put("nodes", healths);
        return details;
    }

    private HealthStatus convertNodeStatusToHealthStatus(String nodeStatus) {
        if (isEmpty(nodeStatus))
            return UNKNOWN;

        switch (nodeStatus) {
            case "GOOD":
                return UP;
            case "FAILED":
                return DOWN;
            default:
                return UNKNOWN;
        }
    }

    private HealthResult buildUnknownReport(Response response) {
        return getBuilder()
                .status(UNKNOWN)
                .details(response.getBody().getAsString())
                .build();
    }

    private HealthResult buildErrorReport(Throwable t) {
        return getBuilder()
                .status(DOWN)
                .exception(t.getCause())
                .build();
    }

    private static HealthResult.Builder getBuilder() {
        return HealthResult.builder(NAME);
    }

    private Optional<HealthCluster> convertToClusterHealth(Response response) {
        try {
            return Optional.ofNullable(mapper.readValue(response.getBody().getAsString(), HealthCluster.class));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }
}
