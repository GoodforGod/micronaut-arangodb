package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * ArangoDB database initialized activated by
 * {@link ArangoClientConfiguration#isCreateDatabaseIfNotExist()}
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoClientConfiguration#isCreateDatabaseIfNotExist()
 * @since 16.3.2020
 */
@Requires(property = "arangodb.createDatabaseIfNotExist", value = "true", defaultValue = "false")
@Requires(beans = ArangoClientConfiguration.class)
@Context
public class ArangoDatabaseInitializer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    @Inject
    protected void setupDatabase(ArangoClientConfiguration configuration) {
        final String database = configuration.getDatabase();
        try {
            final long setupStart = System.nanoTime();
            setupDatabaseIfConfiguredAsync(configuration).get(30, TimeUnit.SECONDS);
            final long tookNanoTime = System.nanoTime() - setupStart;
            logger.info("Database '{}' initialization took '{}' millis", database, tookNanoTime / 1000000);
        } catch (Exception e) {
            logger.error("Could not create '{}' database in 30 seconds, failed with: {}", database, e.getMessage());
            throw new ConfigurationException("Could not initialize database due to connection failure: " + e.getMessage());
        }
    }

    /**
     * Creates database
     * {@link ArangoClientConfiguration#isCreateDatabaseIfNotExist()} if configured
     * in {@link ArangoClientConfiguration}
     *
     * @param configuration to get settings from
     * @return True if database was created or existed already, False otherwise
     */
    protected CompletableFuture<Boolean> setupDatabaseIfConfiguredAsync(ArangoClientConfiguration configuration) {
        final ArangoDBAsync accessor = configuration.getAccessor();
        final boolean isDatabaseSystem = ArangoSettings.DEFAULT_DATABASE.equals(configuration.getDatabase());

        if (configuration.isCreateDatabaseIfNotExist() && !isDatabaseSystem) {
            try {
                return accessor.db(configuration.getDatabase()).exists().thenCompose(isExist -> {
                    if (isExist) {
                        logger.debug("Database '{}' is already initialized", configuration.getDatabase());
                        return CompletableFuture.completedFuture(true);
                    } else {
                        logger.debug("Creating Arango database '{}' as specified for Arango configuration, " +
                                "you can turn off initial database creating by setting 'createDatabaseIfNotExist' property to 'false'",
                                configuration.getDatabase());
                        return accessor.createDatabase(configuration.getDatabase());
                    }
                }).exceptionally(e -> {
                    logger.error("Failed to setup database with '{}' error message", e.getMessage());
                    return false;
                });
            } catch (Exception e) {
                logger.error("Could not create database in 30 seconds, failed with: {}", e.getMessage());
                throw new ConfigurationException(
                        "Could not initialize database due to connection failure: " + configuration.getDatabase());
            }
        } else if (isDatabaseSystem) {
            logger.debug("Database creation is set to 'true', for system database, skipping database creation...");
            return CompletableFuture.completedFuture(true);
        } else {
            logger.debug("Database creation is set to 'true', for '{}' database, skipping database creation...",
                    ArangoSettings.DEFAULT_DATABASE);
            return CompletableFuture.completedFuture(true);
        }
    }
}
