package view.settings;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class SettingsPage extends JFrame {

    private JPanel settingsMainPanel;
    private JTree tree;

    // ride side: card layout
    private JPanel cardPanel;

    // panels
    SettingsProxyPanel proxyPanel = new SettingsProxyPanel();
    SettingIntruderPanel intruderPanel = new SettingIntruderPanel();


    public SettingsPage() {

        setSize(new Dimension(1024, 800));


        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tools");
        root.add(new DefaultMutableTreeNode("Proxy"));
        root.add(new DefaultMutableTreeNode("Intruder"));


        // Apply custom styling to the JTree
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setBackgroundSelectionColor(new Color(173, 216, 230));
        renderer.setTextSelectionColor(Color.BLACK);
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);



        DefaultTreeModel model = new DefaultTreeModel(root);
        tree.setPreferredSize(new Dimension(200, 200 ));
        tree.setModel(model);

       cardPanel.add(proxyPanel.getProxyPanel(), "Proxy");
       cardPanel.add(intruderPanel.getIntruderPanel(), "Intruder");

       // display proxy panel selected by default
        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, "Proxy");

        // Select the Proxy node by default in the tree
        System.out.println(root.getFirstChild());
        TreePath proxyPath = new TreePath(root.getNextNode().getPath());
        tree.setSelectionPath(proxyPath); // This ensures the blue highlight
        tree.scrollPathToVisible(proxyPath); // Ensures it's visible if nested
        tree.repaint(); // Force the tree to repaint to update selection

        add(settingsMainPanel);
    }

    public JPanel getCardPanel() {
        return cardPanel;
    }

    public JTree getTree() {
        return tree;
    }

    public JButton getExportButton(){
        return proxyPanel.getExportCACertificateButton();
    }
}
