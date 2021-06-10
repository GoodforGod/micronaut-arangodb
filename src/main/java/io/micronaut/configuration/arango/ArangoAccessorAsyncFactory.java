package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.*;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.runtime.context.scope.Refreshable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Default factory for creating ArangoDB Async Connection {@link ArangoDBAsync}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
@Requires(beans = ArangoAsyncConfiguration.class)
@Factory
public class ArangoAccessorAsyncFactory {

    /**
     * Factory method to return a ArangoDB async connection.
     *
     * @param configuration configuration pulled in for async accessor.
     * @return {@link ArangoDBAsync}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "shutdown")
    @Primary
    @Prototype
    public ArangoDBAsync getAccessor(ArangoAsyncConfiguration configuration) {
        final ArangoDBAsync.Builder builder = new ArangoDBAsync.Builder();
        try (final InputStream properties = configuration.getPropertiesAsInputStream()) {
            builder.loadProperties(properties);
            if (configuration.getSslConfiguration().getUseSsl()) {
                builder.sslContext(configuration.getSslConfiguration().getSSLContext());
            }

            return builder.build();
        } catch (IOException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }
}
