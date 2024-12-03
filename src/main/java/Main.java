import com.formdev.flatlaf.FlatIntelliJLaf;
import controller.proxy.VajraInterceptController;
import filters.InterceptingFilter;

import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;
import net.lightbody.bmp.mitm.PemFileCertificateSource;

import org.littleshoot.proxy.HttpProxyServer;

import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import utils.CertificateUtil;
import view.Vajra;

import javax.swing.*;


public class Main {

    public static void main(String[] args) throws Exception {



        CertificateUtil certificateUtil = new CertificateUtil();

//        certificateUtil.generateCertificate("C:\\code\\certs\\cacert.cer", "C:\\code\\certs\\key.pem");

        PemFileCertificateSource pemFileCertificateSource = certificateUtil.CertificateSource("C:\\code\\certs\\cacert.cer",
                "C:\\code\\certs\\key.pem");


        // tell the MitmManager to use the root certificate we just generated
        ImpersonatingMitmManager mitmManager = ImpersonatingMitmManager.builder()
                .rootCertificateSource(pemFileCertificateSource)
                .build();

        try {
            UIManager.setLookAndFeel( new FlatIntelliJLaf());

            // create view and controller
            Vajra view = new Vajra();
            VajraInterceptController controller = new VajraInterceptController(view);

            javax.swing.SwingUtilities.invokeLater(() -> {
                view.setVisible(true);
            });

            HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                    .withPort(8080)
                    .withManInTheMiddle(mitmManager)
                    .withFiltersSource(new InterceptingFilter(controller))
                    .withAllowLocalOnly(false)
                    .withAuthenticateSslClients(false)
                    .start();




        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }









        System.out.println("Proxy started on port 8080 with intercepting capabilities.");

    }
}