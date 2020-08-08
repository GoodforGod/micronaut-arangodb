package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

/**
 * ArangoDB Async configuration class.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(property = ArangoSettings.PREFIX)
@Requires(classes = ArangoDBAsync.class)
@ConfigurationProperties(ArangoSettings.PREFIX)
public class ArangoAsyncConfiguration extends AbstractArangoConfiguration {

    @ConfigurationBuilder(prefixes = "", excludes = { "host" })
    protected ArangoDBAsync.Builder config = new ArangoDBAsync.Builder();

    /**
     * @return client configuration builder
     */
    ArangoDBAsync.Builder getConfig() {
        return config.host(getHost(), getPort());
    }

    /**
     * @return client configuration
     */
    public ArangoDBAsync getAccessor() {
        return getConfig().build();
    }
}
