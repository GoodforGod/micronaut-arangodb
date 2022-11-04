package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import io.micronaut.configuration.arango.ssl.ArangoSSLConfiguration;
import io.micronaut.configuration.arango.ssl.SSLContextProvider;
import io.micronaut.context.annotation.*;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.runtime.context.scope.Refreshable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import javax.net.ssl.SSLContext;

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
     * @param sslContextProvider provides ssl context for accessor
     * @param configuration      configuration pulled in for sync accessor.
     * @return {@link ArangoDB}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "shutdown")
    @Primary
    @Prototype
    ArangoDB getAccessor(ArangoConfiguration configuration, SSLContextProvider sslContextProvider, List<ArangoDBBuilderConfigurator> configurators) {
        final ArangoSSLConfiguration sslConfiguration = configuration.getSslConfiguration();

        final ArangoDB.Builder builder = new ArangoDB.Builder();
        try (final InputStream properties = configuration.getPropertiesAsInputStream()) {
            builder.loadProperties(properties);
            if (sslConfiguration.isEnabled()) {
                final SSLContext sslContext = sslContextProvider.get(sslConfiguration);
                builder.useSsl(true).sslContext(sslContext);
            }
            if (Objects.nonNull(configurators) && !configurators.isEmpty()) {
                configurators.forEach(consumer -> consumer.accept(builder));
            }
            return builder.build();
        } catch (IOException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }
}
