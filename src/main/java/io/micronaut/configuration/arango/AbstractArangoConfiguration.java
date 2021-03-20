package io.micronaut.configuration.arango;

import com.arangodb.internal.ArangoDefaults;
import io.micronaut.context.annotation.ConfigurationBuilder;

import static io.micronaut.configuration.arango.ArangoSettings.SYSTEM_DATABASE;

/**
 * Abstract ArangoDB configuration class.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
public abstract class AbstractArangoConfiguration {

    @ConfigurationBuilder("health")
    protected EnableConfiguration health = new EnableConfiguration(true);
    @ConfigurationBuilder("health-cluster")
    protected EnableConfiguration healthCluster = new EnableConfiguration(false);

    private String user = ArangoDefaults.DEFAULT_USER;
    private String host = ArangoDefaults.DEFAULT_HOST;
    private int port = ArangoDefaults.DEFAULT_PORT;
    private String database = SYSTEM_DATABASE;

    private boolean createDatabaseIfNotExist = false;
    private boolean createDatabaseAsync = false;
    private int createDatabaseTimeoutInMillis = 10000;

    /**
     * @return whenever to create database on context initialization
     */
    public boolean isCreateDatabaseIfNotExist() {
        return createDatabaseIfNotExist;
    }

    /**
     * @param createDatabaseIfNotExist indicates to create database if not exist
     *                                 while context initialization
     */
    public void setCreateDatabaseIfNotExist(boolean createDatabaseIfNotExist) {
        this.createDatabaseIfNotExist = createDatabaseIfNotExist;
    }

    /**
     * @return true if database should be created asynchronously
     */
    public boolean isCreateDatabaseAsync() {
        return createDatabaseAsync;
    }

    public void setCreateDatabaseAsync(boolean createDatabaseAsync) {
        this.createDatabaseAsync = createDatabaseAsync;
    }

    /**
     * @return database create timeout in millis
     */
    public int getCreateDatabaseTimeoutInMillis() {
        return createDatabaseTimeoutInMillis;
    }

    /**
     * @param createDatabaseTimeoutInMillis database create timeout in millis
     */
    public void setCreateDatabaseTimeoutInMillis(int createDatabaseTimeoutInMillis) {
        this.createDatabaseTimeoutInMillis = createDatabaseTimeoutInMillis;
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
     * @return user configured
     */
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return health indicator configuration
     */
    public EnableConfiguration getHealth() {
        return health;
    }

    /**
     * @return cluster health indicator configuration
     */
    public EnableConfiguration getHealthCluster() {
        return healthCluster;
    }

    @Override
    public String toString() {
        return "AbstractArangoConfiguration{" +
                "user='" + user + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", database='" + database + '\'' +
                ", createDatabaseIfNotExist=" + createDatabaseIfNotExist +
                '}';
    }
}
