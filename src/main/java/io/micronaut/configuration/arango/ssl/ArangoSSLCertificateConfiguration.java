package io.micronaut.configuration.arango.ssl;

import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;

@Requires(property = ArangoSettings.PREFIX)
@ConfigurationProperties(ArangoSettings.PREFIX + ".ssl.certificate")
public class ArangoSSLCertificateConfiguration {

    /**
     * Is certificate provider enabled
     */
    private boolean enabled = false;

    /**
     * The name of the requested certificate type.
     *
     * @see CertificateFactory#getInstance(String)
     */
    private String type = "X.509";

    /**
     * The standard name of the requested trust management algorithm.
     *
     * @see TrustManagerFactory#getInstance(String)
     */
    private String algorithm = TrustManagerFactory.getDefaultAlgorithm();

    /**
     * Key storage
     * 
     * @see KeyStore#getInstance(String)
     */
    private String keyStore = KeyStore.getDefaultType();

    /**
     * Base64 encoded CA certificate.
     */
    private String value;

    /**
     * The alias name of certificate.
     */
    private String alias = "arangodb";

    /**
     * The standard name of the requested protocol.
     *
     * @see javax.net.ssl.SSLContext#getInstance(String)
     */
    private String protocol = "TLS";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return "[enabled=" + enabled +
                ", type=" + type +
                ", algorithm=" + algorithm +
                ", keyStore=" + keyStore +
                ", value=" + value +
                ", alias=" + alias +
                ", protocol=" + protocol + ']';
    }
}
