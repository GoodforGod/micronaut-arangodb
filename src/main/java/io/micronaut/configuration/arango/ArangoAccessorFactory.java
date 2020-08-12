package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import io.micronaut.context.annotation.*;
import io.micronaut.runtime.context.scope.Refreshable;

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
     * Factory method to return a AranoDB sync connection.
     *
     * @param configuration configuration pulled in for sync accessor.
     * @return {@link ArangoDB}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "shutdown")
    @Primary
    @Prototype
    public ArangoDB getAccessor(ArangoConfiguration configuration) {
        return configuration.getAccessor();
    }
}
