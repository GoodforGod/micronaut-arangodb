package io.micronaut.configuration.arango;

import com.arangodb.Protocol;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

import javax.inject.Inject;
import java.util.Properties;

/**
 * ArangoDB Sync configuration class.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(property = ArangoSettings.PREFIX)
@ConfigurationProperties(ArangoSettings.PREFIX)
public class ArangoConfiguration extends AbstractArangoConfiguration {

    private Protocol protocol;

    @Inject
    public ArangoConfiguration(ArangoSSLConfiguration sslConfiguration) {
        super(sslConfiguration);
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#useProtocol(Protocol)
     */
    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public Properties getProperties() {
        final Properties properties = super.getProperties();
        if (protocol != null) {
            properties.setProperty(ArangoProperties.PROTOCOL, String.valueOf(getProtocol()));
        }
        return properties;
    }
}
