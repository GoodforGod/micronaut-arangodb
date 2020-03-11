package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.context.scope.Refreshable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Default factory for creating ArangoDB client {@link ArangoClient}.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(beans = { ArangoConfiguration.class })
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
    @Bean(preDestroy = "close")
    @Singleton
    @Primary
    public ArangoClient getClient(ArangoConfiguration configuration) {
        final ArangoClient client = new ArangoClient(configuration);
        createDatabaseIfConfiguredAsync(configuration, client);
        return client;
    }

    /**
     * Creates database {@link ArangoConfiguration#isCreateDatabaseIfNotExist()} if
     * configured in {@link ArangoConfiguration}
     *
     * @param configuration for ArngoDB client.
     * @param client        ArangoDB.
     */
    private void createDatabaseIfConfiguredAsync(ArangoConfiguration configuration, ArangoClient client) {
        final boolean isDatabaseSystem = ArangoSettings.DEFAULT_DATABASE.equals(configuration.getDatabase());

        if (configuration.isCreateDatabaseIfNotExist() && !isDatabaseSystem) {
            try {
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
                }).get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Could not create datebase in 30 seconds, failed with: {}", e.getMessage());
            }
        } else if (isDatabaseSystem) {
            logger.debug("Database creation is set to 'true', for system database, skipping database creation...");
        } else {
            logger.debug("Database creation is set to 'true', for '{}' database, skipping database creation...",
                    ArangoSettings.DEFAULT_DATABASE);
        }
    }
}
