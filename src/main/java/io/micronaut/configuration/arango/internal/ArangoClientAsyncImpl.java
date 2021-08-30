package io.micronaut.configuration.arango.internal;

import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;
import io.micronaut.configuration.arango.ArangoAsyncConfiguration;
import io.micronaut.configuration.arango.ArangoClientAsync;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * ArangoDB Async Accessor {@link ArangoDBAsync} and database name as configured
 * for application.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
public class ArangoClientAsyncImpl implements ArangoClientAsync {

    /**
     * Configured database name for application
     * {@link ArangoAsyncConfiguration#getDatabase()}.
     */
    private final ArangoDatabaseAsync database;
    private final Map<String, Object> properties;

    /**
     * ArangoDB accessor {@link ArangoDBAsync}.
     */
    private final ArangoDBAsync accessor;

    public ArangoClientAsyncImpl(ArangoDBAsync accessor, ArangoAsyncConfiguration configuration) {
        this.accessor = accessor;
        this.database = accessor.db(configuration.getDatabase());
        this.properties = configuration.getProperties().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }

    /**
     * @return Accessor to specified ArangoDB database.
     */
    public ArangoDatabaseAsync db() {
        return database;
    }

    /**
     * @return Configured ArangoDB accessor {@link ArangoDBAsync}.
     */
    public ArangoDBAsync accessor() {
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
