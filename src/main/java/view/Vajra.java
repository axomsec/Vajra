package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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
    private JTextArea interceptedRequest;

    private JButton interceptButton;
    private String INTERCEPT_OFF    = "Intercept off";
    private String INTERCEPT_ON     = "Intercept on";


    private JButton forwardButton;
    private JButton dropButton;
    private JTextArea httpHistoryRequestTextArea;
    private JTextArea httpHistoryResponseTextArea;


    // --> end: proxy


    // --> start: http history



    // ------ history table  ------- //
    private JTable httpHistoryTable;
    // ------ history table  ------- //


    // ----- Request & Response Panes ----- //




    // --> end: http history



    // Menus
    JMenu mainMenu;
    JMenuItem mainMenuSettings;
    JMenuItem mainMenuExit;


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

        // set menu bar icon
//        setVajraIcon(menuBar);

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

        // UI changes specific to  JTextArea
        // line wrapping
        interceptedRequest.setLineWrap(true);
        interceptedRequest.setWrapStyleWord(true);


        /***
         * HTTP History UI Changes
         */

//        String data[][] = {
//                {"1", "https://google.com", "POST", "/feed/", "x", "", "200", "7263", "text", "txt", "Example title", "x", "127.0.0.1", "09:55:23", "8080"}
//        };

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

//        Object[] rowData = {1, "haxx ", 25};
//        tableModel.addRow(rowData);


//        httpHistoryTable = new JTable(data, column);
//        httpHistoryScrollPane = new JScrollPane(httpHistoryTable);



        setPreferredSize(new Dimension(1024, 900));

        setVisible(true);
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

    public JTextArea getInterceptedRequest(){
        return interceptedRequest;
    }

    public void setInterceptedRequest(String data){
        interceptedRequest.setText(data);
    }

    // clears the JTextArea for Interception on/off area.
    public void clearInterceptedRequestArea(){
        interceptedRequest.setText("");
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

    public void setVajraIcon(JMenuBar menuBar){
        // set icon
        // Create a custom JLabel with an icon
        ImageIcon originalIcon = new ImageIcon("./src/main/java/resources/letter-v.png"); // Replace with your icon path
        Image resizedImage = originalIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        // Create a JLabel with the resized icon
        JLabel iconLabel = new JLabel(resizedIcon);
        iconLabel.setBorder(new EmptyBorder(5, 0, 5, 5));

        menuBar.add(iconLabel);
    }

    public Image setTaskbarIcon(){
        // Load the custom icon
        // Replace with your icon file path
        ImageIcon icon = new ImageIcon("./src/main/java/resources/letter-v.png");
        // Get the Image object from the icon
        Image image = icon.getImage();
        return image;
    }



//    // UI logic: intercept button on/off
//    private void interceptButtonAction(){
//        Color onColorButton = Color.decode("#01307a");
//        Color onTextColor = Color.decode("#ffffff");
//
//        Color offColorButton = Color.decode("#ffffff");
//        Color offTextColor = Color.decode("#000000");
//
//        if(interceptButton.getText().equals(INTERCEPT_OFF)){
//            interceptButton.setText(INTERCEPT_ON);
//            interceptButton.setBackground(onColorButton);
//            interceptButton.setForeground(onTextColor);
//        }else{
//            interceptButton.setText(INTERCEPT_OFF);
//            interceptButton.setBackground(offColorButton);
//            interceptButton.setForeground(offTextColor);
//        }
//    }
//
//    // actions
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        if(e.getSource() == interceptButton){
//            System.out.println("clicked --> " + interceptButton.getText());
//            interceptButtonAction();
//        }
//    }


}
