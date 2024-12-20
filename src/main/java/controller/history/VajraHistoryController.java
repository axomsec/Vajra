package controller.history;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import model.HttpHistoryEntryModel;
import view.Vajra;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;


public class VajraHistoryController implements ActionListener {
    

    private final Vajra view;

    private final JTable httpHistoryTable;
    private final JMenuItem sendToRepeaterItem;

    private final LinkedList<HttpHistoryEntryModel> historyList;


    // variables related to the response status code
    int responseCode;



    public VajraHistoryController(Vajra view, JTable httpHistoryTable, JMenuItem sendToRepeaterItem) {
        this.view = view;
        this.httpHistoryTable = httpHistoryTable;
        this.sendToRepeaterItem = sendToRepeaterItem;
        this.historyList = new LinkedList<>();

        System.out.println("calling from VajraHistoryController()");


        System.out.println("historyList ==  " + historyList.toString());

//        populateTable(view.getTableModel());

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


    // Add a single history entry
    public void addHistoryEntry(HttpHistoryEntryModel entry) {
        historyList.add(entry);
    }

    // Add multiple history entries at once
    public void addAllHistory(LinkedList<HttpHistoryEntryModel> entries) {
        historyList.addAll(entries);
    }

    // Populate table model with all history data
    public void populateTable(DefaultTableModel tableModel) {
        // Clear existing data
        // important step, else repeated data populates.
        tableModel.setRowCount(0);

        // iterate through the entries.
        for (HttpHistoryEntryModel entry : historyList) {
            tableModel.addRow(entry.toTableRow());
        }
    }

    // Clear all history from both LinkedList and table model
    public void clearHistory(DefaultTableModel tableModel) {
        historyList.clear();
        tableModel.setRowCount(0);
    }

    // Get the LinkedList (optional, for other operations)
    public LinkedList<HttpHistoryEntryModel> getHistoryList() {
        return historyList;
    }


    public HttpHistoryEntryModel createHttpHistoryEntry(FullHttpRequest rqx, int id, String ip, String time,
                                                        int listenerPort, boolean tls) {
        // Extract HTTP method
        String method = rqx.method().name();

        // Extract URL: Combine the Host header and URI
        HttpHeaders headers = rqx.headers();

        // the tls flag is coming from InterceptingFiler
        // context: ctx
        // if the flag is true its TLS enabled else plain HTTP.
        String host = (tls ? "https://" : "http://") + headers.get("Host", "unknown-host");
        String uri = rqx.uri();
        String url = uri;
        int statusCode = getStatusCode();




        // Extract parameters (query string)
        String params = "";
        if (uri.contains("?")) {

            params = "✓";
            //            params = uri.substring(uri.indexOf("?") + 1);
        }

        // Extract body length
        int contentLength = rqx.content().readableBytes();

        // MIME Type: Look for Content-Type header
        String mimeType = headers.get("Content-Type", "unknown");

        // TLS: Check if the request is over HTTPS (context-dependent)
//        boolean tls = url.startsWith("https");

        // Build and return the HttpHistoryEntry object
        return new HttpHistoryEntryModel(
                id,                      // Unique ID
                host,                    // Host
                method,                  // HTTP method
                url,                     // Full URL
                params,                  // Parameters
                false,                   // Edited (default: false)
                statusCode,              // Status code (to be updated when response is received)
                contentLength,           // Request content length
                mimeType,                // MIME Type
                "",                      // Extension (can be parsed from URL if needed)
                "",                      // Title (can be added later if applicable)
                tls,                     // TLS
                ip,                      // IP Address
                time,                    // Request time
                listenerPort             // Listener port
        );
    }


    /***
     *
     * @param input
     * @return
     *
     * This method is used to get the IP address of a hostname which is coming from the clientToProxy
     * and this data could be then feed to the HTTP History table.
     */
    public String getClientIp(String input){
        String resolvedIp = "";
        try {
            InetAddress inetAddress = InetAddress.getByName(input);
            resolvedIp = inetAddress.getHostAddress();
            return resolvedIp;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return resolvedIp;
    }

    public void setStatusCode(int code){
        responseCode = code;
    }

    public int getStatusCode(){
        return responseCode;
    }

}
