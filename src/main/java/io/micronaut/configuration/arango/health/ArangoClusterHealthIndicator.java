package io.micronaut.configuration.arango.health;

import com.arangodb.ArangoDB;
import com.arangodb.velocystream.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
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
@Requires(property = "arangodb.health.cluster.enabled", value = "true", defaultValue = "false")
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

    @Inject
    public ArangoClusterHealthIndicator(ArangoDB accessor, ObjectMapper mapper) {
        this.accessor = accessor;
        this.mapper = mapper;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        return Flowable.fromCallable(() -> accessor.db(ArangoSettings.DEFAULT_DATABASE).route("/_admin/cluster/health").get())
                .timeout(10, TimeUnit.SECONDS)
                .retry(3)
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
                logger.error("Cluster nodes named '{}' reported with status DOWN", down);
                return getBuilder().status(DOWN).details(details).build();
            } else if (streamCriticalNodes(health).allMatch(n -> UP.equals(n.getHealthStatus()))) {
                return getBuilder().status(UP).details(details).build();
            } else {
                return getBuilder().status(UNKNOWN).details(details).build();
            }
        }).orElseGet(() -> buildUnknownReport(response));
    }

    private Map<String, Object> buildDetails(HealthCluster healthCluster) {
        final List<Map<Object, Object>> formattedNodesHealth = streamCriticalNodes(healthCluster)
                .map(h -> {
                    final int reportSize = h.isLeading() ? 3 : 2;
                    final Map<Object, Object> report = new HashMap<>(reportSize);
                    final String name = isEmpty(h.getShortName()) ? h.getRoleWithNodeId() : h.getShortName();
                    report.put("name", name);
                    report.put("status", h.getStatus());
                    if (h.isLeading())
                        report.put("leading", true);

                    return report;
                })
                .collect(Collectors.toList());

        final Map<String, Object> details = new HashMap<>(2);
        details.put("clusterId", healthCluster.getClusterId());
        details.put("nodes", formattedNodesHealth);
        return details;
    }

    private Stream<HealthNode> streamCriticalNodes(HealthCluster healthCluster) {
        return healthCluster.streamNodes().filter(node -> !node.isCanBeDeleted());
    }

    private HealthResult buildUnknownReport(Response response) {
        return getBuilder()
                .status(UNKNOWN)
                .details(response.getBody().toString())
                .build();
    }

    private HealthResult buildErrorReport(Throwable e) {
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
