package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(beans = ArangoConfiguration.class)
@Factory
public class ArangoClientFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean(preDestroy = "shutdown")
    @Primary
    public ArangoClient getArangoClient(ArangoConfiguration configuration) {
        final ArangoDBAsync async = configuration.getConfigBuilder()
                .host(configuration.getHost(), configuration.getPort())
                .build();

        try {
            if (configuration.isCreateDatabaseIfNotExist()
                    && !ArangoSettings.DEFAULT_DATABASE.equals(configuration.getDatabase())) {
                logger.debug("Creating arango database '{}' as specified for arango configuration, " +
                        "you can turn off initial database creating by setting 'createDatabaseIfNotExist' property to 'false'",
                        configuration.getDatabase());

                async.createDatabase(configuration.getDatabase()).get(2, TimeUnit.MINUTES);
            } else {
                logger.debug("Database creation is set to 'false', skipping database creation...");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return new ArangoClient(configuration.getDatabase(), async);
    }
}
