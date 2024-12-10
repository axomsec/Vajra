import com.formdev.flatlaf.FlatIntelliJLaf;
import controller.proxy.VajraInterceptController;
import controller.settings.VajraSettingsController;
import filters.InterceptingFilter;

import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;
import net.lightbody.bmp.mitm.PemFileCertificateSource;

import org.littleshoot.proxy.HttpProxyServer;

import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CertificateUtil;
import view.settings.SettingsPage;
import view.Vajra;
import view.settings.SettingsProxyPanel;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {


        final Lock interceptLock = new ReentrantLock();
        final Condition interceptCondition = interceptLock.newCondition();


        CertificateUtil certificateUtil = new CertificateUtil();


        // get the user's home directory dynamically
        String userHome = System.getProperty("user.home");

        // Create the path for the Vajra folder
        Path vajraFolder = Paths.get(userHome, "Vajra");

        // define the file paths inside the Vajra folder
        Path cacertFile = vajraFolder.resolve("cacert.cer");
        Path keyFile = vajraFolder.resolve("key.pem");

        // ensure Vajra folder exists
        try {
            if (!Files.exists(vajraFolder)) {
                Files.createDirectories(vajraFolder);
                System.out.println("Vajra folder created: " + vajraFolder);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Check if the certificate and key files exist
        if (!Files.exists(cacertFile) || !Files.exists(keyFile)) {
            System.out.println("Certificates not found. Generating new ones...");
            certificateUtil.generateCertificate(cacertFile.toString(), keyFile.toString());
        } else {
            System.out.println("Certificates already exist. Skipping generation.");
        }

        // Proceed with creating PemFileCertificateSource
        PemFileCertificateSource pemFileCertificateSource = certificateUtil.CertificateSource(
                cacertFile.toString(),
                keyFile.toString()
        );



        // tell the MitmManager to use the root certificate we just generated
        ImpersonatingMitmManager mitmManager = ImpersonatingMitmManager.builder()
                .rootCertificateSource(pemFileCertificateSource)
                .build();

        try {
            UIManager.setLookAndFeel( new FlatIntelliJLaf());

            // create view and controller for Main Window
            Vajra view = new Vajra();
            VajraInterceptController controller = new VajraInterceptController(view, interceptLock, interceptCondition);

            // create view and controller for settings Main Window
            SettingsPage settingsPageView = new SettingsPage();
            //  init view and controller for settings > proxy page
            SettingsProxyPanel settingsProxyPanelView = new SettingsProxyPanel();
            VajraSettingsController vajraSettingsController = new VajraSettingsController(view, settingsPageView);



            javax.swing.SwingUtilities.invokeLater(() -> {
                view.setVisible(true);
            });

            HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                    .withPort(8080)
                    .withManInTheMiddle(mitmManager)
                    .withFiltersSource(new InterceptingFilter(controller, interceptLock, interceptCondition))
                    .withAllowLocalOnly(false)
                    .withAuthenticateSslClients(false)
                    .start();


        } catch( Exception ex ) {
            System.out.println(ex);
            System.err.println( "Failed to initialize LaF" );
        }



        System.out.println("Proxy started on port 8080 with intercepting capabilities.");

    }
}