package io.micronaut.configuration.arango.health;

import com.arangodb.ArangoDB;
import com.arangodb.entity.DatabaseEntity;
import io.micronaut.configuration.arango.ArangoClientAsync;
import io.micronaut.configuration.arango.ArangoClient;
import io.micronaut.context.annotation.Requires;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
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
@Requires(beans = ArangoClientAsync.class, classes = HealthIndicator.class)
@Singleton
public class ArangoHealthIndicator implements HealthIndicator {

    /**
     * The name to expose details with.
     */
    private static final String NAME = "arangodb";
    private final ArangoDB accessor;
    private final String database;

    @Inject
    public ArangoHealthIndicator(ArangoDB accessor, ArangoClient client) {
        this.accessor = accessor;
        this.database = client.database();
    }

    @Override
    public Publisher<HealthResult> getResult() {
        return Flowable.fromCallable(() -> accessor.db(database).getInfo())
                .timeout(10, TimeUnit.SECONDS)
                .retry(3)
                .map(this::buildUpReport)
                .onErrorReturn(this::buildDownReport);
    }

    private Map<String, Object> buildDetails(DatabaseEntity db) {
        final Map<String, Object> details = new HashMap<>(2);
        details.put("database", db.getName());
        details.put("id", db.getId());
        return details;
    }

    private HealthResult buildUpReport(DatabaseEntity db) {
        return getBuilder()
                .status(UP)
                .details(buildDetails(db))
                .build();
    }

    private HealthResult buildDownReport(Throwable t) {
        return getBuilder()
                .status(DOWN)
                .details(Collections.singletonMap("database", database))
                .exception(t)
                .build();
    }

    private static HealthResult.Builder getBuilder() {
        return HealthResult.builder(NAME);
    }
}
