package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

import static com.arangodb.internal.ArangoDefaults.DEFAULT_HOST;
import static com.arangodb.internal.ArangoDefaults.DEFAULT_PORT;
import static io.micronaut.configuration.arango.ArangoSettings.DEFAULT_DATABASE;

/**
 * ArangoDB configuration class.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(property = ArangoSettings.PREFIX)
@Requires(classes = ArangoDBAsync.Builder.class)
@ConfigurationProperties(ArangoSettings.PREFIX)
public class ArangoConfiguration {

    @ConfigurationBuilder(prefixes = "", excludes = { "host" })
    protected ArangoDBAsync.Builder config = new ArangoDBAsync.Builder();

    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private String database = DEFAULT_DATABASE;

    private boolean createDatabaseIfNotExist = false;

    /**
     * @return whenever to create database on client creation
     */
    public boolean isCreateDatabaseIfNotExist() {
        return createDatabaseIfNotExist;
    }

    /**
     * @param createDatabaseIfNotExist indicates to create database if not exist
     *                                 while creating client
     */
    public void setCreateDatabaseIfNotExist(boolean createDatabaseIfNotExist) {
        this.createDatabaseIfNotExist = createDatabaseIfNotExist;
    }

    public String getHost() {
        return host;
    }

    /**
     * @param host for arango database to connect
     */
    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    /**
     * @param port for arango database to connect
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return clients database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * @param database to set for client
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * @return client configuration builder
     */
    public ArangoDBAsync.Builder getConfigBuilder() {
        return config;
    }

    /**
     * @return client configuration
     */
    public ArangoDBAsync getAccessor() {
        return config.host(getHost(), getPort()).build();
    }
}
