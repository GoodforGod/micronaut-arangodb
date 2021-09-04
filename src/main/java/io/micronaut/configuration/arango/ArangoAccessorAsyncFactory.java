package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.configuration.arango.ssl.ArangoSSLConfiguration;
import io.micronaut.configuration.arango.ssl.SSLContextProvider;
import io.micronaut.context.annotation.*;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.runtime.context.scope.Refreshable;
import java.io.IOException;
import java.io.InputStream;
import javax.net.ssl.SSLContext;

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
     * @param sslContextProvider provides ssl context for accessor
     * @param configuration      configuration pulled in for async accessor.
     * @return {@link ArangoDBAsync}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "shutdown")
    @Primary
    @Prototype
    ArangoDBAsync getAccessor(ArangoAsyncConfiguration configuration, SSLContextProvider sslContextProvider) {
        final ArangoSSLConfiguration sslConfiguration = configuration.getSslConfiguration();

        final ArangoDBAsync.Builder builder = new ArangoDBAsync.Builder();
        try (final InputStream properties = configuration.getPropertiesAsInputStream()) {
            builder.loadProperties(properties);
            if (sslConfiguration.isEnabled()) {
                final SSLContext sslContext = sslContextProvider.get(sslConfiguration);
                builder.useSsl(true).sslContext(sslContext);
            }

            return builder.build();
        } catch (IOException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }
}
