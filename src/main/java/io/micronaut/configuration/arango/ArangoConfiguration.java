package io.micronaut.configuration.arango;

import static io.micronaut.configuration.arango.ArangoSettings.SYSTEM_DATABASE;

import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.HostDescription;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.InternalArangoDBBuilder;
import io.micronaut.configuration.arango.ssl.ArangoSSLConfiguration;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Abstract ArangoDB configuration class.
 *
 * @see ArangoProperties
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(property = ArangoSettings.PREFIX)
@ConfigurationProperties(ArangoSettings.PREFIX)
public class ArangoConfiguration {

    private static final class ArangoConfigPropertiesInternal implements ArangoConfigProperties {

        private final ArangoConfiguration configuration;

        private ArangoConfigPropertiesInternal(ArangoConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public Optional<List<HostDescription>> getHosts() {
            return Optional.ofNullable(configuration.getHostDescriptions());
        }

        @Override
        public Optional<Protocol> getProtocol() {
            return Optional.ofNullable(configuration.getProtocol());
        }

        @Override
        public Optional<String> getUser() {
            return Optional.ofNullable(configuration.getUser());
        }

        @Override
        public Optional<String> getPassword() {
            return Optional.ofNullable(configuration.getPassword());
        }

        @Override
        public Optional<String> getJwt() {
            return Optional.ofNullable(configuration.getJwt());
        }

        @Override
        public Optional<Integer> getTimeout() {
            return Optional.of(configuration.getTimeout().toMillisPart());
        }

        @Override
        public Optional<Boolean> getUseSsl() {
            return Optional.of(configuration.getSslConfiguration().isEnabled());
        }

        @Override
        public Optional<Boolean> getVerifyHost() {
            return Optional.ofNullable(configuration.getVerifyHost());
        }

        @Override
        public Optional<Integer> getChunkSize() {
            return Optional.of(configuration.getChunksize());
        }

        @Override
        public Optional<Integer> getMaxConnections() {
            return Optional.of(configuration.getConnectionMax());
        }

        @Override
        public Optional<Long> getConnectionTtl() {
            return Optional.ofNullable(configuration.getConnectionTtl());
        }

        @Override
        public Optional<Integer> getKeepAliveInterval() {
            return Optional.ofNullable(configuration.getKeepAliveInterval());
        }

        @Override
        public Optional<Boolean> getAcquireHostList() {
            return Optional.of(configuration.getAcquireHostList());
        }

        @Override
        public Optional<Integer> getAcquireHostListInterval() {
            return Optional.of(configuration.getAcquireHostListInterval());
        }

        @Override
        public Optional<LoadBalancingStrategy> getLoadBalancingStrategy() {
            return Optional.ofNullable(configuration.getLoadBalancingStrategy());
        }

        @Override
        public Optional<Integer> getResponseQueueTimeSamples() {
            return Optional.of(configuration.getResponseQueueTimeSamples());
        }
    }

    public static class ArangoSerdeConfig {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    protected final ArangoSSLConfiguration sslConfiguration;

    private String user = ArangoDefaults.DEFAULT_USER;
    private String password;
    private String jwt;
    private List<String> hosts;
    private String database = SYSTEM_DATABASE;
    private Protocol protocol = ArangoDefaults.DEFAULT_PROTOCOL;
    private Duration timeout = Duration.ofSeconds(10);
    private int chunksize = ArangoDefaults.DEFAULT_CHUNK_SIZE;
    private int connectionMax = ArangoDefaults.MAX_CONNECTIONS_HTTP2_DEFAULT;
    private Long connectionTtl;
    private Integer keepAliveInterval;
    private Boolean verifyHost = ArangoDefaults.DEFAULT_VERIFY_HOST;
    private boolean acquireHostList = ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST;
    private int acquireHostListInterval = ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST_INTERVAL;
    private LoadBalancingStrategy loadBalancingStrategy = ArangoDefaults.DEFAULT_LOAD_BALANCING_STRATEGY;
    private int responseQueueTimeSamples = ArangoDefaults.DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES;

    private boolean createDatabaseIfNotExist = false;
    private boolean createDatabaseAsync = false;
    private Duration createDatabaseTimeout = Duration.ofSeconds(10);

    @ConfigurationBuilder("serde")
    private final ArangoSerdeConfig serde = new ArangoSerdeConfig();

