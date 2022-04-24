package io.micronaut.configuration.arango;

import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Assertions;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
public abstract class ArangoRunner extends Assertions {

    protected static ArangoContainer getContainer() {
        return new ArangoContainer(ArangoContainer.LATEST)
                .withoutAuth()
                .withFixedPort(ArangoContainer.DEFAULT_PORT);
    }
}
