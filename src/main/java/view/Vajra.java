package view;


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
//    private JTextArea interceptedRequest;
//    private JEditorPane interceptedRequest;
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

        // UI changes specific to JTextPane
        // line wrapping
//        StyledDocument document = interceptedRequest.getStyledDocument();
//        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
//        Style requestLineStyle = document.addStyle("requestLine", defaultStyle);
//        StyleConstants.setBold(requestLineStyle, true);
//        StyleConstants.setForeground(requestLineStyle, new Color(0x00, 0x88, 0x88)); // teal
//
//        // Header key style
//        Style headerKeyStyle = document.addStyle("headerKey", defaultStyle);
//        StyleConstants.setBold(headerKeyStyle, true);
//        StyleConstants.setForeground(headerKeyStyle, new Color(0x80, 0x00, 0x80)); // purple
//
//        // Header value style
//        Style headerValueStyle = document.addStyle("headerValue", defaultStyle);
//        StyleConstants.setForeground(headerValueStyle, new Color(0x00, 0x60, 0x00)); // dark green
//
//        // Body style
//        Style bodyStyle = document.addStyle("body", defaultStyle);
//        // Slightly darker gray for body
//        StyleConstants.setForeground(bodyStyle, new Color(0x33, 0x33, 0x33));


//        interceptedRequest.setWrao(true);
//        interceptedRequest.setWrapStyleWord(true);

//        interceptedRequest.setEditorKit(new WrapEditorKit());


        /***
         * HTTP History UI Changes
         */

        // add the menu item to popup menu.

        popupMenu.add(sendToRepeaterItem);



        String[] column = {"#", "Host", "Method", "URL", "Params", "Edited", "Status code", "Length", "MIME Type", "Extension", "title", "TLS", "IP", "Time", "Listener Port"};
        Object[][] data = {
                {1, "example.com", "GET", "http://example.com/home", "id=123", false, 200, 1024, "text/html", "html", "Home Page", true, "93.184.216.34", "2024-12-07 10:00:00", 8080},
                {2, "testsite.com", "POST", "https://testsite.com/login", "username=admin&password=1234", true, 302, 512, "application/json", "json", "Login Redirect", true, "192.168.1.1", "2024-12-07 10:05:00", 443},
                {3, "myapi.com", "PUT", "https://myapi.com/update", "item=45&value=on", true, 204, 0, "application/json", "json", "", true, "172.217.0.0", "2024-12-07 10:10:00", 80},
                {4, "secure-site.com", "DELETE", "https://secure-site.com/remove", "token=abcdef123456", false, 401, 256, "text/plain", "txt", "Unauthorized", true, "8.8.8.8", "2024-12-07 10:15:00", 8443},
                {5, "example.org", "GET", "http://example.org/contact", "", false, 404, 512, "text/html", "html", "Not Found", false, "93.184.216.35", "2024-12-07 10:20:00", 8080}
        };


        DefaultTableModel tableModel = new DefaultTableModel(data, column);


        httpHistoryTable.setModel(tableModel);


        setSize(1024, 900);
        setVisible(true);
    }

    // methods related to color scheme.
    private void insertRequestLine(StyledDocument document, String requestLine) {
        // requestLine might look like: "POST /api-2.0/auth/code-generation/login/4.0/ HTTP/2"
        Style requestLineStyle = document.getStyle("requestLine");
        try {
            document.insertString(document.getLength(), requestLine + "\n", requestLineStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // Getters for GUI components
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




}
