package io.micronaut.configuration.arango.health;

import com.arangodb.ArangoDB;
import com.arangodb.entity.DatabaseEntity;
import io.micronaut.configuration.arango.ArangoConfiguration;
import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.Requires;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
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
@Requires(property = ArangoSettings.PREFIX + ".health.enabled", value = "true", defaultValue = "true")
@Requires(beans = ArangoDB.class, classes = HealthIndicator.class)
@Singleton
public class ArangoHealthIndicator implements HealthIndicator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The name to expose details with.
     */
    private static final String NAME = "arangodb";
    private final ArangoDB accessor;
    private final String database;
    private final ArangoHealthConfiguration healthConfiguration;

    @Inject
    public ArangoHealthIndicator(ArangoDB accessor,
                                 ArangoConfiguration configuration,
                                 ArangoHealthConfiguration healthConfiguration) {
        this.accessor = accessor;
        this.database = configuration.getDatabase();
        this.healthConfiguration = healthConfiguration;
    }

    @Override
    public Flowable<HealthResult> getResult() {
        return Flowable.fromCallable(() -> accessor.db(database).getInfo())
                .timeout(healthConfiguration.getTimeoutInMillis(), TimeUnit.MILLISECONDS)
                .retry(healthConfiguration.getRetry())
                .map(this::buildUpReport)
                .onErrorReturn(this::buildDownReport);
    }

    private Map<String, Object> buildDetails(DatabaseEntity db) {
        return Map.of("database", db.getName(), "id", db.getId());
    }

    private HealthResult buildUpReport(DatabaseEntity db) {
        final Map<String, Object> details = buildDetails(db);
        logger.debug("Heath '{}' reported UP with details: {}", NAME, details);
        return getBuilder()
                .status(UP)
                .details(details)
                .build();
    }

    private HealthResult buildDownReport(Throwable e) {
        logger.debug("Heath '{}' reported DOWN with error: {}", NAME, e.getMessage());
        return getBuilder()
                .status(DOWN)
                .exception(e)
                .build();
    }

    private static HealthResult.Builder getBuilder() {
        return HealthResult.builder(NAME);
    }
}
