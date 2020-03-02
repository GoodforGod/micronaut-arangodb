package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoClient;
import io.micronaut.context.annotation.Requires;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.inject.Singleton;

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
        return null;
    }

    private static HealthResult.Builder getBuilder() {
        return HealthResult.builder(NAME);
    }
}
