package io.micronaut.configuration.arango;

import com.arangodb.ArangoDBException;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.runtime.exceptions.ApplicationStartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ArangoDB database context Initialization
 *
 * @author Anton Kurako (GoodforGod)
 * @see AbstractArangoConfiguration#isCreateDatabaseIfNotExist()
 * @since 16.3.2020
 */
@Requires(property = ArangoSettings.PREFIX + ".create-database-if-not-exist", value = "true", defaultValue = "false")
@Requires(beans = ArangoAsyncConfiguration.class)
@Context
@Internal
public class ArangoDatabaseInitializer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void setupDatabase(ArangoClientAsync clientAsync,
                              ArangoAsyncConfiguration configuration) {
        final String database = configuration.getDatabase();
        if (ArangoSettings.SYSTEM_DATABASE.equals(database)) {
            logger.debug("Arango is configured to use System Database, skipping initialization");
            return;
        }

        if (configuration.isCreateDatabaseAsync()) {
            CompletableFuture.runAsync(() -> {
                try {
                    initializeDatabaseSynchronously(clientAsync, configuration);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            });
        } else {
            initializeDatabaseSynchronously(clientAsync, configuration);
        }
    }

    protected void initializeDatabaseSynchronously(ArangoClientAsync clientAsync,
                                                   ArangoAsyncConfiguration configuration) {
        final String database = configuration.getDatabase();
        final int timeout = configuration.getCreateDatabaseTimeoutInMillis();

        try {
            logger.debug("Arango Database '{}' initialization starting...", database);
            final long startTime = System.currentTimeMillis();
            clientAsync.db().exists()
                    .thenCompose(exist -> exist
                            ? CompletableFuture.completedFuture(true)
                            : clientAsync.db().create())
                    .get(timeout, TimeUnit.MILLISECONDS);
            final long tookTime = System.currentTimeMillis() - startTime;
            logger.debug("Arango Database '{}' creation took '{}' millis", database, tookTime);
        } catch (TimeoutException e) {
            throw new ApplicationStartupException("Arango Database initialization timed out in '" + timeout + "' millis");
        } catch (ArangoDBException e) {
            final Integer code = e.getResponseCode();
            switch (code) {
                case 400:
                case 409:
                    logger.debug("Arango Database '{}' already exists", database);
                    return;
                default:
                    throw new ApplicationStartupException(
                            "Arango Database initialization failed with code '" + code + "' and error: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new ApplicationStartupException("Arango Database initialization failed without code due to: " + e.getMessage());
        }
    }
}
