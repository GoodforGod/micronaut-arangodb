package io.micronaut.configuration.arango;

import com.arangodb.internal.ArangoDefaults;

import static io.micronaut.configuration.arango.ArangoSettings.SYSTEM_DATABASE;

/**
 * Abstract ArangoDB configuration class.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
public abstract class AbstractArangoConfiguration {

    private String user = ArangoDefaults.DEFAULT_USER;
    private String host = ArangoDefaults.DEFAULT_HOST;
    private int port = ArangoDefaults.DEFAULT_PORT;
    private String database = SYSTEM_DATABASE;

    private boolean createDatabaseIfNotExist = false;

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
