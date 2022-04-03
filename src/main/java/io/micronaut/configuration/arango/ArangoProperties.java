package io.micronaut.configuration.arango;

/**
 * Properties for ArangoDB driver
 *
 * @see com.arangodb.internal.InternalArangoDBBuilder
 * @author Anton Kurako (GoodforGod)
 * @since 11.6.2021
 */
public final class ArangoProperties {

    private ArangoProperties() {}

    public static final String HOSTS = "arangodb.hosts";
    public static final String HOST = "arangodb.host";
    public static final String PORT = "arangodb.port";
    public static final String USER = "arangodb.user";
    public static final String PASSWORD = "arangodb.password";
    public static final String TIMEOUT = "arangodb.timeout";
    public static final String PROTOCOL = "arangodb.protocol";
    public static final String USE_SSL = "arangodb.usessl";
    public static final String CHUNK_SIZE = "arangodb.chunksize";
    public static final String MAX_CONNECTIONS = "arangodb.connections.max";
    public static final String CONNECTION_TTL = "arangodb.connections.ttl";
    public static final String KEEP_ALIVE_INTERVAL = "arangodb.connections.keepAlive.interval";
    public static final String ACQUIRE_HOST_LIST = "arangodb.acquireHostList";
    public static final String ACQUIRE_HOST_LIST_INTERVAL = "arangodb.acquireHostList.interval";
    public static final String LOAD_BALANCING_STRATEGY = "arangodb.loadBalancingStrategy";
}
