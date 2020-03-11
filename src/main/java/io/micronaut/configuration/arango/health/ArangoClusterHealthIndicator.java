package io.micronaut.configuration.arango.health;

import com.arangodb.velocystream.Response;
import io.micronaut.configuration.arango.ArangoClient;
import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.Requires;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.micronaut.health.HealthStatus.DOWN;
import static io.micronaut.health.HealthStatus.UP;

/**
 * A {@link HealthIndicator} for ArangoDB cluster. Indicates health of the
 * ArangoDB cluster itself and if it is critical for database availability.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(beans = ArangoClient.class, classes = HealthIndicator.class)
@Singleton
public class ArangoClusterHealthIndicator implements HealthIndicator {

    /**
     * The name to expose details with.
     */
    private static final String NAME = "arangodb (cluster)";
    private final ArangoClient client;

    @Inject
    public ArangoClusterHealthIndicator(ArangoClient client) {
        this.client = client;
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
        return getBuilder()
                .status(UP)
                .details(buildDetails(response))
                .build();
    }

    private Map<String, Object> buildDetails(Response response) {
        final Map<String, Object> details = new HashMap<>();
        response.getBody().getAsString();
        return details;
    }

    private HealthResult buildErrorReport(Throwable t) {
        return getBuilder()
                .status(DOWN)
                .exception(t)
                .build();
    }

    private static HealthResult.Builder getBuilder() {
        return HealthResult.builder(NAME);
    }
}
