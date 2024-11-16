import filters.InterceptingFilter;

import net.lightbody.bmp.mitm.CertificateInfo;
import net.lightbody.bmp.mitm.RootCertificateGenerator;
import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.MitmManager;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Date;

public class Main {

    public static void main(String[] args) throws Exception {


        CertificateInfo certificateInfo = new CertificateInfo()
                .commonName("Vajra Certificate")
                .organization("Vajra Private Ltd")
                .organizationalUnit("IT Security")
                .email("rony@axomsec.com")
                .locality("Guwahati")
                .state("Assam")
                .countryCode("IN")
                .notBefore(new Date())
                .notAfter(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)
        );



        // create a dynamic CA root certificate generator using default settings (2048-bit RSA keys)
        RootCertificateGenerator rootCertificateGenerator = RootCertificateGenerator.builder()
                .certificateInfo(certificateInfo)
                .build();

        // save the dynamically-generated CA root certificate for installation in a browser
        rootCertificateGenerator.saveRootCertificateAsPemFile(new File("/tmp/my-ca.cer"));



        System.out.println(rootCertificateGenerator.encodeRootCertificateAsPem());


        // tell the MitmManager to use the root certificate we just generated
        ImpersonatingMitmManager mitmManager = ImpersonatingMitmManager.builder()
                .rootCertificateSource(rootCertificateGenerator)
                .build();


        HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                        .withPort(8080)
                        .withManInTheMiddle(mitmManager)
                        .withFiltersSource(new InterceptingFilter())
                        .withAllowLocalOnly(false)
                        .withAuthenticateSslClients(false)
                        .start();



        System.out.println("Proxy started on port 8080 with intercepting capabilities.");

    }
}
