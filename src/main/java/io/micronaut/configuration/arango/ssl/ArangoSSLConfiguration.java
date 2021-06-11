package io.micronaut.configuration.arango.ssl;

import com.arangodb.internal.ArangoDefaults;
import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

import javax.inject.Inject;

@Requires(property = ArangoSettings.PREFIX)
@ConfigurationProperties(ArangoSettings.PREFIX + ".ssl")
public class ArangoSSLConfiguration {

    /**
     * If set to true SSL will be used when connecting to an ArangoDB server.
     * 
     * @see com.arangodb.ArangoDB.Builder#useSsl(Boolean)
     */
    private boolean enabled = ArangoDefaults.DEFAULT_USE_SSL;

    private final ArangoSSLCertificateConfiguration certificateConfiguration;

    @Inject
    public ArangoSSLConfiguration(ArangoSSLCertificateConfiguration certificateConfiguration) {
        this.certificateConfiguration = certificateConfiguration;
    }

    public ArangoSSLCertificateConfiguration getCertificateConfiguration() {
        return certificateConfiguration;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
