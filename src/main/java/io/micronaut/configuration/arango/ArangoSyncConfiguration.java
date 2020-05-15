package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

/**
 * ArangoDB Sync configuration class.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(property = ArangoSettings.PREFIX)
@Requires(classes = ArangoDB.class)
@ConfigurationProperties(ArangoSettings.PREFIX)
public class ArangoSyncConfiguration extends AbstractArangoConfiguration {

    @ConfigurationBuilder(prefixes = "", excludes = { "host" })
    protected ArangoDB.Builder config = new ArangoDB.Builder();

    /**
     * @return client configuration builder
     */
    public ArangoDB.Builder getConfig() {
        return config.host(getHost(), getPort());
    }

    /**
     * @return client configuration
     */
    public ArangoDB getAccessor() {
        return getConfig().host(getHost(), getPort()).build();
    }
}