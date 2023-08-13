package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import com.arangodb.entity.DatabaseEntity;
import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArangoClientAuthTests extends ArangoRunner {

    private static final String PASS = "mypass";

    @Container
    private static final ArangoContainer<?> CONTAINER_3_11 = new ArangoContainer<>(IMAGE_3_11).withPassword(PASS);

    @Test
    void createConnectionWithUserAndPassword() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", List.of(CONTAINER_3_11.getHost() + ":" + CONTAINER_3_11.getPort()));
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
        properties.put("arangodb.hosts", List.of("localhost:" + CONTAINER_3_11.getPort()));
        properties.put("arangodb.user", "root");
        properties.put("arangodb.password", PASS);

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            final ArangoConfiguration configuration = context.getBean(ArangoConfiguration.class);

            final ArangoDB accessor = context.getBean(ArangoDB.class);
            final DatabaseEntity entity = accessor.db(ArangoSettings.SYSTEM_DATABASE).getInfo();
            assertNotNull(entity);

            configuration.setProtocol(Protocol.VST);
            configuration.setAcquireHostList(true);
            configuration.setConnectionTtl(Duration.ofMillis(10000L));
            configuration.setKeepAliveInterval(Duration.ofMillis(10000));
            configuration.setConnectionTtl(Duration.ofMillis(10000L));
            configuration.setHosts(List.of("localhost:8080", "localhost:8081"));
            configuration.setJwt("123");
            configuration.setResponseQueueTimeSamples(123);

            final Properties configurationProperties = configuration.getProperties();
            assertNotNull(configurationProperties.getProperty(ArangoProperties.HOSTS));
            assertNotNull(configuration.toString());
        }
    }
}
