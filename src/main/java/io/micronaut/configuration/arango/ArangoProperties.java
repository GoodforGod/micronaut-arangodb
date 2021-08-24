package io.micronaut.configuration.arango;

/**
 * Properties for ArangoDB driver
 *
 * @see com.arangodb.internal.InternalArangoDBBuilder
 *
 * @author Anton Kurako (GoodforGod)
 * @since 11.6.2021
 */
public interface ArangoProperties {

    String HOSTS = "arangodb.hosts";
    String HOST = "arangodb.host";
    String PORT = "arangodb.port";
    String USER = "arangodb.user";
    String PASSWORD = "arangodb.password";
    String TIMEOUT = "arangodb.timeout";
    String PROTOCOL = "arangodb.protocol";
    String USE_SSL = "arangodb.usessl";
    String CHUNK_SIZE = "arangodb.chunksize";
    String MAX_CONNECTIONS = "arangodb.connections.max";
    String CONNECTION_TTL = "arangodb.connections.ttl";
    String KEEP_ALIVE_INTERVAL = "arangodb.connections.keepAlive.interval";
    String ACQUIRE_HOST_LIST = "arangodb.acquireHostList";
    String ACQUIRE_HOST_LIST_INTERVAL = "arangodb.acquireHostList.interval";
    String LOAD_BALANCING_STRATEGY = "arangodb.loadBalancingStrategy";
}
