package controller.proxy;

import view.Vajra;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/***
 * Controllers handles the logic for button actions, keeping UI and business logic separate.
 */

public class VajraInterceptController implements ActionListener{

    private final Vajra view;
    private static final String INTERCEPT_OFF = "Intercept off";
    private static final String INTERCEPT_ON = "Intercept on";

    public VajraInterceptController(Vajra view) {
        this.view = view;
        initController();
    }

    private void initController(){
        // assign action commands
        view.getInterceptButton().setActionCommand("forward");
        view.getInterceptButton().setActionCommand("drop");
        view.getInterceptButton().setActionCommand("intercept");

        // attach a single listener to all buttons
        view.getInterceptButton().addActionListener(this);
        view.getForwardButton().addActionListener(this);
        view.getDropButton().addActionListener(this);
    }

    public void updateRequestText(String interceptedData){
        view.setInterceptedRequest(interceptedData);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            System.out.println(command);

            switch (command){
                case "intercept":
                    handleInterceptButton();
                    break;
                case "Forward":
                    System.out.println("will call handleForwardButton()");
                    break;
                case "Drop":
                    System.out.println("will call handleDropButton()");
                    break;
                default:
                    System.out.println("Unknown action command: " + command);
            }
    }

    private void handleInterceptButton(){
        String currentText = view.getInterceptButton().getText();
        if(INTERCEPT_OFF.equals(currentText)){
            view.setInterceptButtonState(INTERCEPT_ON, Color.decode("#01307a"), Color.decode("#ffffff"));
        }else{
            view.setInterceptButtonState(INTERCEPT_OFF, Color.decode("#ffffff"), Color.decode("#000000"));
        }
    }

    private void handleFowardButton(){
        System.out.println("forward button clicked.");
    }

    private void handleDropButton(){
        System.out.println("drop button clicked.");
    }


}
