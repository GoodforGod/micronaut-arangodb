package io.micronaut.configuration.arango;

import com.arangodb.internal.ArangoDefaults;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;

public class ArangoSSLConfiguration {

    /**
     * If set to true SSL will be used when connecting to an ArangoDB server.
     * @see com.arangodb.ArangoDB.Builder#useSsl(Boolean)
     */
    private boolean useSsl = ArangoDefaults.DEFAULT_USE_SSL;

    /**
     * The name of the requested certificate type.
     * @see CertificateFactory#getInstance(String)
     */
    private String certificateType = "X.509";
    /**
     * The standard name of the requested trust management algorithm.
     * @see TrustManagerFactory#getInstance(String)
     */
    private String algorithmName = TrustManagerFactory.getDefaultAlgorithm();

    /**
     * Base64 encoded CA certificate.
     */
    private String certificate;

    /**
     * The alias name of certificate.
     */
    private String alias = "caCert";

    /**
     * The standard name of the requested protocol.
     * @see javax.net.ssl.SSLContext#getInstance(String)
     */
    private String protocol = "TLS";

    public SSLContext getSSLContext() {
        try {
            final byte[] certificateAsBytes = Base64.getDecoder().decode(getCertificate());
            try (final InputStream is = new ByteArrayInputStream(certificateAsBytes)) {
                final CertificateFactory factory = CertificateFactory.getInstance(getCertificateType());
                final Certificate certificate = factory.generateCertificate(is);

                final TrustManagerFactory managerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null);
                keyStore.setCertificateEntry(getAlias(), certificate);

                managerFactory.init(keyStore);

                final SSLContext sslContext = SSLContext.getInstance(getProtocol());
                sslContext.init(null, managerFactory.getTrustManagers(), null);
                return sslContext;
            }
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    public boolean getUseSsl() {
        return useSsl;
    }

    public void setUseSsl(Boolean useSsl) {
        this.useSsl = useSsl;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
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
}
