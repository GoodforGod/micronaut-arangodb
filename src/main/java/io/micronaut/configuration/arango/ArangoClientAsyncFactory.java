package io.micronaut.configuration.arango;

import io.micronaut.context.annotation.*;
import io.micronaut.runtime.context.scope.Refreshable;

import javax.inject.Named;
import javax.inject.Singleton;

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
     * @param configuration configuration pulled in
     * @return {@link ArangoClientAsync}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Singleton
    @Primary
    public ArangoClientAsync getClient(ArangoAsyncConfiguration configuration) {
        return getClientPrototype(configuration);
    }

    /**
     * Factory method to return a client.
     *
     * @param configuration configuration pulled in
     * @return {@link ArangoClientAsync}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Named("prototype")
    @Prototype
    protected ArangoClientAsync getClientPrototype(ArangoAsyncConfiguration configuration) {
        return new ArangoClientAsync(configuration);
    }
}
