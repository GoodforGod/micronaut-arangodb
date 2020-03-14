package io.micronaut.configuration.arango.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.arango.ArangoClient;
import io.micronaut.configuration.arango.ArangoConfiguration;
import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.runtime.server.EmbeddedServer;
import io.reactivex.Single;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Tests when health is UP for mocked ArangoDB cluster
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Testcontainers
public class ArangoClusterHealthUpMockTests extends ArangoRunner {

    @Container
    private static final ArangoContainer agency = new ArangoContainer().withoutAuthentication()
            .setPort(8000)
            .setCommand("arangod --server.endpoint tcp://0.0.0.0:5001 --server.authentication false --agency.activate true --agency.size 1 --agency.supervision true --database.directory /var/lib/arangodb3/agency1");
    @Container
    private static final ArangoContainer coordinator1 = (ArangoContainer) new ArangoContainer().withoutAuthentication()
            .setPort(8001)
            .dependsOn(agency);
    @Container
    private static final ArangoContainer coordinator2 = (ArangoContainer) new ArangoContainer().withoutAuthentication()
            .setPort(8002)
            .dependsOn(agency);
    @Container
    private static final ArangoContainer db1 = (ArangoContainer) new ArangoContainer().withoutAuthentication()
            .setPort(8003)
            .dependsOn(agency);
    @Container
    private static final ArangoContainer db2 = (ArangoContainer) new ArangoContainer().withoutAuthentication()
            .setPort(8004)
            .dependsOn(agency);

    private static EmbeddedServer embeddedServer;

    @BeforeAll
    public static void setup() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.host", "localhost");
        properties.put("arangodb.port", "8090");
        properties.put("micronaut.server.port", "8090");
        properties.put("arango.mock.cluster.enabled", "true");
        properties.put("arangodb.health.enabled", "false");

        embeddedServer = ApplicationContext.run(EmbeddedServer.class, properties);
    }

    @Test
    void healthClusterDownWhenDatabaseIsSingle() {
        final ArangoConfiguration configuration = embeddedServer.getApplicationContext().getBean(ArangoConfiguration.class);
        configuration.getConfigBuilder().user(null);
        final ArangoClient client = new ArangoClient(configuration);
        final ArangoClusterHealthIndicator indicator = new ArangoClusterHealthIndicator(client, new ObjectMapper());

        final HealthResult result = Single.fromPublisher(indicator.getResult()).timeout(10, TimeUnit.SECONDS).blockingGet();
        assertNotNull(result);
//
//        assertEquals(HealthStatus.UP, result.getStatus());
//        assertEquals("arangodb (cluster)", result.getName());
//        assertNotNull(result.getDetails());
    }


    @AfterAll
    public static void stopServer() {
        if (embeddedServer != null) {
            embeddedServer.stop();
        }
    }
}
