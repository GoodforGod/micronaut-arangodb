package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.*;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.runtime.context.scope.Refreshable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Default factory for creating ArangoDB Sync Connection {@link ArangoDB}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
@Requires(beans = ArangoConfiguration.class)
@Factory
public class ArangoAccessorFactory {

    /**
     * Factory method to return a ArangoDB sync connection.
     *
     * @param configuration configuration pulled in for sync accessor.
     * @return {@link ArangoDB}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "shutdown")
    @Primary
    @Prototype
    public ArangoDB getAccessor(ArangoConfiguration configuration) {
        final ArangoDB.Builder builder = new ArangoDB.Builder();
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
