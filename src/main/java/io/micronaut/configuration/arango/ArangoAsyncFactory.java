package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.*;
import io.micronaut.runtime.context.scope.Refreshable;

/**
 * Default factory for creating ArangoDB Async Connection {@link ArangoDBAsync}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
@Requires(beans = { ArangoConfiguration.class })
@Factory
public class ArangoAsyncFactory {

    /**
     * Factory method to return a arango db async connection.
     *
     * @param configuration configuration pulled in
     * @return {@link ArangoDBAsync}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "shutdown")
    @Primary
    @Prototype
    public ArangoDBAsync getAccessor(ArangoConfiguration configuration) {
        return configuration.getConfigBuilder()
                .host(configuration.getHost(), configuration.getPort())
                .build();
    }
}
