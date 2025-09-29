package goblinbob.mobends.core.util;

import java.io.IOException;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

/**
 * Utility class for handling SSL certificate validation bypassing.
 * Applies SSL bypassing directly to HTTPS connections to avoid SSL certificate issues.
 */
public class SSLHelper {

    /**
     * Creates a TrustManager that accepts all certificates without validation.
     */
    private static TrustManager[] createTrustAllManager() {
        return new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                    X509Certificate[] certs,
                    String authType
                ) {
                    // Accept all client certificates
                }

                public void checkServerTrusted(
                    X509Certificate[] certs,
                    String authType
                ) {
                    // Accept all server certificates
                }
            },
        };
    }

    /**
     * Creates a HostnameVerifier that accepts all hostnames.
     */
    private static HostnameVerifier createTrustAllHostnameVerifier() {
        return new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true; // Accept all hostnames
            }
        };
    }

    /**
     * Creates an SSLContext that trusts all certificates.
     */
    private static SSLContext createTrustAllSSLContext()
        throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
            null,
            createTrustAllManager(),
            new java.security.SecureRandom()
        );
        return sslContext;
    }

    /**
     * Configures an HttpsURLConnection to bypass SSL certificate validation.
     */
    public static void bypassSSLForConnection(HttpsURLConnection connection) {
        try {
            SSLContext sslContext = createTrustAllSSLContext();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setHostnameVerifier(createTrustAllHostnameVerifier());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            System.err.println(
                "Failed to configure SSL bypass for connection: " +
                    e.getMessage()
            );
            e.printStackTrace();
        }
    }

    /**
     * Opens a URL connection with SSL certificate bypass applied for HTTPS URLs.
     * For HTTP URLs, returns a normal connection.
     */
    public static URLConnection openConnectionWithSSLBypass(java.net.URL url)
        throws java.io.IOException {
        URLConnection connection = url.openConnection();

        // Apply SSL bypass for HTTPS connections
        if (connection instanceof HttpsURLConnection) {
            bypassSSLForConnection((HttpsURLConnection) connection);
        }

        return connection;
    }
}
