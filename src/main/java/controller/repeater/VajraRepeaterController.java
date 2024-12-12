package controller.repeater;

import view.Vajra;

import javax.swing.*;
import java.awt.event.*;

public class VajraRepeaterController  {

    private final Vajra view;

    int tabCount = 0;

    public VajraRepeaterController(Vajra view) {
        this.view = view;

        JTabbedPane tabs = view.getRepeaterTabs();


        tabCount = view.getRepeaterTabs().getTabCount();
        System.out.println("tab count: " + tabCount);

        // handles editing of tabs
        tabs.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // detect double click on a tab
                if(e.getClickCount() == 2){
                    int tabIndex = tabs.getSelectedIndex();
                    if(tabIndex >= 0){
                        String currentTitle = tabs.getTitleAt(tabIndex);

                        // create text field for editing.
                        JTextField textField = new JTextField(currentTitle);
                        textField.setBorder(BorderFactory.createEmptyBorder());
                        textField.setOpaque(false);

                        // add the text field to the tab header.
                        tabs.setTabComponentAt(tabIndex, textField);

                        // focus the text field and select the current text
                        textField.requestFocusInWindow();
                        textField.selectAll();

                        // handle when the user finishes editing
                        textField.addActionListener(actionEvent -> {
                            String newTitle = textField.getText();
                            tabs.setTitleAt(tabIndex, newTitle);
                            tabs.setTabComponentAt(tabIndex, null);
                        });

                        // handle focus loss to save changes on the text field
                        textField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                String newTitle = textField.getText();
                                tabs.setTitleAt(tabIndex, newTitle);
                                tabs.setTabComponentAt(tabIndex, null);
                            }
                        });

                    }
                }
            }
        });


    }


}
