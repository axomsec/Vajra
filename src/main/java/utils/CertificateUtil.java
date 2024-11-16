package utils;

import net.lightbody.bmp.mitm.CertificateInfo;
import net.lightbody.bmp.mitm.PemFileCertificateSource;
import net.lightbody.bmp.mitm.RootCertificateGenerator;

import java.io.File;
import java.util.Date;


/**
 * Certificate Utility
 * -------------------
 * This certificate helper class helps contributors to generate certificates.
 * One can modify the methods of this class to tamper with the Certificate information and more.
 * The basic moto of this class help devs to create and load certs separately.
 * One can use the generateCertificate() by satisfying parameters to generate a .cer file and a .pem file.
 * Then you can invoke CertificateSource method which can be then passed to:
 *                 .rootCertificateSource(CertificateSource)
 *                 .build();
 */

public class CertificateUtil {

    private static String PASSWORD = "changeit";


    // this method helps uses to generate .cer file and a .pem file
    // the output of this method then can be passed to the CertificateSource() method as parameters.
    public void generateCertificate(String rootCertFileName, String privateKeyFileName){
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

        // write the certificate file (.cer file)
        rootCertificateGenerator.saveRootCertificateAsPemFile(new File(rootCertFileName));

        // write the private key file (.pem file)
        rootCertificateGenerator.savePrivateKeyAsPemFile(new File(privateKeyFileName), PASSWORD);

    }

    // this method is specifically build to be passed to .rootCertificateSource(CertificateSource)
    public PemFileCertificateSource CertificateSource(String certFileName, String pKeyFileName){
       return new PemFileCertificateSource(new File(certFileName),
                new File(pKeyFileName), PASSWORD);
    }

}
