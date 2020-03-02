package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;

import java.io.Closeable;

/**
 * ArangoDB accessor {@link ArangoDBAsync} and database name as configured for
 * application.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
public class ArangoClient implements Closeable {

    /**
     * Configured database name for application
     * {@link ArangoConfiguration#getDatabase()}.
     */
    private final String database;

    /**
     * ArangoDB accessor {@link ArangoDBAsync}.
     */
    private final ArangoDBAsync arangodb;

    public ArangoClient(String database, ArangoDBAsync db) {
        this.database = database;
        this.arangodb = db;
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
