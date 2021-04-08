package io.micronaut.configuration.arango.health;

import com.arangodb.ArangoDB;
import com.arangodb.velocystream.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.arango.ArangoConfiguration;
import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
@Requires(property = ArangoSettings.PREFIX + ".health-cluster.enabled", value = "true", defaultValue = "false")
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

    @Inject
    public ArangoClusterHealthIndicator(ArangoDB accessor, ArangoConfiguration configuration, ObjectMapper mapper) {
        this.accessor = accessor;
        this.mapper = mapper;
        this.database = configuration.getDatabase();
    }

    @Override
    public Flowable<HealthResult> getResult() {
        return Flowable.fromCallable(() -> accessor.db(database).route("/_admin/cluster/health").get())
                .timeout(20000, TimeUnit.MILLISECONDS)
                .retry(2)
                .map(this::buildReport)
                .onErrorReturn(this::buildErrorReport);
    }

    private HealthResult buildReport(Response response) {
        return HttpStatus.OK.getCode() == response.getResponseCode()
                ? buildHealthResponse(response)
                : buildUnknownReport(response);
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
                return getBuilder().status(DOWN).details(details).build();
            } else if (streamCriticalNodes(health).allMatch(n -> UP.equals(n.getHealthStatus()))) {
                logger.debug("Health '{}' reported UP with details: {}", NAME, details);
                return getBuilder().status(UP).details(details).build();
            } else {
                logger.debug("Health '{}' reported UNKNOWN with details: {}", NAME, details);
                return getBuilder().status(UNKNOWN).details(details).build();
            }
        }).orElseGet(() -> buildUnknownReport(response));
    }

    private Map<String, Object> buildDetails(HealthCluster healthCluster) {
        final List<Map<String, ?>> formattedNodesHealth = streamCriticalNodes(healthCluster)
                .map(n -> {
                    final String name = isEmpty(n.getShortName()) ? n.getRoleWithNodeId() : n.getShortName();
                    return n.isLeading()
                            ? Map.of("leading", true, "name", name, "status", n.getStatus())
                            : Map.of("name", name, "status", n.getStatus());
                })
                .collect(Collectors.toList());

        return Map.of("clusterId", healthCluster.getClusterId(),
                "nodes", formattedNodesHealth);
    }

    private Stream<HealthNode> streamCriticalNodes(HealthCluster healthCluster) {
        return healthCluster.streamNodes().filter(node -> !node.isCanBeDeleted());
    }

    private HealthResult buildUnknownReport(Response response) {
        final String details = response.getBody().toString();
        logger.debug("Health '{}' reported UNKNOWN with details: {}", NAME, details);
        return getBuilder()
                .status(UNKNOWN)
                .details(details)
                .build();
    }

    private HealthResult buildErrorReport(Throwable e) {
        logger.debug("Health '{}' reported DOWN with error: {}", NAME, e.getMessage());
        return getBuilder()
                .status(DOWN)
                .exception(e)
                .build();
    }

    private static HealthResult.Builder getBuilder() {
        return HealthResult.builder(NAME);
    }

    private Optional<HealthCluster> convertToClusterHealth(Response response) {
        try {
            return Optional.ofNullable(mapper.readValue(response.getBody().toString(), HealthCluster.class));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }
}
