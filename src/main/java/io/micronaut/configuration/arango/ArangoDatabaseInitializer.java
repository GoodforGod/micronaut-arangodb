package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * ArangoDB database initialized activated by
 * {@link ArangoAsyncConfiguration#isCreateDatabaseIfNotExist()}
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoAsyncConfiguration#isCreateDatabaseIfNotExist()
 * @since 16.3.2020
 */
@Requires(property = "arangodb.createDatabaseIfNotExist", value = "true", defaultValue = "false")
@Requires(beans = ArangoAsyncConfiguration.class)
@Context
@Internal
public class ArangoDatabaseInitializer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${arangodb.createDatabaseIfNotExist.timeout:10}")
    private Integer createTimeout;

    @PostConstruct
    @Inject
    protected void setupDatabase(ArangoAsyncConfiguration configuration) {
        final String database = configuration.getDatabase();
        try {
            final long setupStart = System.nanoTime();
            setupDatabaseIfConfiguredAsync(configuration).get(createTimeout, TimeUnit.SECONDS);
            final long tookNanoTime = System.nanoTime() - setupStart;
            logger.debug("Database '{}' initialization took '{}' millis", database, tookNanoTime / 1000000);
        } catch (Exception e) {
            final Throwable t = e instanceof CompletionException ? e.getCause() : e;
            logger.error("Could not create '{}' database in {} seconds, failed with: {}", database, createTimeout, t.getMessage());
            throw new ConfigurationException("Could not initialize database due to connection failure: " + t.getMessage());
        }
    }

    /**
     * Creates database
     * {@link ArangoAsyncConfiguration#isCreateDatabaseIfNotExist()} if configured
     * in {@link ArangoAsyncConfiguration}
     *
     * @param configuration to get settings from
     * @return True if database was created or existed already, False otherwise
     */
    protected CompletableFuture<Boolean> setupDatabaseIfConfiguredAsync(ArangoAsyncConfiguration configuration) {
        if (!configuration.isCreateDatabaseIfNotExist()) {
            logger.debug("Database creation is set to 'false'");
            return CompletableFuture.completedFuture(true);
        }

        final ArangoDBAsync accessor = configuration.getAccessor();
        final String database = configuration.getDatabase();

        if (ArangoSettings.DEFAULT_DATABASE.equals(database)) {
            logger.debug("Database creation is set to 'true', for '{}' database, skipping database creation...",
                    ArangoSettings.DEFAULT_DATABASE);
            return CompletableFuture.completedFuture(true);
        }

        return accessor.db(database).exists().thenCompose(isExist -> {
            if (isExist) {
                logger.debug("Database '{}' is already initialized", database);
                return CompletableFuture.completedFuture(true);
            } else {
                logger.debug("Creating Arango database '{}' as specified per configuration, " +
                        "you can turn off initial database creating by setting 'createDatabaseIfNotExist' property to 'false'",
                        database);
                return accessor.createDatabase(database);
            }
        });
    }
}
