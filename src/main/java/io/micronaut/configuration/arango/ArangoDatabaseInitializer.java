package io.micronaut.configuration.arango;

import com.arangodb.ArangoDBException;
import com.arangodb.async.ArangoDBAsync;
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
    protected void setupDatabase(ArangoAsyncConfiguration configuration) {
        if (configuration.isCreateDatabaseAsync()) {
            CompletableFuture.runAsync(() -> initializeDatabaseSynchronously(configuration))
                    .exceptionally(e -> {
                        logger.error(e.getMessage());
                        return null;
                    });
        } else {
            initializeDatabaseSynchronously(configuration);
        }
    }

    private void initializeDatabaseSynchronously(ArangoAsyncConfiguration configuration) {
        final String database = configuration.getDatabase();
        final int timeout = configuration.getCreateDatabaseTimeoutInMillis();
        if (ArangoSettings.SYSTEM_DATABASE.equals(database)) {
            logger.debug("Arango is configured to use System Database, skipping initialization");
            return;
        }

        try {
            logger.debug("Arango Database '{}' initialization starting...", database);
            final ArangoDBAsync accessor = configuration.getAccessor();
            final long startTime = System.nanoTime();
            accessor.createDatabase(database).get(timeout, TimeUnit.MILLISECONDS);
            final long tookTime = System.nanoTime() - startTime;
            logger.debug("Arango Database '{}' creation took '{}' millis", database, tookTime / 1000000);
        } catch (TimeoutException e) {
            throw new ApplicationStartupException("Arango Database initialization timed out in '" + timeout + "' millis");
        } catch (Exception e) {
            final Integer code = (e.getCause() instanceof ArangoDBException) ? ((ArangoDBException) e.getCause()).getResponseCode() : null;
            if (code == null)
                throw new ApplicationStartupException("Arango Database initialization failed without code due to: " + e.getMessage());

            switch (code) {
                case 400:
                case 409:
                    logger.debug("Arango Database '{}' already exists", database);
                    return;
                default:
                    throw new ApplicationStartupException(
                            "Arango Database initialization failed with code '" + code + "' and error: " + e.getMessage());
            }
        }
    }
}
