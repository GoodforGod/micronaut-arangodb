package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import io.micronaut.context.annotation.*;
import io.micronaut.runtime.context.scope.Refreshable;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * Default factory for creating ArangoDB client {@link ArangoClient}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(beans = { ArangoDB.class, ArangoConfiguration.class })
@Factory
public class ArangoClientFactory {

    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Singleton
    ArangoClient getClient(ArangoDB accessor, ArangoConfiguration configuration) {
        return new ArangoClientImpl(accessor, configuration);
    }

    /**
     * Factory method to return a client.
     *
     * @param accessor      that will be used in client
     * @param configuration configuration pulled in
     * @return {@link ArangoClient}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Named("prototype")
    @Prototype
    @Secondary
    ArangoClient getClientPrototype(ArangoDB accessor, ArangoConfiguration configuration) {
        return new ArangoClientImpl(accessor, configuration);
    }
}
