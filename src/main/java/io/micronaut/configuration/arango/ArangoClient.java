package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;

/**
 * ArangoDB accessor {@link ArangoDBAsync} and database name as configured for
 * application.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
public class ArangoClient implements AutoCloseable {

    /**
     * Configured database name for application
     * {@link ArangoConfiguration#getDatabase()}.
     */
    private final String database;

    /**
     * ArangoDB accessor {@link ArangoDBAsync}.
     */
    private final ArangoDBAsync arangodb;

    public ArangoClient(ArangoConfiguration configuration) {
        this.database = configuration.getDatabase();
        this.arangodb = configuration.getAccessor();
    }

    public String getDatabase() {
        return database;
    }

    /**
     * @return Accessor to specified ArangoDB database.
     */
    public ArangoDatabaseAsync db() {
        return arangodb.db(database);
    }

    /**
     * @return Configured ArangoDB accessor {@link ArangoDBAsync}.
     */
    public ArangoDBAsync accessor() {
        return arangodb;
    }

    @Override
    public void close() {
        if (arangodb != null)
            arangodb.shutdown();
    }
}
