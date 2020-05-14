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
@Requires(beans = ArangoClientSyncConfiguration.class)
@Factory
public class ArangoAccessorSyncFactory {

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
    public ArangoDB getAccessorSync(ArangoClientSyncConfiguration configuration) {
        return configuration.getAccessor();
    }
}
