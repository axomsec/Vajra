package controller.proxy;


import filters.InterceptingFilter;
import io.netty.handler.codec.http.*;
import model.RequestModel;
import view.Vajra;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;


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

    // Add a logger instance
    private static final Logger logger = Logger.getLogger(InterceptingFilter.class.getName());

    public boolean isForwarding() {
        return isFowarding;
    }

    private boolean isFowarding = false;

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

    public void setFowarding(boolean fowarding) {
        isFowarding = fowarding;
    }

    public void updateRequestText(String interceptedData){
        view.setInterceptedRequest(interceptedData);
    }

    public JTextPane getInterceptTextPane(){
        return view.getInterceptedRequestJTextPane();
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
                    handleForwardButton();
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

    private void handleForwardButton(){
        setFowarding(true);
        interceptLock.lock();
        try{
            interceptCondition.signal();
        }finally {
            interceptLock.unlock();
        }
        System.out.println("forward button clicked.");
    }

    private void handleDropButton(){
        System.out.println("drop button clicked.");
    }


    /**
     * @param queueStrings
     * This method clears the queued Request strings if the interception is toggled off.
     * Earlier, if the intercept was being toggled off, we were not clearing the intercepted requests strings
     * from the Blocking Queue. This rarely-raised bug where you turn the interception off and on again for forwarding
     * the previous requests that were being queued stayed at the top of the queue which then tries to forward
     * resulting a Bad request or some similar errors - even the UI was getting stalled with a previous request.
     * This method seems to have solved the bug, and I have tested this with several test cases but who the fuck
     * knows if this bug is actually resolved? it may occur, and you might have to debug yourself and shit yourself bro.
     */
    public void onInterceptionToggledOff(BlockingQueue<String> queueStrings) {
        interceptLock.lock();
        try {
            // Remove all old/stale requests from the queue
            queueStrings.clear();

            // Reset the text in your interception pane
            updateRequestText("");

            logger.log(Level.INFO, "Interception toggled OFF: cleared interceptedRequestStrings and reset UI.");
        } finally {
            interceptLock.unlock();
        }
    }


}
