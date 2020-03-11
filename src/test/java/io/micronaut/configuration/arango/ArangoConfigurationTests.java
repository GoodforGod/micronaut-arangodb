package io.micronaut.configuration.arango;

import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@Testcontainers
class ArangoConfigurationTests {

    @Container
    private static final ArangoContainer container = new ArangoContainer().setPort(8528);

    @Test
    void createConnectionWithCustomDatabase() {
        final Map<String, Object> properties = new HashMap<>();
        final ApplicationContext context = ApplicationContext.run(

        );
    }

    @Test
    void createConnectionWithCreateDatabaseIfNotExistOnStartup() {

    }
}
