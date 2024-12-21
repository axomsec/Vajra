package filters;

import controller.history.VajraHistoryController;
import controller.proxy.VajraInterceptController;
import handlers.RequestInterceptorHandler;
import handlers.ResponseInterceptorHandler;
import httphighlighter.HttpHighLighter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import model.HttpHistoryEntryModel;
import org.bouncycastle.cert.ocsp.Req;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import view.Vajra;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterceptingFilter extends HttpFiltersSourceAdapter {

    // Thread-safe global ID counter
    // Auto-incrementing ID for HTTP history
    private static final AtomicInteger requestId = new AtomicInteger(1);


    private final Vajra view;
    private final VajraInterceptController vajraInterceptController;
    private final VajraHistoryController vajraHistoryController;

     final Lock interceptLock;
     final Condition interceptCondition;


    private final LinkedBlockingQueue<FullHttpRequest> interceptedRequestsQueue = new LinkedBlockingQueue<FullHttpRequest>();
    private final BlockingQueue<FullHttpRequest> requestQueue = new LinkedBlockingQueue<>();

    // Use a queue for ALL intercepted requests:
    private final BlockingQueue<String> interceptedRequestStrings = new LinkedBlockingQueue<>();


    // Use these maps to store requests and responses before adding them to history
    // Key: requestId
    // Value: HttpHistoryEntryModel associated with that request
    private final Map<Integer, HttpHistoryEntryModel> pendingEntries = new ConcurrentHashMap<>();

    // Keep track of request bodies and modifications separately if needed
    private final Map<Integer, FullHttpRequest> pendingFullRequests = new ConcurrentHashMap<>();



    // Log responses against the requestId as well
    // We'll set the status code (and possibly full response) in pendingEntries once available
    private final Map<Integer, Integer> pendingResponseStatus = new ConcurrentHashMap<>();




    // request handler
    private final RequestInterceptorHandler requestInterceptorHandler = new RequestInterceptorHandler();

    // Add a logger instance
    private static final Logger logger = Logger.getLogger(InterceptingFilter.class.getName());


    public InterceptingFilter(Vajra view, VajraInterceptController vajraInterceptController, VajraHistoryController vajraHistoryController, Lock interceptLock, Condition interceptCondition) {
        this.view = view;
        this.vajraInterceptController = vajraInterceptController;
        this.vajraHistoryController = vajraHistoryController;
        this.interceptCondition = interceptCondition;
        this.interceptLock = interceptLock;

    }


    @Override
    public HttpFiltersAdapter filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {


        // Check if the connection has TLS
        boolean isTls = ctx.pipeline().get(SslHandler.class) != null;
        System.out.println("fucking tls: " + isTls);

        return new HttpFiltersAdapter(originalRequest) {

            // We will store the modified request here after user edits.
            private String modifiedRequest = null;
            private int currentRequestId = -1; // Track this request's ID

            @Override
            public HttpResponse clientToProxyRequest(HttpObject httpObject) {

                if (httpObject instanceof FullHttpRequest) {

                    FullHttpRequest rqx = (FullHttpRequest) httpObject;

                    // Ignore CONNECT requests for UI update, just pass them through
                    if (rqx.method() == HttpMethod.CONNECT) {
                        return null;
                    }

                    // data for IP, time, and listener port
                    // Replace with actual client IP
                    String ip = vajraHistoryController.getClientIp(rqx.headers().get("Host"));
                    String time = java.time.LocalDateTime.now().toString();
                    int listenerPort = 8080;

                    // Use the thread-safe ID increment
                    currentRequestId = requestId.getAndIncrement();
                    System.out.println("Request Counter: id = " + currentRequestId);


                    HttpHistoryEntryModel entry = vajraHistoryController.createHttpHistoryEntry(
                            rqx,
                            currentRequestId,
                            ip,
                            time,
                            listenerPort,
                            isTls
                    );




                    RequestInterceptorHandler.InterceptedRequestData data  = RequestInterceptorHandler.handleRequest(rqx);
                    // You now have three separate parts:
                    String requestLine = data.getRequestLine();
                    LinkedHashMap<String, String> requestHeaders = data.getHeaders();
                    String requestBody = data.getBody();

                    // Later, you can reconstruct a formatted-style HTTP request string:
                    StringBuilder reconstructed = new StringBuilder();
                    reconstructed.append(requestLine).append("\n");
                    requestHeaders.forEach((key, value) -> reconstructed.append(key).append(": ").append(value).append("\n"));
                    reconstructed.append("\n");
                    if (!requestBody.isEmpty()) {
                        reconstructed.append(requestBody).append("\n");
                    }

                    String interceptedData = reconstructed.toString();
                    System.out.println("finalHttpRequestString: " + interceptedData);


                    // Store the entry and request; don't populate the table yet
                    pendingEntries.put(currentRequestId, entry);
                    pendingFullRequests.put(currentRequestId, rqx);

                    // this is reconstructed intercepted requests being stored
                    // this is for the HTTP history request/response being shown below when clicked into a table element.
                    vajraHistoryController.getReconstructedFullRequests().put(currentRequestId, interceptedData);

//                    if (!vajraInterceptController.getInterceptionStatus()) {
//                        addEntryToHistory(currentRequestId);
//                    }

                    // Add the entry to the controller
//                    vajraHistoryController.addHistoryEntry(entry);
//                    // populate the table with the data saved to the controller
//                    vajraHistoryController.populateTable(view.getTableModel());

                    if (vajraInterceptController.getInterceptionStatus()) {
                        interceptLock.lock();
                        try {


                            // Enqueue the new intercepted request
                            interceptedRequestStrings.add(interceptedData);
                            logger.log(Level.INFO , "Added to interceptedRequestStrings: {0}. Queue size: {1}",
                                    new Object[]{interceptedData, interceptedRequestStrings.size()});

                            // If the UI is currently "khali" or empty, show this request immediately
                            // Check if UI currently shows "khali" or nothing
                            String currentText = vajraInterceptController.getInterceptTextPane().getText();
                            if (currentText.equals("khali") || currentText.trim().isEmpty()) {
                                displayNextQueuedRequest();
                            }


                            // Wait while interception is ON and not forwarding
                            while (vajraInterceptController.getInterceptionStatus() && !vajraInterceptController.isForwarding()) {
                                interceptCondition.await();
                            }

                            // If user clicked Forward:
                            if (vajraInterceptController.isForwarding()) {
                                vajraInterceptController.setFowarding(false);



                                // WARNING: DON'T MOVE THIS POLLING STATEMENT FROM HERE
                                // REQUESTS HAS TO BE IMMEDIATELY POLLED, AFTER FORWARD,
                                // AS SOMETIMES IT DOES NOT REACH TO THE POLL OR SOME SHIT HAPPENS I DON'T KNOW
                                // IF POLLED LATER, THERE COULD BE UI STALLING ISSUES, WHICH IS VERY FUCKED UP.
                                // SO DON'T FUCKING TOUCH THIS CODE, I KNOW I WRITE DOG SHIT CODE, BUT DARE YOU.
                                // The request currently shown has been forwarded, remove it from the queue.
                                // The one on top of the queue is the one we are showing, so remove it now.
                                // remove the currently processed request
                                String removedData = interceptedRequestStrings.poll();
                                logger.log(Level.INFO, "Polled from interceptedRequestStrings: {0}. Queue size: {1}",
                                        new Object[]{removedData, interceptedRequestStrings.size()});


                                // Convert the edited request text from UI back into FullHttpRequest
                                modifiedRequest = vajraInterceptController.getInterceptTextPane().getText();

                                FullHttpRequest parsedData = requestInterceptorHandler.parseModifiedRequestToFullHttpRequest(modifiedRequest);
                                RequestInterceptorHandler.InterceptedRequestData parseModifiedData = RequestInterceptorHandler.handleRequest(parsedData);


                                // first line of segregation coming from parseModifiedData
                                String[] parsedRequestLineData = parseModifiedData.getRequestLine().split(" ");
                                String parsedMethod = parsedRequestLineData[0];
                                String parsedUri = parsedRequestLineData[1];
                                String parsedProtocolVersion = parsedRequestLineData[2];

                                rqx.setMethod(parsedData.method());
                                rqx.setUri(parsedData.uri());
                                rqx.setProtocolVersion(parsedData.protocolVersion());

                                System.out.println("parsedMethod " + parsedMethod);
                                System.out.println("parsedUri " + parsedUri);
                                System.out.println("parsedProtocolVersion " + parsedProtocolVersion);


                                // second line of segregation coming from parsedData
                                rqx.headers().clear();
                                rqx.headers().set(parsedData.headers());

                                // third line of segregation coming from parsedData
                                rqx.content().clear();
                                byte[] newContentBytes = parsedData.content().toString(StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);
                                rqx.content().writeBytes(newContentBytes);

                                // dynamically set the correct Content-Length header
                                rqx.headers().set(HttpHeaderNames.CONTENT_LENGTH, newContentBytes.length);

                                if(rqx.method() == HttpMethod.OPTIONS){
                                    System.out.println("Content length for the OPTIONS request: " + newContentBytes.length);
                                }



                                // Now display the next request if available
                                if (!interceptedRequestStrings.isEmpty()) {
                                    displayNextQueuedRequest();
                                } else {
                                    // No more requests in queue, show "khali"
                                    vajraInterceptController.updateRequestText("");
                                }

                                // Now that user forwarded, add entry to history
//                                addEntryToHistory(currentRequestId);


                                // Clear UI text or set it to something else if needed
//                                vajraInterceptController.updateRequestText("khali");

                                // Return null to continue pipeline
                                // We can substitute the modified request in proxyToServerRequest()
                                // but are not doing it currently, if faced any issues we will do it later
                                // or we will take it in the refactoring iteration.
                                return null;
                            }

                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            e.printStackTrace();
                        } finally {
                            interceptLock.unlock();
                        }
                    }
                }
                // If not intercepting or no modifications, return null to continue normal proxying
                return null;
            }


            @Override
            public HttpObject proxyToClientResponse(HttpObject httpObject) {
                if (httpObject instanceof FullHttpResponse && currentRequestId != -1) {
                    FullHttpResponse response = (FullHttpResponse) httpObject;
                    String reconstructedResponse = ResponseInterceptorHandler.handleResponse(response);

                    int statusCode = response.status().code();
                    pendingResponseStatus.put(currentRequestId, statusCode);
                    vajraHistoryController.getReconstructedFullResponses().put(currentRequestId, reconstructedResponse);

                    // Now, add the entry to history
                    addEntryToHistory(currentRequestId);

                    vajraHistoryController.displayRequestAndResponse(currentRequestId);
                }
                return httpObject;
            }
        };
    }

    @Override
    public int getMaximumRequestBufferSizeInBytes() {
        return 10 * 1024 * 1024;
    }

    @Override
    public int getMaximumResponseBufferSizeInBytes() {
        return 10 * 1024 * 1024;
    }

    private void addEntryToHistory(int requestId) {
        HttpHistoryEntryModel entry = pendingEntries.get(requestId);
        logger.log(Level.INFO, "pendingEntries: {0}", entry);
        if (entry == null) {
            logger.log(Level.WARNING, "No pending entry found for Request ID: {0}", requestId);
            return;
        }

        // Update entry with response code if available
        Integer status = pendingResponseStatus.get(requestId);
        if (status != null) {
            entry.setStatusCode(status);
        } else {
            logger.log(Level.WARNING, "No status code found for Request ID: {0}", requestId);
        }


        // Add the entry to the controller and populate the table
        vajraHistoryController.addHistoryEntry(entry);
        vajraHistoryController.populateTable(view.getTableModel());

        // Clean up maps
        pendingEntries.remove(requestId);
        pendingResponseStatus.remove(requestId);
//        reconstructedPendingFullResponses.remove(requestId);
    }


    public void debugProxyData(int queueCount, int queueSize, String queueValue){
        System.out.println("QUEUE_COUNT: " + queueCount);
        System.out.println("QUEUE_SIZE: " + queueSize);
        System.out.println("QUEUE_VALUE: " + queueValue);
    }


    /**
     * Display the next request from the queue in the JTextPane.
     */
    private void displayNextQueuedRequest() {
        String nextRequest = interceptedRequestStrings.peek();
        logger.log(Level.INFO, "Peeked interceptedRequestStrings: {0}. Queue size: {1}",
                new Object[]{nextRequest, interceptedRequestStrings.size()});

        JTextPane interceptPane = vajraInterceptController.getInterceptTextPane();

        // updating of content in the JTextPane happens here.
        SwingUtilities.invokeLater(() -> {
            if (nextRequest != null) {
                HttpHighLighter.createStyledHttpView(nextRequest, interceptPane);
                // Move caret to the top to prevent auto-scrolling
                interceptPane.setCaretPosition(0);
            } else {
                interceptedRequestStrings.clear();
                vajraInterceptController.updateRequestText("");
                System.out.println("Queue cleared as no requests are ongoing.");
            }
        });
    }

}
