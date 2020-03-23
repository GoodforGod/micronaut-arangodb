package io.micronaut.configuration.arango.health;

import com.arangodb.entity.ArangoDBVersion;
import io.micronaut.configuration.arango.ArangoClient;
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
 * A {@link HealthIndicator} for ArangoDB.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(property = "arangodb.health.enabled", value = "true", defaultValue = "true")
@Requires(beans = ArangoClient.class, classes = HealthIndicator.class)
@Singleton
public class ArangoHealthIndicator implements HealthIndicator {

    /**
     * The name to expose details with.
     */
    private static final String NAME = "arangodb";
    private final ArangoClient client;

    @Inject
    public ArangoHealthIndicator(ArangoClient client) {
        this.client = client;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        return Flowable.fromFuture(client.db().getVersion())
                .timeout(10, TimeUnit.SECONDS)
                .retry(3)
                .map(this::buildUpReport)
                .onErrorReturn(this::buildDownReport);
    }

    private Map<String, Object> buildDetails(ArangoDBVersion version) {
        final Map<String, Object> details = new HashMap<>(2);
        details.put("database", client.getDatabase());
        details.put("version", version.getVersion());
        return details;
    }

    private HealthResult buildUpReport(ArangoDBVersion version) {
        return getBuilder()
                .status(UP)
                .details(buildDetails(version))
                .build();
    }

    private HealthResult buildDownReport(Throwable t) {
        return getBuilder()
                .status(DOWN)
                .exception(t)
                .build();
    }

    private static HealthResult.Builder getBuilder() {
        return HealthResult.builder(NAME);
    }
}
