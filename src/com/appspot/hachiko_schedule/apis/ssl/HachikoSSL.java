package com.appspot.hachiko_schedule.apis.ssl;

import android.content.Context;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class HachikoSSL {
    public static SSLSocketFactory getSocketFactory(Context context) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(
                    context.getResources().openRawResource(R.raw.hachiko));
            HachikoLogger.debug("ca=" + ((X509Certificate) certificate).getSubjectDN());

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);

            TrustManagerFactory managerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            managerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, managerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {

        }
        return null;
    }
}