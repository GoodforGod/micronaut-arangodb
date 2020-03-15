package io.micronaut.configuration.arango;

import com.arangodb.ArangoDBException;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.context.scope.Refreshable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * Default factory for creating ArangoDB client {@link ArangoClient}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(beans = ArangoClientConfiguration.class)
@Factory
public class ArangoSyncClientFactory {

    /**
     * Factory method to return a client.
     *
     * @param configuration configuration pulled in
     * @return {@link ArangoClient}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "close")
    @Singleton
    @Primary
    public ArangoSyncClient getClient(ArangoSyncClientConfiguration configuration) {
        return new ArangoSyncClient(configuration);
    }
}
