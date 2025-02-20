package view;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import view.settings.SettingsProxyPanel;

import javax.print.attribute.standard.JobHoldUntil;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.text.*;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/***
 * View: will handle the GUI components and layout.
 */
public class Vajra extends JFrame {
    private JPanel MainPane;

    // main tab
    private JTabbedPane vajraDashboard;
    private JPanel dashboardPane;

    // --> start: proxy
    private JTabbedPane vajraProxy;

    // intercept tab
    private JPanel interceptPane;
    private JTextPane interceptedRequest;


    private JButton interceptButton;
    private final String INTERCEPT_OFF = "Intercept off";
    private final String INTERCEPT_ON = "Intercept on";


    private JButton forwardButton;
    private JButton dropButton;

    // --> end: proxy


    // --> start: http history
    // getters for history table
    public JTable getHttpHistoryTable() {
        return httpHistoryTable;
    }

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    public JMenuItem getSendToRepeaterItem() {
        return sendToRepeaterItem;
    }


    // ------ history table  ------- //
    private JTable httpHistoryTable;
    // ------ history table  ------- //


    // ----- Request & Response Panes ----- //
    private JEditorPane httpHistoryRequestTextArea;
    private JEditorPane httpHistoryResponseTextArea;


    public JEditorPane getHttpHistoryRequestEditorPane() {
        return httpHistoryRequestTextArea;
    }

    public JEditorPane getHttpHistoryResponseEditorPane() {
        return httpHistoryResponseTextArea;
    }

    // ----- Request & Response Panes ----- //


    // create the popup menu
    // init has tobe done here for JPopupMenu else controller aint getting triggered.
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem sendToRepeaterItem = new JMenuItem("Send to Repeater");


    // --> end: http history


    // --> start repeater

    // getters for repeater empty panel
    public JPanel getRepeaterPanel() {
        return repeaterPanel;
    }

    public JTabbedPane getRepeaterTabs() {
        return repeaterTabs;
    }

    private JPanel repeaterPanel;
    private JTabbedPane repeaterTabs;
    private JButton sendButton;
    private JButton cancelButton;
    
    private JTextArea textArea1;
    private JTextArea textArea2;

    public JTextArea getTextArea1() {   
        return textArea1;
    }

    public JTextArea getTextArea2() {
        return textArea2;
    }   


    // --> end repeater


    // Menus
    JMenu mainMenu;
    JMenuItem mainMenuSettings;
    JMenuItem mainMenuExit;


    private SettingsProxyPanel settingsProxyPanelView;


    // init variables related to the HTTP History Table;
    String[] column = {"#", "Host", "Method", "URL", "Params", "Edited", "Status code", "Length", "MIME Type", "Extension", "title", "TLS", "IP", "Time", "Listener Port"};
    Object[][] data = {};
    DefaultTableModel tableModel = new DefaultTableModel(data, column) {
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Integer.class;
            }
            return String.class;
        }
    };

    // getters for the table model
    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    //constructor
    public Vajra() {


        setContentPane(MainPane);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 900);

        // Set the icon for the frame (this affects the taskbar icon)
        setIconImage(setTaskbarIcon());


        /***
         * Main Menu UI
         * setMenu() created to add items to the Menu.
         */
        JMenuBar menuBar = new JMenuBar();


        // menus
        mainMenu = new JMenu("Vajra");
        mainMenuSettings = new JMenuItem("Settings");
        mainMenuExit = new JMenuItem("Exit");

        setMenu(menuBar, mainMenu, mainMenuSettings);
        setMenu(menuBar, mainMenu, mainMenuExit);


        setJMenuBar(menuBar);


        /***
         * Interception Related UI Changes
         */


        /***
         * HTTP History UI Changes
         */


        getRequestResponseHistoryJPanel().setVisible(false);

        // add the menu item to popup menu.
        popupMenu.add(sendToRepeaterItem);

        // set HTTP history table model


        // this custom wrappers helps you paint a specific column in bold
        DefaultTableCellRenderer boldColumnRenderer = boldColumnRenderer();

        httpHistoryTable.getTableHeader().setPreferredSize(new Dimension(httpHistoryTable.getColumnModel().getTotalColumnWidth(), 25));
        httpHistoryTable.setModel(tableModel);

        // Add these optimizations
        httpHistoryTable.setDoubleBuffered(true);
        ((JComponent) httpHistoryTable.getParent()).setDoubleBuffered(true);
        
        // Reduce table update frequency
        tableModel.setRowCount(0);
        httpHistoryTable.setAutoCreateRowSorter(true);
        httpHistoryTable.getTableHeader().setReorderingAllowed(false);
        
        // Optional: Set a larger row height if needed
        httpHistoryTable.setRowHeight(25);

        // Get the current default renderer for Integer
        TableCellRenderer intRenderer = httpHistoryTable.getDefaultRenderer(Integer.class);

        // Check if it's a DefaultTableCellRenderer
        if (intRenderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer) intRenderer).setHorizontalAlignment(SwingConstants.LEFT);
        }

