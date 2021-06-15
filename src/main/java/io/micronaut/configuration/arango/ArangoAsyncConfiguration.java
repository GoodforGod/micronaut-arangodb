package io.micronaut.configuration.arango;

import io.micronaut.configuration.arango.ssl.ArangoSSLConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

import javax.inject.Inject;

/**
 * ArangoDB Async configuration class.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.2.2020
 */
@Requires(property = ArangoSettings.PREFIX)
@ConfigurationProperties(ArangoSettings.PREFIX)
public class ArangoAsyncConfiguration extends AbstractArangoConfiguration {

    @Inject
    public ArangoAsyncConfiguration(ArangoSSLConfiguration sslConfiguration) {
        super(sslConfiguration);
    }
}
