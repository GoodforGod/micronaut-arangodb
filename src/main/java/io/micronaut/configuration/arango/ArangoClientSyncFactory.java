package io.micronaut.configuration.arango;

import io.micronaut.context.annotation.*;
import io.micronaut.runtime.context.scope.Refreshable;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Default factory for creating ArangoDB client {@link ArangoClientSync}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(beans = ArangoSyncConfiguration.class)
@Factory
public class ArangoClientSyncFactory {

    /**
     * Factory method to return a client.
     *
     * @param configuration configuration pulled in
     * @return {@link ArangoClientSync}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Singleton
    @Primary
    public ArangoClientSync getClient(ArangoSyncConfiguration configuration) {
        return getClientPrototype(configuration);
    }

    /**
     * Factory method to return a client.
     *
     * @param configuration configuration pulled in
     * @return {@link ArangoClientSync}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Named("prototype")
    @Prototype
    protected ArangoClientSync getClientPrototype(ArangoSyncConfiguration configuration) {
        return new ArangoClientSync(configuration);
    }
}
