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
@Requires(beans = ArangoClientConfiguration.class)
@Factory
public class ArangoAccessorFactory {

    /**
     * Factory method to return a AranoDB async connection.
     *
     * @param configuration configuration pulled in for async accessor.
     * @return {@link ArangoDBAsync}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "shutdown")
    @Primary
    @Prototype
    public ArangoDBAsync getAccessorAsync(ArangoClientConfiguration configuration) {
        return configuration.getAccessor();
    }
}
