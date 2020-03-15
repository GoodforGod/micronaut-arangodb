package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.*;
import io.micronaut.runtime.context.scope.Refreshable;

/**
 * Default factory for creating ArangoDB Async Connection {@link ArangoDBAsync}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
@Requires(beans = ArangoSyncClientConfiguration.class)
@Factory
public class ArangoSyncAccessorFactory {

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
    public ArangoDB getAccessor(ArangoSyncClientConfiguration configuration) {
        return configuration.getAccessor();
    }
}
