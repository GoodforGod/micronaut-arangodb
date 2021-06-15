package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import com.arangodb.entity.DatabaseEntity;
import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
@Testcontainers
class ArangoConfigurationAuthTests extends ArangoRunner {

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
            final ArangoDB accessor = context.getBean(ArangoDB.class);
            assertNotNull(accessor.toString());
            accessor.db(ArangoSettings.SYSTEM_DATABASE).getInfo();
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

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);

            final ArangoDB accessor = context.getBean(ArangoDB.class);
            assertNotNull(configuration.getHealth());
            assertTrue(configuration.getHealth().isEnabled());
            assertNotNull(configuration.getHealthCluster());
            assertFalse(configuration.getHealthCluster().isEnabled());

            final DatabaseEntity entity = accessor.db(ArangoSettings.SYSTEM_DATABASE).getInfo();
            assertNotNull(entity);

            configuration.setProtocol(Protocol.VST);
            configuration.setAcquireHostList(true);
            configuration.setConnectionTtl(10000L);
            configuration.setKeepAliveInterval(10000);
            configuration.setConnectionTtl(10000L);
            configuration.setHosts(List.of("localhost:8080", "localhost:8081"));
            configuration.setHost("localhost");
            final Properties configurationProperties = configuration.getProperties();
            assertNotNull(configurationProperties.getProperty(ArangoProperties.HOSTS));
            assertNotNull(configuration.toString());
        }
    }
}
