package controller.history;

import view.Vajra;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VajraHistoryController implements ActionListener {


    private final Vajra view;

    private final JTable httpHistoryTable;
    private final JMenuItem sendToRepeaterItem;


    public VajraHistoryController(Vajra view, JTable httpHistoryTable, JMenuItem sendToRepeaterItem) {
        this.view = view;
        this.httpHistoryTable = httpHistoryTable;
        this.sendToRepeaterItem = sendToRepeaterItem;

        System.out.println("calling from VajraHistoryController()");



        // add the action listener
        sendToRepeaterItem.addActionListener(this);

        httpHistoryTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if(e.isPopupTrigger()){
                    showMenu(e);
                    System.out.println("inside mousePressed");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.isPopupTrigger()){
                    showMenu(e);
                    System.out.println("inside mouseReleased");
                }
            }

            private void showMenu(MouseEvent e){
                int row = httpHistoryTable.rowAtPoint(e.getPoint());
                httpHistoryTable.setRowSelectionInterval(row, row);
                view.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }

        });

    }


    @Override
    public void actionPerformed(ActionEvent e) {

        int selectedRow = httpHistoryTable.getSelectedRow();
        System.out.println(selectedRow);

        // change the second param for getting access to other details on the table.
        System.out.println(httpHistoryTable.getValueAt(selectedRow, 1 ));
    }

    private void handleSendToRepeaterMenuClick(){
        System.out.println("inside handleSendToRepeaterMenuClick");
    }
}
