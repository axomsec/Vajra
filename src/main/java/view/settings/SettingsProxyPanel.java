package view.settings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsProxyPanel extends JFrame{
    private JPanel proxyPanel;
    private JButton addButton;
    private JButton editButton;
    private JButton removeButton;
    private JTable proxyListenerTable;



    private JButton exportCACertificateButton;


    public SettingsProxyPanel() {

        /**
          UI Changes for Proxy Listener
         */
        String[] column     = {"Running", "Interface"};
        Object[][] data       = {{true, "127.0.0.1"}};


        // Create DefaultTableModel
        DefaultTableModel tableModel = new DefaultTableModel(data, column) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Define the column class for checkbox
                if (columnIndex == 0) {
                    // First column will display checkboxes
                    return Boolean.class;
                }

                // Other columns as Strings
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Make only the checkbox column editable
                return column == 0;
            }
        };
        proxyListenerTable.setModel(tableModel);


        System.out.println("print button ref from view: " + exportCACertificateButton);

    }

    public JPanel getProxyPanel() {
        return proxyPanel;
    }

    public JButton getExportCACertificateButton() {
        return exportCACertificateButton;
    }


}



