package view;


import controller.history.VajraHistoryController;
import view.settings.SettingsProxyPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;

/***
 * View: will handle the GUI components and layout.
 */
public class Vajra extends JFrame  {
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
    private String INTERCEPT_OFF    = "Intercept off";
    private String INTERCEPT_ON     = "Intercept on";


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
    private JTextArea httpHistoryRequestTextArea;
    private JTextArea httpHistoryResponseTextArea;

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

    // --> end repeater



    // Menus
    JMenu mainMenu;
    JMenuItem mainMenuSettings;
    JMenuItem mainMenuExit;


    private SettingsProxyPanel settingsProxyPanelView;



    // init variables related to the HTTP History Table;
    String[] column = {"#", "Host", "Method", "URL", "Params", "Edited", "Status code", "Length", "MIME Type", "Extension", "title", "TLS", "IP", "Time", "Listener Port"};
    Object[][] data = {};
    DefaultTableModel tableModel = new DefaultTableModel(data, column);
    // getters for the table model
    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    //constructor
    public Vajra()  {

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

        // add the menu item to popup menu.
        popupMenu.add(sendToRepeaterItem);

        // set HTTP history table model
        httpHistoryTable.setModel(tableModel);


        // main window related UI
        setSize(1024, 900);
        setVisible(true);
    }


    // Getters for Interception, Forwarding, Dropping GUI components
    public JButton getInterceptButton(){
        return interceptButton;
    }

    public JButton getForwardButton(){
        return forwardButton;
    }

    public JButton getDropButton(){
        return dropButton;
    }

    public JTextPane getInterceptedRequestJTextPane(){
        return interceptedRequest;
    }

    public void setInterceptedRequest(String data){
        interceptedRequest.setText(data);
    }

    // clears the JTextArea for Interception on/off area.
    public void clearInterceptedRequestArea(){
        interceptedRequest.setText("");
    }

    // getters - menu items
    public JMenuItem getSettingsMenuItemClick(){
        return mainMenuSettings;
    }

    // methods to update GUI for button state
    public void setInterceptButtonState(String text, Color background, Color foreground){
        interceptButton.setText(text);
        interceptButton.setBackground(background);
        interceptButton.setForeground(foreground);
    }

    public void setMenu(JMenuBar menuBar, JMenu menu, JMenuItem menuItem){
        menuBar.add(menu).add(menuItem);
    }

    public Image setTaskbarIcon(){
        // Load the custom icon
        // Replace with your icon file path
        ImageIcon icon = new ImageIcon("./src/main/java/resources/letter-v.png");
        // Get the Image object from the icon
        Image image = icon.getImage();
        return image;
    }

    // will move this to controller
    // this is only for testing!
    private static void sendToRepeater(String requestDetails) {
        // Logic to send the request to the Repeater tab/component
        System.out.println("Sending to Repeater: " + requestDetails);
    }

    // Custom EditorKit that supports wrapping
    static class WrapEditorKit extends StyledEditorKit {
        private ViewFactory defaultFactory = new WrapColumnFactory();

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
