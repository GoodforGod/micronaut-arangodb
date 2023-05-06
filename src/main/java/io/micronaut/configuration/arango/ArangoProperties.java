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

    public static final String PREFIX = "arangodb";

    public static final String HOSTS = PREFIX + ".hosts";
    public static final String PROTOCOL = PREFIX + ".protocol";
    public static final String USER = PREFIX + ".user";
    public static final String PASSWORD = PREFIX + ".password";
    public static final String JWT = PREFIX + ".jwt";
    public static final String TIMEOUT = PREFIX + ".timeout";
    public static final String USE_SSL = PREFIX + ".useSsl";
    public static final String VERIFY_HOST = PREFIX + ".verifyHost";
    public static final String CHUNK_SIZE = PREFIX + ".chunkSize";
    public static final String MAX_CONNECTIONS = PREFIX + ".maxConnections";
    public static final String CONNECTION_TTL = PREFIX + ".connectionTtl";
    public static final String KEEP_ALIVE_INTERVAL = PREFIX + ".keepAliveInterval";
    public static final String ACQUIRE_HOST_LIST = PREFIX + ".acquireHostList";
    public static final String ACQUIRE_HOST_LIST_INTERVAL = PREFIX + ".acquireHostListInterval";
    public static final String LOAD_BALANCING_STRATEGY = PREFIX + ".loadBalancingStrategy";
    public static final String RESPONSE_QUEUE_TIME_SAMPLES = PREFIX + ".responseQueueTimeSamples";
}