    public ArangoConfiguration(ArangoSSLConfiguration sslConfiguration) {
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

        properties.setProperty(ArangoProperties.USER, getUser());
        if (StringUtils.isNotEmpty(getPassword())) {
            properties.setProperty(ArangoProperties.PASSWORD, getPassword());
        }
        if (getJwt() != null) {
            properties.setProperty(ArangoProperties.JWT, getJwt());
        }
        properties.setProperty(ArangoProperties.PROTOCOL, String.valueOf(getProtocol()));
        properties.setProperty(ArangoProperties.TIMEOUT, String.valueOf(getTimeout().toMillis()));
        properties.setProperty(ArangoProperties.USE_SSL, String.valueOf(getSslConfiguration().isEnabled()));
        properties.setProperty(ArangoProperties.VERIFY_HOST, String.valueOf(getSslConfiguration().isEnabled()));
        properties.setProperty(ArangoProperties.CHUNK_SIZE, String.valueOf(getChunksize()));
        properties.setProperty(ArangoProperties.MAX_CONNECTIONS, String.valueOf(getConnectionMax()));
        if (getConnectionTtl() != null) {
            properties.setProperty(ArangoProperties.CONNECTION_TTL, String.valueOf(getConnectionTtl()));
        }
        if (getKeepAliveInterval() != null) {
            properties.setProperty(ArangoProperties.KEEP_ALIVE_INTERVAL, String.valueOf(getKeepAliveInterval()));
        }

        properties.setProperty(ArangoProperties.ACQUIRE_HOST_LIST, String.valueOf(getAcquireHostList()));
        properties.setProperty(ArangoProperties.ACQUIRE_HOST_LIST_INTERVAL, String.valueOf(getAcquireHostListInterval()));
        properties.setProperty(ArangoProperties.LOAD_BALANCING_STRATEGY, String.valueOf(getLoadBalancingStrategy()));
        properties.setProperty(ArangoProperties.RESPONSE_QUEUE_TIME_SAMPLES, String.valueOf(getResponseQueueTimeSamples()));
        return properties;
    }

    /**
     * @see #getProperties()
     * @return properties as input stream
     */
    public ArangoConfigProperties getArangoConfigProperties() {
        return new ArangoConfigPropertiesInternal(this);
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

    /**
     * Multiple hosts to set
     * 
     * @see com.arangodb.ArangoDB.Builder#host(String, int)
     * @return value
     */
    public List<String> getHosts() {
        if (CollectionUtils.isEmpty(hosts)) {
            throw new ConfigurationException("Hosts is empty, but is required!");
        }
        return hosts;
    }

    public List<HostDescription> getHostDescriptions() {
        return getHosts().stream()
                .map(HostDescription::parse)
                .collect(Collectors.toList());
    }

    /**
     * Multiple hosts to set
     *
     * @see com.arangodb.ArangoDB.Builder#host(String, int)
     */
    public void setHosts(List<String> hosts) {
        this.hosts = List.copyOf(hosts);
    }

    /**
     * Multiple hosts to set
     *
     * @see com.arangodb.ArangoDB.Builder#host(String, int)
     */
    public void setHosts(String hosts) {
        if (StringUtils.isNotEmpty(hosts)) {
            this.hosts = Arrays.stream(hosts.split(",")).sequential()
                    .distinct()
                    .collect(Collectors.toUnmodifiableList());
        }
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

    public String getJwt() {
        return jwt;
    }

    /**
     * @param jwt Sets the JWT for the user authentication.
     */
    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#chunkSize(Integer)
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
    public int getConnectionMax() {
        return connectionMax;
    }

    public void setConnectionMax(int connectionMax) {
        this.connectionMax = connectionMax;
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

    public boolean getAcquireHostList() {
        return acquireHostList;
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#acquireHostList(Boolean)
     */
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

    public Boolean getVerifyHost() {
        return verifyHost;
    }

    public void setVerifyHost(Boolean verifyHost) {
        this.verifyHost = verifyHost;
    }

    public int getResponseQueueTimeSamples() {
        return responseQueueTimeSamples;
    }

    public void setResponseQueueTimeSamples(int responseQueueTimeSamples) {
        this.responseQueueTimeSamples = responseQueueTimeSamples;
    }

    public LoadBalancingStrategy getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    /**
     * Set driver loading balancer strategy when multiple hosts set
     *
     * @see com.arangodb.ArangoDB.Builder#loadBalancingStrategy(LoadBalancingStrategy)
     */
    public void setLoadBalancingStrategy(LoadBalancingStrategy loadBalancingStrategy) {
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    public ArangoSSLConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    public ArangoSerdeConfig getSerde() {
        return serde;
    }

    @Override
    public String toString() {
        return getProperties().toString();
    }
}
