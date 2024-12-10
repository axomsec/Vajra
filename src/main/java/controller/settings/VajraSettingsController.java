package controller.settings;

import com.sun.source.tree.TryTree;
import utils.CertificateUtil;
import view.settings.SettingsPage;
import view.Vajra;
import view.settings.SettingsProxyPanel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VajraSettingsController implements ActionListener {

    private final Vajra view;
    private final SettingsPage settingsPageView;




    // cert util
    CertificateUtil certificateUtil = new CertificateUtil();


    public VajraSettingsController(Vajra view, SettingsPage settingsPage) {
        this.view = view;
        this.settingsPageView = settingsPage;


        settingsPageView.getExportButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("mama mia");
                exportCACertificate();
            }
        });




        initController();


    }

    private void initController(){
        // assign action commands
        view.getSettingsMenuItemClick().setActionCommand("Settings");

        // attach a single listener to all buttons
        view.getSettingsMenuItemClick().addActionListener(this);

        // init to access right panel
        JPanel rightCardPanel = settingsPageView.getCardPanel();

        // init to access Jtree
        JTree settingsTree = settingsPageView.getTree();


        // tree selection listener
        settingsTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) settingsTree.getLastSelectedPathComponent();
            System.out.println("from controller: " + selectedNode);
            if(selectedNode != null){
                String selectedNodeName = selectedNode.toString();
                CardLayout cardLayout = (CardLayout) rightCardPanel.getLayout();
                System.out.println("showing..");

                try{
                    cardLayout.show(rightCardPanel, selectedNodeName);
                }catch (Exception err){
                    err.printStackTrace();
                }
            }
        });

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        System.out.println(command);

        switch (command){
            case "Settings":
                handleSettingsMenuButton();
                break;
        }

    }

    private void handleSettingsMenuButton() {
        System.out.println("settings menu button clicked.");
        settingsPageView.dispose();
        settingsPageView.setPreferredSize(new Dimension(700, 500));
        settingsPageView.setVisible(true);
    }

    private void handleProxyListenerExportCAButton(){
        System.out.println("Proxy Listener > Export CA button clicked!");
    }

    // Vajra generates a new certificate on launch.
    // if the certificate is not deleted manually , it stays in the Vajra's user directory
    // on clicking the export button - that internally generated certificate should be allowed to save whereever you need it.
    // Note: the certificate is generated only once, on first installation.
    private void exportCACertificate(){
        // Get the user's home directory dynamically
        String userHome = System.getProperty("user.home");

        // Create the path for the Vajra folder
        Path vajraFolder = Paths.get(userHome, "Vajra");

        // Define the file path inside the Vajra folder
        Path cacertFile = vajraFolder.resolve("cacert.cer");

        // Check if the certificate file exist
        if (Files.exists(cacertFile)) {
            System.out.println("cacert.cer files exists!");

            // Show a dialog box to save the `cacert.cer` file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save CA Certificate");
            fileChooser.setSelectedFile(new java.io.File("cacert.cer"));

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                // Get the file selected by the user
                java.io.File saveFile = fileChooser.getSelectedFile();

                try {
                    // read all bytes from the original cert file
                    byte[] originalCertificateData = Files.readAllBytes(cacertFile);

                    // Save the certificate to the selected file
                    Files.write(saveFile.toPath(), originalCertificateData);
                    System.out.println("Certificate saved to: " + saveFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(null, "Certificate file successfully exported to: " + saveFile.getAbsolutePath(),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to save the certificate: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("Save operation canceled by the user.");
            }


        } else {
            System.out.println(".cer file does not exists!");
        }

    }

}


