package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

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
    private JPasswordField passwordField1;
    // --> end: proxy


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

//        JScrollPane scrollPane = new JScrollPane(interceptedRequest);
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);



        // UI Changes specific to Intercept, Forward, Drop Buttons

        // Intercept button
//        interceptButton.setPreferredSize(new Dimension(100, 2));
//        interceptButton.setMinimumSize(new Dimension(2,2));
//        interceptButton.setMaximumSize(new Dimension(2,2));

        // Forward button
//        forwardButton.setPreferredSize(new Dimension(2, 2));
//        forwardButton.setMinimumSize(new Dimension(2,2));
//        forwardButton.setMaximumSize(new Dimension(2,2));


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

    public String getInterceptedRequest(){
        return interceptedRequest.getText();
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
