package filters;



import controller.proxy.VajraInterceptController;
import handlers.RequestInterceptorHandler;
import handlers.ResponseInterceptorHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.bouncycastle.cert.ocsp.Req;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

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

    private final LinkedBlockingDeque<String> interceptedRequests = new LinkedBlockingDeque<>(1);

    private static final LinkedBlockingDeque<String> interceptQueue = new LinkedBlockingDeque<>();
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

                            // If this is the first intercepted request after enabling interception,
                            // update UI with its data.
                            if (firstInterceptedRequest == null && rqx.method() != HttpMethod.CONNECT) {
                                firstInterceptedRequest = interceptedData;
                                vajraInterceptController.updateRequestText(firstInterceptedRequest);
                            }

                            // Wait while interception is ON and not forwarding
                            while (vajraInterceptController.getInterceptionStatus() && !vajraInterceptController.isForwarding()) {
                                interceptCondition.await();
                            }

                            // Reset the firstInterceptedRequest for the next request
                            firstInterceptedRequest = null;

                            // If user clicked Forward:
                            if (vajraInterceptController.isForwarding()) {
                                vajraInterceptController.setFowarding(false);

                                // Convert the edited request text from UI back into FullHttpRequest
                                modifiedRequest = vajraInterceptController.getInterceptTextArea().getText();

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
                                rqx.content().writeBytes(parsedData.content());

                                // Clear UI text or set it to something else if needed
                                vajraInterceptController.updateRequestText("khali");

                                if (vajraInterceptController.getInterceptTextArea().getText().equalsIgnoreCase("khali")) {
                                    // Return the original request unmodified (if you have it).
                                    return null;
                                }


                                // Return null to continue pipeline
                                // We can substitute the modified request in proxyToServerRequest()
                                // but are not doing it currently, if faced any issues we will do it later
                                // or we will take it in the refactoring iteration.

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

}
