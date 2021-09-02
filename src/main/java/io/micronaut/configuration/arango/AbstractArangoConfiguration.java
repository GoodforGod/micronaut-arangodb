package io.micronaut.configuration.arango;

import static io.micronaut.configuration.arango.ArangoSettings.SYSTEM_DATABASE;

import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.InternalArangoDBBuilder;
import io.micronaut.configuration.arango.ssl.ArangoSSLConfiguration;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Abstract ArangoDB configuration class.
 *
 * @see ArangoProperties
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
public abstract class AbstractArangoConfiguration {

    protected final ArangoSSLConfiguration sslConfiguration;

    private String user = ArangoDefaults.DEFAULT_USER;
    private String password;
    private String host = ArangoDefaults.DEFAULT_HOST;
    private List<String> hosts;
    private int port = ArangoDefaults.DEFAULT_PORT;
    private String database = SYSTEM_DATABASE;
    private Duration timeout = Duration.ofSeconds(10);
    private int chunksize = ArangoDefaults.CHUNK_DEFAULT_CONTENT_SIZE;
    private int maxConnections = ArangoDefaults.MAX_CONNECTIONS_VST_DEFAULT;
    private Long connectionTtl;
    private Integer keepAliveInterval;
    private boolean acquireHostList = ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST;
    private int acquireHostListInterval = ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST_INTERVAL;
    private LoadBalancingStrategy loadBalancingStrategy = ArangoDefaults.DEFAULT_LOAD_BALANCING_STRATEGY;

    private boolean createDatabaseIfNotExist = false;
    private boolean createDatabaseAsync = false;
    private Duration createDatabaseTimeout = Duration.ofSeconds(10);

    protected AbstractArangoConfiguration(ArangoSSLConfiguration sslConfiguration) {
        this.sslConfiguration = sslConfiguration;
    }

    /**
     * @see ArangoProperties
     * @see InternalArangoDBBuilder
     * @return client configuration properties
     */
    public Properties getProperties() {
        final Properties properties = new Properties();
        if (CollectionUtils.isNotEmpty(getHosts())) {
            final String hostAsProperty = String.join(",", getHosts());
            properties.setProperty(ArangoProperties.HOSTS, hostAsProperty);
        }
        properties.setProperty(ArangoProperties.HOST, getHost());
        properties.setProperty(ArangoProperties.PORT, String.valueOf(getPort()));
        properties.setProperty(ArangoProperties.USER, getUser());
        if (StringUtils.isNotEmpty(getPassword())) {
            properties.setProperty(ArangoProperties.PASSWORD, getPassword());
        }
        properties.setProperty(ArangoProperties.TIMEOUT, String.valueOf(getTimeout().toMillis()));
        properties.setProperty(ArangoProperties.USE_SSL, String.valueOf(getSslConfiguration().isEnabled()));
        properties.setProperty(ArangoProperties.CHUNK_SIZE, String.valueOf(getChunksize()));
        properties.setProperty(ArangoProperties.MAX_CONNECTIONS, String.valueOf(getMaxConnections()));
        if (getConnectionTtl() != null) {
            properties.setProperty(ArangoProperties.CONNECTION_TTL, String.valueOf(getConnectionTtl()));
        }
        if (getKeepAliveInterval() != null) {
            properties.setProperty(ArangoProperties.KEEP_ALIVE_INTERVAL, String.valueOf(getKeepAliveInterval()));
        }
        properties.setProperty(ArangoProperties.ACQUIRE_HOST_LIST, String.valueOf(getAcquireHostList()));
        properties.setProperty(ArangoProperties.ACQUIRE_HOST_LIST_INTERVAL, String.valueOf(getAcquireHostListInterval()));
        properties.setProperty(ArangoProperties.LOAD_BALANCING_STRATEGY, String.valueOf(getLoadBalancingStrategy()));
        return properties;
    }

    /**
     * @see #getProperties()
     * @return properties as input stream
     */
    public InputStream getPropertiesAsInputStream() {
        final Properties properties = getProperties();
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            properties.store(outputStream, "arangodb");
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

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
    public Duration getCreateDatabaseTimeout() {
        return createDatabaseTimeout;
    }

    /**
     * @param createDatabaseTimeout database create timeout in millis
     */
    public void setCreateDatabaseTimeout(Duration createDatabaseTimeout) {
        if (createDatabaseTimeout.isNegative())
            throw new ConfigurationException("Timeout for create database can not be less than 0");
        this.createDatabaseTimeout = createDatabaseTimeout;
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

    /**
     * Multiple hosts to set
     * 
     * @see com.arangodb.ArangoDB.Builder#host(String, int)
     * @return value
     */
    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = List.copyOf(hosts);
    }

    public void setHosts(String hosts) {
        if (StringUtils.isNotEmpty(hosts)) {
            this.hosts = Arrays.stream(hosts.split(",")).sequential()
                    .distinct()
                    .collect(Collectors.toUnmodifiableList());
        }
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
     * @see com.arangodb.ArangoDB.Builder#password(String)
     * @return value
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#timeout(Integer)
     * @return value
     */
    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        if (timeout.isNegative())
            throw new ConfigurationException("Timeout for driver can not be less than 0");
        this.timeout = timeout;
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#chunksize(Integer)
     * @return value
     */
    public int getChunksize() {
        return chunksize;
    }

    public void setChunksize(int chunksize) {
        this.chunksize = chunksize;
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#maxConnections(Integer)
     * @return value
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#connectionTtl(Long)
     * @return value
     */
    public Long getConnectionTtl() {
        return connectionTtl;
    }

    public void setConnectionTtl(Long connectionTtl) {
        this.connectionTtl = connectionTtl;
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#keepAliveInterval(Integer)
     * @return value
     */
    public Integer getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public void setKeepAliveInterval(Integer keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#acquireHostList(Boolean)
     * @return value
     */
    public boolean getAcquireHostList() {
        return acquireHostList;
    }

    public void setAcquireHostList(boolean acquireHostList) {
        this.acquireHostList = acquireHostList;
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#acquireHostListInterval(Integer)
     * @return value
     */
    public int getAcquireHostListInterval() {
        return acquireHostListInterval;
    }

    public void setAcquireHostListInterval(int acquireHostListInterval) {
        this.acquireHostListInterval = acquireHostListInterval;
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#loadBalancingStrategy(LoadBalancingStrategy)
     * @return value
     */
    public LoadBalancingStrategy getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    public void setLoadBalancingStrategy(LoadBalancingStrategy loadBalancingStrategy) {
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    public ArangoSSLConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    @Override
    public String toString() {
        return getProperties().toString();
    }
}
