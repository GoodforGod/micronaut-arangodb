package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.ApplicationContext;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 28.2.2020
 */
@Testcontainers
class ArangoClientConfiguratorTests extends ArangoRunner {

    @Container
    private static final ArangoContainer container = getContainer()
            .withFixedPort(8528);

    @Test
    void testArangoDBBuilderConfiguratorCall() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", "localhost:8528,localhost:8528");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            TestArangoDBBuilderConfigurator configurator = new TestArangoDBBuilderConfigurator();
            context.registerSingleton(ArangoDBBuilderConfigurator.class, configurator);

            final ArangoClient client = context.getBean(ArangoClient.class);
            assertTrue(client.db().exists());

            assertEquals(1, configurator.getCallTimes());
        }
    }

    @Test
    void testArangoDBAsyncBuilderConfiguratorCall() throws ExecutionException, InterruptedException {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("arangodb.hosts", "localhost:8528,localhost:8528");

        try (final ApplicationContext context = ApplicationContext.run(properties)) {
            TestArangoDBAsyncBuilderConfigurator configurator = new TestArangoDBAsyncBuilderConfigurator();
            context.registerSingleton(TestArangoDBAsyncBuilderConfigurator.class, configurator);

            final ArangoClientAsync client = context.getBean(ArangoClientAsync.class);
            assertTrue(client.db().exists().get());

            assertEquals(1, configurator.getCallTimes());
        }
    }

    static class TestArangoDBBuilderConfigurator implements ArangoDBBuilderConfigurator {
        private int callTimes;

        @Override
        public void accept(ArangoDB.Builder builder) {
            callTimes++;
        }

        public int getCallTimes() {
            return callTimes;
        }
    }

    static class TestArangoDBAsyncBuilderConfigurator implements ArangoDBAsyncBuilderConfigurator {
        private int callTimes;

        @Override
        public void accept(ArangoDBAsync.Builder builder) {
            callTimes++;
        }

        public int getCallTimes() {
            return callTimes;
        }
    }
}
