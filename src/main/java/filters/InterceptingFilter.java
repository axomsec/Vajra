package filters;



import controller.proxy.VajraInterceptController;
import handlers.RequestInterceptorHandler;
import handlers.ResponseInterceptorHandler;
import httphighlighter.HttpHighLighter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.bouncycastle.cert.ocsp.Req;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import view.Vajra;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class InterceptingFilter extends HttpFiltersSourceAdapter {

    private final VajraInterceptController vajraInterceptController;

     final Lock interceptLock;
     final Condition interceptCondition;


    private boolean interceptionStatus;

    private final LinkedBlockingQueue<FullHttpRequest> interceptedRequestsQueue = new LinkedBlockingQueue<FullHttpRequest>();
    private final BlockingQueue<FullHttpRequest> requestQueue = new LinkedBlockingQueue<>();

    // Use a queue for ALL intercepted requests:
    private final BlockingQueue<String> interceptedRequestStrings = new LinkedBlockingQueue<>();



    private static String firstInterceptedRequest = null;

    private final RequestInterceptorHandler requestInterceptorHandler = new RequestInterceptorHandler();

    int countQueue = 0;


    public InterceptingFilter(VajraInterceptController vajraInterceptController, Lock interceptLock, Condition interceptCondition) {
        this.vajraInterceptController = vajraInterceptController;
        this.interceptCondition = interceptCondition;
        this.interceptLock = interceptLock;

    }

    @Override
    public HttpFiltersAdapter filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        return new HttpFiltersAdapter(originalRequest) {

            // We will store the modified request here after user edits.
            private String modifiedRequest = null;

            @Override
            public HttpResponse clientToProxyRequest(HttpObject httpObject) {

                if (httpObject instanceof FullHttpRequest) {

                    FullHttpRequest rqx = (FullHttpRequest) httpObject;
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


                    if (vajraInterceptController.getInterceptionStatus()) {


                        interceptLock.lock();
                        try {
                            // Ignore CONNECT requests for UI update, just pass them through
                            if (rqx.method() == HttpMethod.CONNECT) {
                                return null;
                            }





                            // Enqueue the new intercepted request
                            interceptedRequestStrings.add(interceptedData);

                            // If the UI is currently "khali" or empty, show this request immediately
                            // Check if UI currently shows "khali" or nothing
                            String currentText = vajraInterceptController.getInterceptTextPane().getText();
                            if (currentText.equals("khali") || currentText.trim().isEmpty()) {
                                displayNextQueuedRequest();
                            }

                            // If this is the first intercepted request after enabling interception,
                            // update UI with its data.
//                            if (firstInterceptedRequest == null && rqx.method() != HttpMethod.CONNECT) {
//
//                                firstInterceptedRequest = interceptedData;
//
//                                // Get the JTextPane from your controller (you need a method for this).
//                                JTextPane interceptPane = vajraInterceptController.getInterceptTextPane();
//
//                                // Apply syntax highlighting
//                                HttpHighLighter.createStyledHttpView(firstInterceptedRequest, interceptPane);
//
////                                vajraInterceptController.updateRequestText(firstInterceptedRequest);
//                            }

                            // Wait while interception is ON and not forwarding
                            while (vajraInterceptController.getInterceptionStatus() && !vajraInterceptController.isForwarding()) {
                                interceptCondition.await();
                            }

                            // Reset the firstInterceptedRequest for the next request
//                            firstInterceptedRequest = null;


                            // If user clicked Forward:
                            if (vajraInterceptController.isForwarding()) {
                                vajraInterceptController.setFowarding(false);

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


                                // The request currently shown has been forwarded, remove it from the queue.
                                // The one on top of the queue is the one we are showing, so remove it now.
                                // remove the currently processed request
                                interceptedRequestStrings.poll();


                                // Now display the next request if available
                                if (!interceptedRequestStrings.isEmpty()) {
                                    displayNextQueuedRequest();
                                } else {
                                    // No more requests in queue, show "khali"
                                    vajraInterceptController.updateRequestText("");
                                }


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
                if (httpObject instanceof FullHttpResponse) {
                    ResponseInterceptorHandler.handleResponse((FullHttpResponse) httpObject);
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

    public void debugProxyData(int queueCount, int queueSize, String queueValue){
        System.out.println("QUEUE_COUNT: " + queueCount);
        System.out.println("QUEUE_SIZE: " + queueSize);
        System.out.println("QUEUE_VALUE: " + queueValue);
    }

    private static void enqueueRequest(String uri, String content, BlockingQueue<FullHttpRequest> requestQueue, FullHttpRequest incomingReq) throws InterruptedException {
        incomingReq.retain();
        requestQueue.put(incomingReq);
        System.out.println("[Main] Enqueued request: URI=" + uri);
    }

    /**
     * Display the next request from the queue in the JTextPane.
     */
    private void displayNextQueuedRequest() {
        String nextRequest = interceptedRequestStrings.peek();
        JTextPane interceptPane = vajraInterceptController.getInterceptTextPane();

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
