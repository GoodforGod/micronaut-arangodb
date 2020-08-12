package io.micronaut.configuration.arango;

import com.arangodb.ArangoDBException;
import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * ArangoDB database initialized activated by
 * {@link ArangoAsyncConfiguration#isCreateDatabaseIfNotExist()}
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
    private Integer createTimeout;

    @PostConstruct
    @Inject
    protected void setupDatabase(ArangoAsyncConfiguration configuration) {
        final String database = configuration.getDatabase();
        if (ArangoSettings.SYSTEM_DATABASE.equals(database)) {
            logger.debug("Arango is configured to use System Database");
            return;
        }

        try {
            final ArangoDBAsync accessor = configuration.getAccessor();
            final long startTime = System.nanoTime();
            accessor.db(database).create().get(createTimeout, TimeUnit.SECONDS);
            final long tookTime = System.nanoTime() - startTime;
            logger.debug("Arango Database '{}' creation took '{}' millis", database, tookTime / 1000000);
        } catch (Exception e) {
            if (e.getCause() instanceof ArangoDBException) {
                final ArangoDBException ex = (ArangoDBException) e.getCause();
                if (ex.getResponseCode() != null
                        && (ex.getResponseCode() == HttpStatus.CONFLICT.getCode()
                                || ex.getResponseCode() == HttpStatus.BAD_REQUEST.getCode())) {
                    logger.debug("Arango Database '{}' already exists", database);
                    return;
                }

                throw new ConfigurationException("Arango Database creation failed due to: " + ex.getMessage());
            } else {
                throw new ConfigurationException("Arango Database creation failed due to: " + e.getMessage());
            }
        }
    }
}
