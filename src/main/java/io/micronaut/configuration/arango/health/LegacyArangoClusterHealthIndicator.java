package io.micronaut.configuration.arango.health;

import com.arangodb.ArangoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.arango.ArangoConfiguration;
import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.Requires;
import io.micronaut.management.health.indicator.HealthIndicator;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.micronaut.health.HealthStatus.*;

/**
 * Deprecated health cluster indicator
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoClusterHealthIndicator
 * @since 29.2.2020
 */
@Deprecated
@Requires(property = ArangoSettings.PREFIX + ".health-cluster.enabled", value = "true", defaultValue = "false")
@Requires(property = ArangoSettings.PREFIX + ".health.cluster.enabled", value = "false", defaultValue = "false")
@Requires(beans = ArangoDB.class, classes = HealthIndicator.class)
@Singleton
public class LegacyArangoClusterHealthIndicator extends AbstractArangoClusterHealthIndicator {

    @Inject
    public LegacyArangoClusterHealthIndicator(ArangoDB accessor,
                                              ArangoConfiguration configuration,
                                              ObjectMapper mapper,
                                              ArangoClusterHealthConfiguration healthConfiguration) {
        super(accessor, configuration, mapper, healthConfiguration);
    }
}
