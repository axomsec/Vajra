package controller.proxy;

import filters.InterceptingFilter;
import model.RequestModel;
import view.Vajra;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


/***
 * Controllers handles the logic for button actions, keeping UI and business logic separate.
 */

public class VajraInterceptController implements ActionListener{

    private final Vajra view;
    private RequestModel model;

    final Lock interceptLock;
    final Condition interceptCondition;

    private static final String INTERCEPT_OFF = "Intercept off";
    private static final String INTERCEPT_ON = "Intercept on";

    private boolean isIntercepting = false;

    private final LinkedBlockingDeque<String> interceptedRequests = new LinkedBlockingDeque<>(1);


    public VajraInterceptController(Vajra view, Lock interceptLock, Condition interceptCondition) {
        this.view = view;
        this.interceptLock = interceptLock;
        this.interceptCondition = interceptCondition;

        this.model = new RequestModel();


        // does initialization for listeners
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

    public boolean getInterceptionStatus(){
        return isIntercepting;
    }

    public void setInterceptionStatus(boolean status){
        isIntercepting = status;
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
            this.isIntercepting = true;

            // requests here will be held, the logic is being implemented in InterceptingFilter class.
            // all requests will be waiting because of the interceptCondition.await();
            System.out.println("Interception ON: Requests will be held.");

        }else{
            view.setInterceptButtonState(INTERCEPT_OFF, Color.decode("#ffffff"), Color.decode("#000000"));
            this.isIntercepting = false;
            view.clearInterceptedRequestArea();

            System.out.println("Interception OFF: Signaling all threads to proceed.");

            // thread locking has to be done before signalling, that is why this is being locked.
            interceptLock.lock();
            try {
                // Notify waiting threads
                interceptCondition.signalAll();
            } finally {
                // unlocking immediately after signalling.
                // this is a very important step.
                interceptLock.unlock();
            }
        }
    }

    private void handleFowardButton(){
        System.out.println("forward button clicked.");
    }

    private void handleDropButton(){
        System.out.println("drop button clicked.");
    }



}
