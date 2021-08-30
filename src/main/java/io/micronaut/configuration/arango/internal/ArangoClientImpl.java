package io.micronaut.configuration.arango.internal;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import io.micronaut.configuration.arango.ArangoAsyncConfiguration;
import io.micronaut.configuration.arango.ArangoClient;
import io.micronaut.configuration.arango.ArangoConfiguration;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * ArangoDB Sync Accessor {@link ArangoDB} and database name as configured for
 * application.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
public class ArangoClientImpl implements ArangoClient {

    /**
     * Configured database name for application
     * {@link ArangoAsyncConfiguration#getDatabase()}.
     */
    private final ArangoDatabase database;
    private final Map<String, Object> properties;

    /**
     * ArangoDB accessor {@link ArangoDB}.
     */
    private final ArangoDB accessor;

    public ArangoClientImpl(ArangoDB accessor, ArangoConfiguration configuration) {
        this.accessor = accessor;
        this.database = accessor.db(configuration.getDatabase());
        this.properties = configuration.getProperties().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(e -> e.getKey().toString(), Map.Entry::getValue));
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
    public Map<String, Object> properties() {
        return properties;
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
