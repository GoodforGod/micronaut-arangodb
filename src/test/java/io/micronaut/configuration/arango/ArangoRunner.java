package io.micronaut.configuration.arango;

import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Assertions;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
public abstract class ArangoRunner extends Assertions {

    protected static final String IMAGE_3_11 = "arangodb:3.11.2";
    protected static final String IMAGE_3_7 = "arangodb:3.7.18";

    protected static ArangoContainer<?> getContainer() {
        return new ArangoContainer<>(IMAGE_3_11);
    }
}
