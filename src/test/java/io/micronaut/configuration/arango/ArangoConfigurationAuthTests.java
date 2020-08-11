package io.micronaut.configuration.arango;

import com.arangodb.entity.DatabaseEntity;
import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
@Testcontainers
public class ArangoConfigurationAuthTests extends ArangoRunner {

    private static final String PASS = "mypass";

    @Container
    private static final ArangoContainer container = new ArangoContainer().withPassword(PASS);

    @Test
    void createConnectionWithUserAndPassword() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.user", "tom");
        properties.put("arangodb.password", "1234");

        final ApplicationContext context = ApplicationContext.run(properties);

        final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);
        assertNotNull(configuration.toString());

        try {
            configuration.getConfig().build().db(ArangoSettings.SYSTEM_DATABASE).getInfo();
            fail("Should've failed with auth error");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void createConnectionSuccessWithCorrectAuth() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.user", "root");
        properties.put("arangodb.password", PASS);

        final ApplicationContext context = ApplicationContext.run(properties);

        final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);
        final DatabaseEntity entity = configuration.getConfig().build().db(ArangoSettings.SYSTEM_DATABASE).getInfo();
        assertNotNull(entity);
    }
}
