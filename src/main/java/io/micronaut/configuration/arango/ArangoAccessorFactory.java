package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.serde.ArangoSerde;
import io.micronaut.configuration.arango.ssl.ArangoSSLConfiguration;
import io.micronaut.configuration.arango.ssl.SSLContextProvider;
import io.micronaut.context.annotation.*;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.runtime.context.scope.Refreshable;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
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

    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "shutdown")
    @Singleton
    ArangoDB getAccessor(ArangoConfiguration configuration,
                         SSLContextProvider sslContextProvider,
                         @Nullable ArangoSerde serde) {
        return createAccessor(configuration, sslContextProvider, serde);
    }

    /**
     * Factory method to return a ArangoDB sync connection.
     *
     * @param sslContextProvider provides ssl context for accessor
     * @param configuration      configuration pulled in for sync accessor.
     * @return {@link ArangoDB}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "shutdown")
    @Named("prototype")
    @Prototype
    @Secondary
    ArangoDB getAccessorPrototype(ArangoConfiguration configuration,
                                  SSLContextProvider sslContextProvider,
                                  @Nullable ArangoSerde serde) {
        return createAccessor(configuration, sslContextProvider, serde);
    }

    private ArangoDB createAccessor(ArangoConfiguration configuration,
                                    SSLContextProvider sslContextProvider,
                                    @Nullable ArangoSerde serde) {
        final ArangoSSLConfiguration sslConfiguration = configuration.getSslConfiguration();

        final ArangoDB.Builder builder = new ArangoDB.Builder();
        final ArangoConfigProperties properties = configuration.getArangoConfigProperties();
        builder.loadProperties(properties);
        if (sslConfiguration.isEnabled()) {
            final SSLContext sslContext = sslContextProvider.get(sslConfiguration);
            builder.useSsl(true).sslContext(sslContext);
        }

        return (serde == null)
                ? builder.build()
                : builder.serde(serde).build();
    }
}
