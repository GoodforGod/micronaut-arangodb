package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;

/**
 * ArangoDB Sync Accessor {@link ArangoDB} and database name as configured for
 * application.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
public interface ArangoClient extends AutoCloseable {

    /**
     * @return Accessor to specified ArangoDB database
     *             {@link ArangoConfiguration#getDatabase()}.
     */
    ArangoDatabase db();

    /**
     * @return Configured ArangoDB accessor {@link ArangoDB}.
     */
    ArangoDB accessor();
}
