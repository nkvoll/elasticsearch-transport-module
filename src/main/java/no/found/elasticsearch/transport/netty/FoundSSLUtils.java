/*
 * Copyright (c) 2013, Found AS.
 * See LICENSE for details.
 */

package no.found.elasticsearch.transport.netty;

import no.found.elasticsearch.transport.netty.ssl.FoundSSLHandler;
import org.elasticsearch.common.collect.Sets;

import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * Collection of SSL-related utils.
 */
public final class FoundSSLUtils {
    private FoundSSLUtils() {}

    public static FoundSSLHandler getSSLHandler(boolean unsafeAllowSelfSigned, InetSocketAddress inetSocketAddress) throws NoSuchAlgorithmException {
        String hostString = inetSocketAddress.getHostString();

        SSLEngine engine = createSslEngine(unsafeAllowSelfSigned, inetSocketAddress, hostString);

        Set supported = Sets.newHashSet(engine.getSupportedCipherSuites());

        Set preferred = Sets.newHashSet("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                "TLS_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
                "TLS_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
                "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
                "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
                "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
                "SSL_RSA_WITH_RC4_128_SHA",
                "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
                "TLS_ECDH_RSA_WITH_RC4_128_SHA",
                "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
                "SSL_RSA_WITH_RC4_128_SHA",
                "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
                "SSL_RSA_WITH_RC4_128_MD5",
                "TLS_ECDH_RSA_WITH_RC4_128_SHA",
                "TLS_EMPTY_RENEGOTIATION_INFO_SCSV",
                "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
                "SSL_RSA_WITH_RC4_128_MD5",
                "TLS_EMPTY_RENEGOTIATION_INFO_SCSV");

        Set<String> usable = Sets.intersection(preferred, supported);

        String[] usableArray = usable.toArray(new String[usable.size()]);

        if(usableArray.length > 0) {
            engine.setEnabledCipherSuites(usableArray);
        }
        engine.setUseClientMode(true);

        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm("HTTPS");
        engine.setSSLParameters(sslParams);

        engine.setEnableSessionCreation(true);
        engine.setNeedClientAuth(false);

        FoundSSLHandler handler = new FoundSSLHandler(engine);
        handler.setIssueHandshake(false);
        handler.setCloseOnSSLException(false);
        handler.setEnableRenegotiation(true);

        return handler;
    }

    public static SSLEngine createSslEngine(boolean unsafeAllowSelfSigned, InetSocketAddress inetSocketAddress, final String hostString) throws NoSuchAlgorithmException {
        if(unsafeAllowSelfSigned) {
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {

                }
                @Override
                public void checkServerTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException {

                    for(X509Certificate cert: chain) {
                        String dn = cert.getSubjectX500Principal().getName();

                        if(dn.contains("CN="+hostString)) return;

                        String[] hostParts = hostString.split("\\.", 2);
                        if(hostParts.length > 1) {
                            String lastHostPart = hostParts[1];

                            if(dn.contains("CN=*."+lastHostPart)) return;
                        }
                    }

                    throw new CertificateException("No name matching [" + hostString + "] found");
                }
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance( "SSL" );
            try {
                sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            return sslContext.createSSLEngine(hostString, inetSocketAddress.getPort());
        } else {
            return SSLContext.getDefault().createSSLEngine(hostString, inetSocketAddress.getPort());
        }
    }
}
