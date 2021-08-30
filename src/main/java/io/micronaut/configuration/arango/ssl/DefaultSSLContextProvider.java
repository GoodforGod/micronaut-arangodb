package io.micronaut.configuration.arango.ssl;

import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.exceptions.ConfigurationException;
import jakarta.inject.Singleton;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;

/**
 * Builds {@link SSLContext} for
 * {@link com.arangodb.ArangoDB.Builder#sslContext(SSLContext)} using
 * {@link ArangoSSLCertificateConfiguration}
 */
@Secondary
@Singleton
public class DefaultSSLContextProvider implements SSLContextProvider {

    @Override
    public SSLContext get(ArangoSSLConfiguration configuration) {
        final ArangoSSLCertificateConfiguration certConfig = configuration.getCertificateConfiguration();
        if (!certConfig.isEnabled())
            return null;

        try {
            final byte[] certificateAsBytes = Base64.getDecoder().decode(certConfig.getValue());
            try (final InputStream is = new ByteArrayInputStream(certificateAsBytes)) {
                final CertificateFactory factory = CertificateFactory.getInstance(certConfig.getType());
                final Certificate certificate = factory.generateCertificate(is);

                final KeyStore keyStore = KeyStore.getInstance(certConfig.getKeyStore());
                keyStore.load(null);
                keyStore.setCertificateEntry(certConfig.getAlias(), certificate);

                final TrustManagerFactory managerFactory = TrustManagerFactory.getInstance(certConfig.getAlgorithm());
                managerFactory.init(keyStore);

                final SSLContext sslContext = SSLContext.getInstance(certConfig.getProtocol());
                sslContext.init(null, managerFactory.getTrustManagers(), null);
                return sslContext;
            }
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }
    }
}
