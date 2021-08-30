package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.configuration.arango.internal.ArangoClientAsyncImpl;
import io.micronaut.context.annotation.*;
import io.micronaut.runtime.context.scope.Refreshable;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * Default factory for creating ArangoDB client {@link ArangoClientAsync}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(beans = ArangoAsyncConfiguration.class)
@Factory
public class ArangoClientAsyncFactory {

    /**
     * Factory method to return a client.
     *
     * @param accessor      that will be used in client
     * @param configuration configuration pulled in
     * @return {@link ArangoClientAsync}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Singleton
    @Primary
    ArangoClientAsync getClient(ArangoDBAsync accessor, ArangoAsyncConfiguration configuration) {
        return getClientPrototype(accessor, configuration);
    }

    /**
     * Factory method to return a client.
     *
     * @param accessor      that will be used in client
     * @param configuration configuration pulled in
     * @return {@link ArangoClientAsync}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Named("prototype")
    @Prototype
    ArangoClientAsync getClientPrototype(ArangoDBAsync accessor, ArangoAsyncConfiguration configuration) {
        return new ArangoClientAsyncImpl(accessor, configuration);
    }
}
