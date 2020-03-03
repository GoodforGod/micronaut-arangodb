package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.context.scope.Refreshable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Default factory for creating ArangoDB {@link ArangoDBAsync}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(beans = ArangoConfiguration.class)
@Factory
public class ArangoClientFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Factory method to return a client.
     *
     * @param configuration configuration pulled in
     * @return {@link ArangoClient}
     */
    @Refreshable(ArangoSettings.PREFIX)
    @Bean(preDestroy = "shutdown")
    @Primary
    public ArangoClient getArangoClient(ArangoConfiguration configuration) {
        final ArangoDBAsync accessor = configuration.getConfigBuilder()
                .host(configuration.getHost(), configuration.getPort())
                .build();

        final ArangoClient client = new ArangoClient(configuration.getDatabase(), accessor);

        createDatabaseIfConfigured(configuration, client);

        return client;
    }

    /**
     * Creates database {@link ArangoConfiguration#isCreateDatabaseIfNotExist()} if
     * configured in {@link ArangoConfiguration}
     *
     * @param configuration for ArngoDB client.
     * @param client        ArangoDB.
     */
    private void createDatabaseIfConfigured(ArangoConfiguration configuration, ArangoClient client) {
        final boolean isDatabaseSystem = !ArangoSettings.DEFAULT_DATABASE.equals(configuration.getDatabase());

        if (configuration.isCreateDatabaseIfNotExist() && !isDatabaseSystem) {
            client.accessor().db(configuration.getDatabase()).exists().thenCompose(isExist -> {
                if (isExist) {
                    logger.debug("Database '{}' is already initialized", configuration.getDatabase());
                    return CompletableFuture.completedFuture(true);
                } else {
                    logger.debug("Creating arango database '{}' as specified for Arango configuration, " +
                            "you can turn off initial database creating by setting 'createDatabaseIfNotExist' property to 'false'",
                            configuration.getDatabase());
                    return client.accessor().createDatabase(configuration.getDatabase());
                }
            }).exceptionally(e -> {
                logger.error("Failed to setup database with '{}' error message", e.getMessage());
                return false;
            });
        } else if (isDatabaseSystem) {
            logger.debug("Database creation is set to 'false', skipping database creation...");
        } else {
            logger.debug("Database creation is set to 'true', for '{}' database, skipping database creation...",
                    ArangoSettings.DEFAULT_DATABASE);
        }
    }
}
