package io.micronaut.configuration.arango;

import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.Assert;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
public abstract class ArangoRunner extends Assert {

    protected static ArangoContainer getContainer() {
        return new ArangoContainer().withoutAuth();
    }
}
