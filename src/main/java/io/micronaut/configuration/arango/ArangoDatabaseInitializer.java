package io.micronaut.configuration.arango;

import com.arangodb.ArangoDBException;
import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.runtime.exceptions.ApplicationStartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ArangoDB database context Initialization
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoAsyncConfiguration#isCreateDatabaseIfNotExist()
 * @since 16.3.2020
 */
@Requires(property = ArangoSettings.PREFIX + ".createDatabaseIfNotExist", value = "true", defaultValue = "false")
@Requires(beans = ArangoAsyncConfiguration.class)
@Context
@Internal
public class ArangoDatabaseInitializer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${arangodb.createDatabaseIfNotExist.timeout:10}")
    private int createTimeout;

    @PostConstruct
    @Inject
    protected void setupDatabase(ArangoAsyncConfiguration configuration) {
        final String database = configuration.getDatabase();
        if (ArangoSettings.SYSTEM_DATABASE.equals(database)) {
            logger.debug("Arango is configured to use System Database, skipping initialization");
            return;
        }

        try {
            logger.debug("Arango Database '{}' initialization starting...", database);
            final ArangoDBAsync accessor = configuration.getAccessor();
            final long startTime = System.nanoTime();
            accessor.createDatabase(database).get(createTimeout, TimeUnit.SECONDS);
            final long tookTime = System.nanoTime() - startTime;
            logger.debug("Arango Database '{}' creation took '{}' millis", database, tookTime / 1000000);
        } catch (ExecutionException e) {
            final Integer code = (e.getCause() instanceof ArangoDBException) ? ((ArangoDBException) e.getCause()).getResponseCode() : null;
            if (code == null)
                throw new ApplicationStartupException("Arango Database initialization failed without code due to: " + e.getMessage());

            switch (code) {
                case 400:
                case 409:
                    logger.debug("Arango Database '{}' already exists", database);
                    return;
                default:
                    throw new ConfigurationException(
                            "Arango Database initialization failed with code '" + code + "' and error: " + e.getMessage());
            }
        } catch (InterruptedException | TimeoutException e) {
            throw new ApplicationStartupException("Arango Database initialization timed out in '" + createTimeout + "' millis");
        }
    }
}
