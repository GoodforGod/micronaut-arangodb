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
@ConfigurationProperties(ArangoSettings.PREFIX)
public class ArangoAsyncConfiguration extends AbstractArangoConfiguration {

    @ConfigurationBuilder(prefixes = "", excludes = { "host", "user" })
    protected ArangoDBAsync.Builder config = new ArangoDBAsync.Builder().timeout(10000);

    /**
     * @return client configuration builder
     */
    protected ArangoDBAsync.Builder getConfig() {
        return config.host(getHost(), getPort()).user(getUser());
    }

    /**
     * @return client configuration
     */
    public ArangoDBAsync getAccessor() {
        return getConfig().build();
    }
}
