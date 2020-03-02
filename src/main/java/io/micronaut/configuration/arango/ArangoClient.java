package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;

import java.io.Closeable;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
public class ArangoClient implements Closeable {

    private final String database;
    private final ArangoDBAsync arangodb;

    public ArangoClient(String database, ArangoDBAsync db) {
        this.database = database;
        this.arangodb = db;
    }

    public String getDatabase() {
        return database;
    }

    /**
     * Connection to specified arango database
     *
     * @return arango db connection
     */
    public ArangoDatabaseAsync db() {
        return arangodb.db(database);
    }

    /**
     * Database as a arango client
     *
     * @return database
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
