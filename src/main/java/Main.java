import filters.InterceptingFilter;

import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;
import net.lightbody.bmp.mitm.PemFileCertificateSource;

import org.littleshoot.proxy.HttpProxyServer;

import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import utils.CertificateUtil;


public class Main {

    public static void main(String[] args) throws Exception {


        CertificateUtil certificateUtil = new CertificateUtil();

        certificateUtil.generateCertificate("/home/jessi/code/cacert/new2/cacert.cer", "/home/jessi/code/cacert/new2/key.pem");

        PemFileCertificateSource pemFileCertificateSource = certificateUtil.CertificateSource("/home/jessi/code/cacert/new2/cacert.cer",
                "/home/jessi/code/cacert/new2/key.pem");


        // tell the MitmManager to use the root certificate we just generated
        ImpersonatingMitmManager mitmManager = ImpersonatingMitmManager.builder()
                .rootCertificateSource(pemFileCertificateSource)
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