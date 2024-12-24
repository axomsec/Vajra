package controller.history;

import filters.InterceptingFilter;
import httphighlighter.HttpHighLighter;
import httphighlighter.WrappingEditorKit;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import model.HttpHistoryEntryModel;
import view.Vajra;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


public class VajraHistoryController implements ActionListener {
    

    private final Vajra view;


    private final JTable httpHistoryTable;
    private final JMenuItem sendToRepeaterItem;

    private final LinkedList<HttpHistoryEntryModel> historyList;

    private HttpHighLighter httpHighLighter;

    // variables related to the response status code
    int responseCode;




    // Log Full Fledged Requests against the requestId
    private final Map<Integer, String> reconstructedFullRequests = new ConcurrentHashMap<>();

    // Log Full Fledged responses against the requestId
    // We'll fetch the responses for the HTTP History request/response window.
    private final Map<Integer, String> reconstructedFullResponses = new ConcurrentHashMap<>();


    // Add a logger instance
    private static final Logger logger = Logger.getLogger(VajraHistoryController.class.getName());


    public VajraHistoryController(Vajra view, JTable httpHistoryTable, JMenuItem sendToRepeaterItem) {
        this.view = view;
        this.httpHistoryTable = httpHistoryTable;
        this.sendToRepeaterItem = sendToRepeaterItem;
        this.historyList = new LinkedList<>();

        System.out.println("calling from VajraHistoryController()");


        System.out.println("historyList ==  " + historyList.toString());


        javax.swing.SwingUtilities.invokeLater(() ->{
            // Selection listener
            ListSelectionModel selectionModel = httpHistoryTable.getSelectionModel();
            selectionModel.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    // check if the event is adjusting to prevent multiple triggers
                    if(!e.getValueIsAdjusting()){
                        int selectedRow = httpHistoryTable.getSelectedRow();
                        if(selectedRow != -1){
                            // assuming the first column is the request id
                            int requestId = (Integer) httpHistoryTable.getValueAt(selectedRow, 0);
                            logger.log(Level.INFO, "requestId (from httpHistoryTable) = " + requestId);

                            // Retrieve the details panel (right component of the main split pane)
                            JPanel requestResponsePanel = view.getRequestResponseHistoryJPanel();
                            JSplitPane mainSplitPane = view.getMainHistorySplitPane();
                            if (!requestResponsePanel.isVisible()) {
                                // Make the details panel visible
                                requestResponsePanel.setVisible(true);

                                // Optionally, set the split pane's divider location
                                if (mainSplitPane != null) {
                                    // Set divider location to 30% for table and 70% for details
                                    mainSplitPane.setDividerLocation(0.3);
                                }

                                // Revalidate and repaint the main frame to ensure layout updates
                                view.getContentPane().revalidate();
                                view.getContentPane().repaint();

                            }


                            // Call the method to display request and response
                            displayRequestAndResponse(requestId);


                            // call the method to display request and response
//                        displayRequestAndResponse(requestId);
                        }
                    }

                }
            });

        });



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


    public void populateTable(DefaultTableModel tableModel) {
//        javax.swing.SwingUtilities.invokeLater(() -> {
            // Preserve the currently selected Request ID
            int selectedRow = httpHistoryTable.getSelectedRow();
            Integer selectedRequestId = null;
            if (selectedRow != -1) {
                selectedRequestId = (Integer) httpHistoryTable.getValueAt(selectedRow, 0); // Assuming column 0 is Request ID
            }

            // Clear existing data
            tableModel.setRowCount(0);

            // Repopulate the table with all entries
            for (HttpHistoryEntryModel entry : historyList) {
                tableModel.addRow(entry.toTableRow());
            }

            // Restore the selection if a Request ID was previously selected
            if (selectedRequestId != null) {
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    Integer requestId = (Integer) tableModel.getValueAt(row, 0); // Assuming column 0 is Request ID
                    if (selectedRequestId.equals(requestId)) {
                        httpHistoryTable.setRowSelectionInterval(row, row);
                        break;
                    }
                }
            }
//        });
    }


    // Populate table model with all history data
//    public void populateTable(DefaultTableModel tableModel) {
//
//       javax.swing.SwingUtilities.invokeLater(() -> {
//           // Clear existing data
//           // important step, else repeated data populates.
//           tableModel.setRowCount(0);
//
//           // iterate through the entries.
//           for (HttpHistoryEntryModel entry : historyList) {
//               updateTablePreservingSelection(() -> {
//                   tableModel.addRow(entry.toTableRow());
//               });
//
//           }
//       });
//
//    }

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


    /**
     * Displays the request and response details in the UI based on the requestId.
     *
     * @param requestId The unique identifier for the HTTP request.
     */
    public void displayRequestAndResponse(int requestId){
        // retrieve the reconstructed request and response strings
        String request      = reconstructedFullRequests.get(requestId);
        String response     = reconstructedFullResponses.get(requestId);

        javax.swing.SwingUtilities.invokeLater(() -> {


            // request
            view.getHttpHistoryRequestEditorPane().setEditorKit(new WrappingEditorKit());
            view.getHttpHistoryRequestEditorPane().setContentType("text/plain");
            view.getHttpHistoryRequestEditorPane().setEditable(false);


            // response
            view.getHttpHistoryResponseEditorPane().setEditorKit(new WrappingEditorKit());
            view.getHttpHistoryResponseEditorPane().setContentType("text/plain");
            view.getHttpHistoryResponseEditorPane().setEditable(false);



            // accessing the UI components from VajHistoryController to update the Panels to be done here
            view.getHttpHistoryRequestEditorPane().setText(request);
            view.getHttpHistoryRequestEditorPane().setCaretPosition(0);

            view.getHttpHistoryResponseEditorPane().setText(response);
            view.getHttpHistoryResponseEditorPane().setCaretPosition(0);

            //HttpHighLighter.highlightHttp(request, view.getHttpHistoryRequestTextArea());
            //HttpHighLighter.highlightHttp(response, view.getHttpHistoryResponseTextArea());
        });



        logger.log(Level.INFO, "displayRequestAndResponse: request  = " + request);
        logger.log(Level.INFO, "displayRequestAndResponse: response = " + response);


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


    /**
     * @return
     * This Getter is for giving InterceptingFilter access to the Map for storing requests.
     */
    public Map<Integer, String> getReconstructedFullRequests() {
        return reconstructedFullRequests;
    }

    /**
     * @return
     * This Getter is for giving InterceptingFilter access to the Map for storing responses.
     */
    public Map<Integer, String> getReconstructedFullResponses() {
        return reconstructedFullResponses;
    }

}
