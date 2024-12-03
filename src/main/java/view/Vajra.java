package view;

import javax.swing.*;
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

    // --> end: proxy


    //constructor
    public Vajra()  {

        setContentPane(MainPane);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 900);

//        interceptButton.addActionListener(this);

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


    // methods to update GUI
    public void setInterceptButtonState(String text, Color background, Color foreground){
        interceptButton.setText(text);
        interceptButton.setBackground(background);
        interceptButton.setForeground(foreground);
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