//        tableModel.fireTableDataChanged();
//        httpHistoryTable.setRowSorter(null);
//        sorter.sort();


        // resize the host column
        httpHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(250);

        // method column
        // resize the method column
        httpHistoryTable.getColumnModel().getColumn(2).setPreferredWidth(45);

        // resize the host column
        httpHistoryTable.getColumnModel().getColumn(3).setPreferredWidth(500);

        // params column
        // resize the params column
        httpHistoryTable.getColumnModel().getColumn(4).setPreferredWidth(40);


        // edited column
        // resize the edited column
        httpHistoryTable.getColumnModel().getColumn(5).setPreferredWidth(40);


        // TLS column
        // resize the TLS column a little smaller.
        httpHistoryTable.getColumnModel().getColumn(11).setPreferredWidth(30);


        httpHistoryTable.getColumnModel().getColumn(4).setCellRenderer(boldColumnRenderer);
        System.out.println("httpHistoryTable columns count: " + httpHistoryTable.getColumnModel().getColumnCount());


        // Create the menu bar
        JMenu requestMenu = new JMenu("\u25BC");

        JMenuItem originalItem = new JMenuItem("Original request");
        JMenuItem editedItem = new JMenuItem("Edited request");

        originalEditedRequest.add(requestMenu);

        requestMenu.add(originalItem);
        requestMenu.add(editedItem);


        requestJLabelPanel.setBackground(Color.WHITE);
        responseJLabelPanel.setBackground(Color.WHITE);


        // main window related UI
        setSize(1024, 900);
        setVisible(true);
    }


    // Getters for Interception, Forwarding, Dropping GUI components
    public JButton getInterceptButton() {
        return interceptButton;
    }

    public JButton getForwardButton() {
        return forwardButton;
    }

    public JButton getDropButton() {
        return dropButton;
    }

    public JTextPane getInterceptedRequestJTextPane() {
        return interceptedRequest;
    }

    public void setInterceptedRequest(String data) {
        interceptedRequest.setText(data);
    }

    // clears the JTextArea for Interception on/off area.
    public void clearInterceptedRequestArea() {
        interceptedRequest.setText("");
    }

    // getters - menu items
    public JMenuItem getSettingsMenuItemClick() {
        return mainMenuSettings;
    }

    // methods to update GUI for button state
    public void setInterceptButtonState(String text, Color background, Color foreground) {
        interceptButton.setText(text);
        interceptButton.setBackground(background);
        interceptButton.setForeground(foreground);
    }

    public void setMenu(JMenuBar menuBar, JMenu menu, JMenuItem menuItem) {
        menuBar.add(menu).add(menuItem);
    }


    // HTTP history
    private JPanel requestResponseHistoryJPanel;

    public JPanel getRequestResponseHistoryJPanel() {
        return requestResponseHistoryJPanel;
    }

    private JSplitPane mainHistorySplitPane;


    // http history panel  original / edited requests
    private JMenuBar originalEditedRequest;
    private JPanel requestJLabelPanel;
    private JPanel responseJLabelPanel;

    public JSplitPane getMainHistorySplitPane() {
        return mainHistorySplitPane;
    }


    public void detectEditingInterceptPane() {
        Document doc = interceptedRequest.getDocument();
        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                System.out.println("insertUpdate() called");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                System.out.println("removeUpdate() called");
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                System.out.println("changedUpdate() called");
            }
        });
    }


    public Image setTaskbarIcon() {
        // Load the custom icon from the classpath
        URL iconURL = getClass().getResource("/resources/vajra.png"); // Adjust the path as needed

        System.out.println("icon: " + iconURL);

        if (iconURL == null) {
            System.err.println("Resource not found: /resource/xx.png");
            return null;
        }

        ImageIcon icon = new ImageIcon(iconURL);
        Image image = icon.getImage();

        // Set the icon to the taskbar if supported
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            try {
                taskbar.setIconImage(image);
            } catch (UnsupportedOperationException e) {
                System.err.println("The OS does not support setting the taskbar icon.");
            } catch (SecurityException e) {
                System.err.println("Permission denied to set the taskbar icon.");
            }
        }

        return image;
    }


    // this is custom wrapper to paint a column bold
    private DefaultTableCellRenderer boldColumnRenderer() {
        DefaultTableCellRenderer boldColumnRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }
        };

        return boldColumnRenderer;
    }


    // will move this to controller
    // this is only for testing!
    private static void sendToRepeater(String requestDetails) {
        // Logic to send the request to the Repeater tab/component
        System.out.println("Sending to Repeater: " + requestDetails);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        MainPane = new JPanel();
        MainPane.setLayout(new CardLayout(0, 0));
        Font MainPaneFont = this.$$$getFont$$$("JetBrains Mono", Font.PLAIN, -1, MainPane.getFont());
        if (MainPaneFont != null) MainPane.setFont(MainPaneFont);
        MainPane.setMinimumSize(new Dimension(700, 700));
        MainPane.setPreferredSize(new Dimension(700, 700));
        vajraDashboard = new JTabbedPane();
        Font vajraDashboardFont = this.$$$getFont$$$("JetBrains Mono", Font.PLAIN, 12, vajraDashboard.getFont());
        if (vajraDashboardFont != null) vajraDashboard.setFont(vajraDashboardFont);
        MainPane.add(vajraDashboard, "Card1");
        dashboardPane = new JPanel();
        dashboardPane.setLayout(new CardLayout(0, 0));
        vajraDashboard.addTab("Dashboard", dashboardPane);
        vajraProxy = new JTabbedPane();
        vajraDashboard.addTab("Proxy", vajraProxy);
        interceptPane = new JPanel();
        interceptPane.setLayout(new GridBagLayout());
        vajraProxy.addTab("Intercept", interceptPane);
        interceptButton = new JButton();
        interceptButton.setAlignmentX(0.2f);
        interceptButton.setAlignmentY(0.9f);
        interceptButton.setMargin(new Insets(0, 0, 0, 0));
        interceptButton.setMaximumSize(new Dimension(150, 30));
        interceptButton.setMinimumSize(new Dimension(150, 30));
        interceptButton.setPreferredSize(new Dimension(150, 30));
        interceptButton.setText("Intercept off");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 2, 0, 2);
        interceptPane.add(interceptButton, gbc);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Request");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 0);
        interceptPane.add(label1, gbc);
        forwardButton = new JButton();
        forwardButton.setMaximumSize(new Dimension(150, 30));
        forwardButton.setMinimumSize(new Dimension(150, 30));
        forwardButton.setPreferredSize(new Dimension(150, 30));
        forwardButton.setText("Forward");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 2, 0, 2);
        interceptPane.add(forwardButton, gbc);
        dropButton = new JButton();
        dropButton.setMaximumSize(new Dimension(150, 30));
        dropButton.setMinimumSize(new Dimension(150, 30));
        dropButton.setPreferredSize(new Dimension(150, 30));
        dropButton.setText("Drop");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 2, 0, 2);
        interceptPane.add(dropButton, gbc);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 9;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        interceptPane.add(panel1, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(100, 100), null, 0, false));
        interceptedRequest = new JTextPane();
        Font interceptedRequestFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 14, interceptedRequest.getFont());
        if (interceptedRequestFont != null) interceptedRequest.setFont(interceptedRequestFont);
        scrollPane1.setViewportView(interceptedRequest);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        vajraProxy.addTab("History", panel2);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mainHistorySplitPane = new JSplitPane();
        mainHistorySplitPane.setDividerLocation(3);
        mainHistorySplitPane.setDividerSize(2);
        mainHistorySplitPane.setOrientation(0);
        mainHistorySplitPane.setResizeWeight(0.5);
        panel3.add(mainHistorySplitPane, BorderLayout.CENTER);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainHistorySplitPane.setLeftComponent(panel4);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel4.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        httpHistoryTable = new JTable();
        scrollPane2.setViewportView(httpHistoryTable);
        requestResponseHistoryJPanel = new JPanel();
        requestResponseHistoryJPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainHistorySplitPane.setRightComponent(requestResponseHistoryJPanel);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(402);
        splitPane1.setDividerSize(3);
        splitPane1.setResizeWeight(1.0);
        requestResponseHistoryJPanel.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        requestJLabelPanel = new JPanel();
        requestJLabelPanel.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(requestJLabelPanel);
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$("Consolas", Font.BOLD, 14, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("Request");
        requestJLabelPanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        requestJLabelPanel.add(scrollPane3, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        httpHistoryRequestTextArea = new JEditorPane();
        Font httpHistoryRequestTextAreaFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 14, httpHistoryRequestTextArea.getFont());
        if (httpHistoryRequestTextAreaFont != null) httpHistoryRequestTextArea.setFont(httpHistoryRequestTextAreaFont);
        scrollPane3.setViewportView(httpHistoryRequestTextArea);
        originalEditedRequest = new JMenuBar();
        originalEditedRequest.setVisible(false);
        requestJLabelPanel.add(originalEditedRequest, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        requestJLabelPanel.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        responseJLabelPanel = new JPanel();
        responseJLabelPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(responseJLabelPanel);
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$("Consolas", Font.BOLD, 14, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("Response");
        responseJLabelPanel.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JScrollPane scrollPane4 = new JScrollPane();
        responseJLabelPanel.add(scrollPane4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        httpHistoryResponseTextArea = new JEditorPane();
        Font httpHistoryResponseTextAreaFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 14, httpHistoryResponseTextArea.getFont());
        if (httpHistoryResponseTextAreaFont != null)
            httpHistoryResponseTextArea.setFont(httpHistoryResponseTextAreaFont);
        scrollPane4.setViewportView(httpHistoryResponseTextArea);
        repeaterPanel = new JPanel();
        repeaterPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        vajraDashboard.addTab("Repeater", repeaterPanel);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        repeaterPanel.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane5 = new JScrollPane();
        panel5.add(scrollPane5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        repeaterTabs = new JTabbedPane();
        Font repeaterTabsFont = this.$$$getFont$$$("JetBrains Mono", Font.PLAIN, 12, repeaterTabs.getFont());
        if (repeaterTabsFont != null) repeaterTabs.setFont(repeaterTabsFont);
        repeaterTabs.setTabLayoutPolicy(0);
        scrollPane5.setViewportView(repeaterTabs);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        repeaterTabs.addTab("Untitled", panel6);
        sendButton = new JButton();
        sendButton.setAlignmentY(0.5f);
        sendButton.setEnabled(true);
        sendButton.setIconTextGap(4);
        sendButton.setText("Send");
        panel6.add(sendButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel6.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setAlignmentY(0.5f);
        cancelButton.setEnabled(true);
        cancelButton.setIconTextGap(4);
        cancelButton.setText("Cancel");
        panel6.add(cancelButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane6 = new JScrollPane();
        repeaterPanel.add(scrollPane6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane2.setDividerSize(3);
        splitPane2.setResizeWeight(0.1);
        scrollPane6.setViewportView(splitPane2);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane2.setLeftComponent(panel7);
        final JScrollPane scrollPane7 = new JScrollPane();
        panel7.add(scrollPane7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea1 = new JTextArea();
        scrollPane7.setViewportView(textArea1);
        final JLabel label4 = new JLabel();
        label4.setText("Request");
        panel7.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane2.setRightComponent(panel8);
        final JScrollPane scrollPane8 = new JScrollPane();
        panel8.add(scrollPane8, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea2 = new JTextArea();
        textArea2.setEditable(false);
        scrollPane8.setViewportView(textArea2);
        final JLabel label5 = new JLabel();
        label5.setText("Response");
        panel8.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return MainPane;
    }

    // Custom EditorKit that supports wrapping
    static class WrapEditorKit extends StyledEditorKit {
        private final ViewFactory defaultFactory = new WrapColumnFactory();

        @Override
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
    }

    // A ViewFactory that creates wrap-capable views
    static class WrapColumnFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new WrapLabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new ParagraphView(elem) {
                            @Override
                            public void layout(int width, int height) {
                                super.layout(Integer.MAX_VALUE, height);
                            }

                            @Override
                            public float getMinimumSpan(int axis) {
                                return super.getPreferredSpan(axis);
                            }
                        };
                    case AbstractDocument.SectionElementName:
                        return new BoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }

    // A LabelView that can wrap text
    static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            if (axis == View.X_AXIS) {
                return 0;
            }
            return super.getMinimumSpan(axis);
        }
    }


}
