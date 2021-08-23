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
 * A {@link HealthIndicator} for ArangoDB cluster. Indicates health of the
 * ArangoDB cluster itself and if it is critical for database availability.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(property = ArangoSettings.PREFIX + ".health.cluster.enabled", value = "true", defaultValue = "false")
@Requires(beans = ArangoDB.class, classes = HealthIndicator.class)
@Singleton
public class ArangoClusterHealthIndicator extends AbstractArangoClusterHealthIndicator {

    @Inject
    public ArangoClusterHealthIndicator(ArangoDB accessor,
                                        ArangoConfiguration configuration,
                                        ObjectMapper mapper,
                                        ArangoClusterHealthConfiguration healthConfiguration) {
        super(accessor, configuration, mapper, healthConfiguration);
    }
}
