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
final class ArangoClientImpl implements ArangoClient {

    /**
     * Configured database name for application {@link ArangoConfiguration#getDatabase()}.
     */
    private final ArangoDatabase database;

    /**
     * ArangoDB accessor {@link ArangoDB}.
     */
    private final ArangoDB accessor;

    public ArangoClientImpl(ArangoDB accessor, ArangoConfiguration configuration) {
        this.accessor = accessor;
        this.database = accessor.db(configuration.getDatabase());
    }

    /**
     * @return Accessor to specified ArangoDB database.
     */
    public ArangoDatabase db() {
        return database;
    }

    /**
     * @return Configured ArangoDB accessor {@link ArangoDB}.
     */
    public ArangoDB accessor() {
        return accessor;
    }

    @Override
    public void close() {
        if (accessor != null)
            accessor.shutdown();
    }

    @Override
    public String toString() {
        return "[database=" + database.name() + ']';
    }
}
