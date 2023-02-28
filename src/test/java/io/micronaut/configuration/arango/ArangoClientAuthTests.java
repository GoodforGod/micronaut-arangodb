package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import com.arangodb.DbName;
import com.arangodb.Protocol;
import com.arangodb.entity.DatabaseEntity;
import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
@Testcontainers
class ArangoClientAuthTests extends ArangoRunner {

    private static final String PASS = "mypass";

    @Container
    private static final ArangoContainer CONTAINER = getContainer()
            .withFixedPort(ArangoContainer.DEFAULT_PORT)
            .withPassword(PASS);

    @Test
    void createConnectionWithUserAndPassword() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", CONTAINER.getPort());
        properties.put("arangodb.user", "tom");
        properties.put("arangodb.password", "1234");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoDB accessor = context.getBean(ArangoDB.class);
            assertNotNull(accessor.toString());
            accessor.db(DbName.of(ArangoSettings.SYSTEM_DATABASE)).getInfo();
            fail("Should've failed with auth error");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void createConnectionSuccessWithCorrectAuth() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.port", CONTAINER.getPort());
        properties.put("arangodb.user", "root");
        properties.put("arangodb.password", PASS);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);

            final ArangoDB accessor = context.getBean(ArangoDB.class);
            final DatabaseEntity entity = accessor.db(DbName.of(ArangoSettings.SYSTEM_DATABASE)).getInfo();
            assertNotNull(entity);

            configuration.setProtocol(Protocol.VST);
            configuration.setAcquireHostList(true);
            configuration.setConnectionTtl(10000L);
            configuration.setKeepAliveInterval(10000);
            configuration.setConnectionTtl(10000L);
            configuration.setHosts(List.of("localhost:8080", "localhost:8081"));
            configuration.setHost("localhost");
            configuration.setJwt("123");
            configuration.setResponseQueueTimeSamples(123);

            final Properties configurationProperties = configuration.getProperties();
            assertNotNull(configurationProperties.getProperty(ArangoProperties.HOSTS));
            assertNotNull(configuration.toString());
        }
    }
}
