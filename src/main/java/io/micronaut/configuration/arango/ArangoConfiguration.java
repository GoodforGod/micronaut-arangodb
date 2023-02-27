package io.micronaut.configuration.arango;

import com.arangodb.Protocol;
import io.micronaut.configuration.arango.ssl.ArangoSSLConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Inject;
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
    private Integer responseQueueTimeSamples;

    @Inject
    public ArangoConfiguration(ArangoSSLConfiguration sslConfiguration) {
        super(sslConfiguration);
    }

    /**
     * @see com.arangodb.ArangoDB.Builder#useProtocol(Protocol)
     * @return protocol value
     */
    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Integer getResponseQueueTimeSamples() {
        return responseQueueTimeSamples;
    }

    /**
     * @param responseQueueTimeSamples Setting the amount of samples kept for queue time metrics
     */
    public void setResponseQueueTimeSamples(Integer responseQueueTimeSamples) {
        this.responseQueueTimeSamples = responseQueueTimeSamples;
    }

    @Override
    public Properties getProperties() {
        final Properties properties = super.getProperties();
        if (protocol != null) {
            properties.setProperty(ArangoProperties.PROTOCOL, String.valueOf(getProtocol()));
        }
        if (responseQueueTimeSamples != null) {
            properties.setProperty(ArangoProperties.RESPONSE_QUEUE_TIME_SAMPLES, String.valueOf(getResponseQueueTimeSamples()));
        }

        return properties;
    }
}
