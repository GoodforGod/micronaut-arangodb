package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;
import java.util.Map;

/**
 * ArangoDB Async Accessor {@link ArangoDBAsync} and database name as configured
 * for application.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
public interface ArangoClientAsync extends AutoCloseable {

    /**
     * @return Accessor to specified ArangoDB database
     *         {@link ArangoAsyncConfiguration#getDatabase()}.
     */
    ArangoDatabaseAsync db();

    /**
     * @return Configured ArangoDB accessor {@link ArangoDBAsync}.
     */
    ArangoDBAsync accessor();

    /**
     * @return Properties accessor as configured with {@link ArangoProperties}
     */
    Map<String, Object> properties();
}
