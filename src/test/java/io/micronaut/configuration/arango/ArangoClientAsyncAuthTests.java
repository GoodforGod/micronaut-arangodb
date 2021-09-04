package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import com.arangodb.entity.DatabaseEntity;
import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
@Testcontainers
class ArangoClientAsyncAuthTests extends ArangoRunner {

    private static final String PASS = "mypass";

    @Container
    private static final ArangoContainer ARANGO_CONTAINER = new ArangoContainer(ArangoContainer.LATEST)
            .withFixedPort(ArangoContainer.PORT_DEFAULT)
            .withPassword(PASS);

    @Test
    void createConnectionWithUserAndPassword() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.user", "tom");
        properties.put("arangodb.password", "1234");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoDBAsync accessor = context.getBean(ArangoDBAsync.class);
            assertNotNull(accessor.toString());
            accessor.db(ArangoSettings.SYSTEM_DATABASE).getInfo().join();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void createConnectionSuccessWithCorrectAuth() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.user", "root");
        properties.put("arangodb.password", PASS);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoDBAsync accessor = context.getBean(ArangoDBAsync.class);
            final DatabaseEntity entity = accessor.db(ArangoSettings.SYSTEM_DATABASE).getInfo().join();
            assertNotNull(entity);
        }
    }
}
