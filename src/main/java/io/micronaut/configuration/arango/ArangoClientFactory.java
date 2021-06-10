package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import io.micronaut.context.annotation.*;
import io.micronaut.runtime.context.scope.Refreshable;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Default factory for creating ArangoDB client {@link ArangoClient}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(beans = ArangoConfiguration.class)
@Factory
public class ArangoClientFactory {

    /**
     * Factory method to return a client.
     *
     * @param accessor that will be used in client
     * @param configuration configuration pulled in
     * @return {@link ArangoClient}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Singleton
    @Primary
    public ArangoClient getClient(ArangoDB accessor, ArangoConfiguration configuration) {
        return getClientPrototype(accessor, configuration);
    }

    /**
     * Factory method to return a client.
     *
     * @param accessor that will be used in client
     * @param configuration configuration pulled in
     * @return {@link ArangoClient}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Named("prototype")
    @Prototype
    protected ArangoClient getClientPrototype(ArangoDB accessor, ArangoConfiguration configuration) {
        return new ArangoClient(accessor, configuration);
    }
}
