package io.micronaut.configuration.arango;

import com.arangodb.ArangoDBException;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.runtime.exceptions.ApplicationStartupException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ArangoDB database initialization
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoConfiguration#isCreateDatabaseIfNotExist()
 * @since 16.3.2020
 */
@Requires(property = ArangoSettings.PREFIX + ".create-database-if-not-exist", value = "true", defaultValue = "false")
@Requires(beans = ArangoConfiguration.class)
@Context
@Internal
public class ArangoDatabaseInitializer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void setupDatabase(ArangoClient client, ArangoConfiguration configuration) {
        final String database = configuration.getDatabase();
        if (ArangoSettings.SYSTEM_DATABASE.equals(database)) {
            logger.debug("Arango is configured to use System Database, skipping initialization");
            return;
        }

        if (configuration.isCreateDatabaseAsync()) {
            CompletableFuture.runAsync(() -> {
                try {
                    initializeDatabaseSynchronously(client, configuration);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
        } else {
            initializeDatabaseSynchronously(client, configuration);
        }
    }

    protected void initializeDatabaseSynchronously(ArangoClient client, ArangoConfiguration configuration) {
        final String database = configuration.getDatabase();
        final Duration timeout = configuration.getCreateDatabaseTimeout();

        try {
            logger.debug("Arango Database '{}' initialization starting...", database);
            final long startTime = System.currentTimeMillis();
            if (!client.db().exists()) {
                CompletableFuture.supplyAsync(() -> client.db().create())
                        .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            }

            final long tookTime = System.currentTimeMillis() - startTime;
            logger.debug("Arango Database '{}' creation took '{}' millis", database, tookTime);
        } catch (TimeoutException e) {
            throw new ApplicationStartupException("Arango Database initialization timed out in '" + timeout + "' millis");
        } catch (ArangoDBException e) {
            throw e;
        } catch (Exception e) {
            final Throwable cause = (e instanceof CompletionException)
                    ? e.getCause()
                    : e;
            throw new ApplicationStartupException("Arango Database initialization failed due to: " + cause.getMessage(), cause);
        }
    }
}
