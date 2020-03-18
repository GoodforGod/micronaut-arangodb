package io.micronaut.configuration.arango.health;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DocumentCreateEntity;
import io.micronaut.configuration.arango.ArangoClient;
import io.micronaut.configuration.arango.ArangoRunner;
import io.micronaut.context.annotation.Property;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.Single;
import io.testcontainers.arangodb.cluster.ArangoClusterDefault;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Tests when health is UP for mocked ArangoDB cluster
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Property(name = "arangodb.health.cluster.enabled", value = "true")
@Property(name = "arangodb.host", value = "localhost")
@Property(name = "arangodb.port", value = "8529")
@MicronautTest
@Testcontainers
class ArangoClusterTests extends ArangoRunner {

    private static ArangoClusterDefault clusterDefault = ArangoClusterDefault.build();

    @Container
    private static final ArangoContainer agency1 = clusterDefault.getAgency1();
    @Container
    private static final ArangoContainer agency2 = clusterDefault.getAgency2();
    @Container
    private static final ArangoContainer agency3 = clusterDefault.getAgency3();
    @Container
    private static final ArangoContainer db1 = clusterDefault.getDatabase1();
    @Container
    private static final ArangoContainer db2 = clusterDefault.getDatabase2();
    @Container
    private static final ArangoContainer coordinator1 = clusterDefault.getCoordinator1();
    @Container
    private static final ArangoContainer coordinator2 = clusterDefault.getCoordinator2();

    @Inject
    private ArangoClusterHealthIndicator clusterHealthIndicator;
    @Inject
    private ArangoHealthIndicator healthIndicator;
    @Inject
    private ArangoClient client;

    @Test
    void simpleHealthUpWhenClusterUp() {
        final HealthResult result = Single.fromPublisher(healthIndicator.getResult())
                .timeout(10, TimeUnit.SECONDS).blockingGet();
        assertNotNull(result);

        assertEquals(HealthStatus.UP, result.getStatus());
        assertEquals("arangodb", result.getName());
        assertNotNull(result.getDetails());
        assertTrue(result.getDetails() instanceof Map);
    }

    @Test
    void healthClusterUpWhenClusterUp() {
        final HealthResult result = Single.fromPublisher(clusterHealthIndicator.getResult())
                .timeout(10, TimeUnit.SECONDS).blockingGet();
        assertNotNull(result);

        assertEquals(HealthStatus.UP, result.getStatus());
        assertEquals("arangodb (cluster)", result.getName());
        assertNotNull(result.getDetails());
        assertTrue(result.getDetails() instanceof Map);
        assertTrue(((Map) result.getDetails()).get("nodes") instanceof Collection);
        assertEquals(7, ((Collection) ((Map) result.getDetails()).get("nodes")).size());
    }

    @Test
    void createDatabaseAndCollectionForCluster() throws Exception {
        final String dbName = "my-database";
        final String collectionName = "my-collection";
        final Boolean createdDb = client.accessor().db(dbName).create().get(60, TimeUnit.SECONDS);
        assertTrue(createdDb);

        final CollectionEntity collection = client.accessor().db(dbName).collection(collectionName).create()
                .get(60, TimeUnit.SECONDS);
        assertNotNull(collection);
        assertNotNull(collection.getId());

        final BaseDocument document = new BaseDocument();
        document.setId(UUID.randomUUID().toString());
        document.addAttribute("my-property", "yes");

        final DocumentCreateEntity<BaseDocument> docEntity = client.accessor()
                .db(dbName).collection(collectionName).insertDocument(document)
                .get(60, TimeUnit.SECONDS);
        assertNotNull(docEntity);
        assertNotNull(docEntity.getId());
    }
}
