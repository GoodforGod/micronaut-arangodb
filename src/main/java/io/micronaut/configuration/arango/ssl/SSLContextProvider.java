package io.micronaut.configuration.arango.ssl;

import javax.net.ssl.SSLContext;

/**
 * Provides {@link SSLContext} for
 * {@link com.arangodb.ArangoDB.Builder#sslContext(SSLContext)}
 */
public interface SSLContextProvider {

    SSLContext get(ArangoSSLConfiguration configuration);
}
