package filters;



import controller.proxy.VajraInterceptController;
import handlers.RequestInterceptorHandler;
import handlers.ResponseInterceptorHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class InterceptingFilter extends HttpFiltersSourceAdapter {

    private static VajraInterceptController vajraInterceptController;

     final Lock interceptLock;
     final Condition interceptCondition;


    private boolean interceptionStatus;

    private final LinkedBlockingDeque<String> interceptedRequests = new LinkedBlockingDeque<>(1);

    private static final LinkedBlockingDeque<String> interceptQueue = new LinkedBlockingDeque<>();
    private static String firstInterceptedRequest = null;


    int countQueue = 0;


    public InterceptingFilter(VajraInterceptController vajraInterceptController, Lock interceptLock, Condition interceptCondition) {
        this.vajraInterceptController = vajraInterceptController;
        this.interceptCondition = interceptCondition;
        this.interceptLock = interceptLock;
    }

    @Override
    public HttpFiltersAdapter filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        return new HttpFiltersAdapter(originalRequest) {


            @Override
            public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                if (httpObject instanceof FullHttpRequest) {

                    String interceptedData = RequestInterceptorHandler.handleRequest((FullHttpRequest) httpObject);
                    interceptQueue.add(interceptedData);

                    if(vajraInterceptController.getInterceptionStatus()){

                        countQueue += 1;
                        System.out.println("intercept_queue_count: " + countQueue);
                        System.out.println("intercept_queue_size: " + interceptQueue.size());
                        try {
                            System.out.println("intercept_queue_value: " + interceptQueue.takeFirst());
                        } catch (InterruptedException e) {

                            throw new RuntimeException(e);
                        }


                        interceptLock.lock();
                        try{

                            if(((FullHttpRequest) httpObject).method() == HttpMethod.CONNECT){
                                System.out.println("CONNECT request received, ignoring UI update.");
                                // this null return  is fucking important
                                // this null return sends the CONNECT requests unmodified for the browser to trigger a HTTP requests that being displayed in UI.
                                return null;
                            }

                            // this first if statement httpObject).method() != HttpMethod.CONNECT filters out the CONNECT requests as we don't want them in our JTextArea
                            // but the CONNECT requests are important as these are browser controls, and we don't want to interfere.
                            // the second if statement firstInterceptedRequest == null checks if the variable is null,
                            // if the variable is null we are making sure this is FIRST ever request AFTER enabling intercept to ON.
                            // then we are assigning the interceptedData variable to firstInterceptedRequest (don't worry filterRequest handles one request at a time)
                            // finally we update the UI with the data.
                            if(((FullHttpRequest) httpObject).method() != HttpMethod.CONNECT){
                                System.out.println("FILTERED_REQUEST_NON_CONNECT:" + interceptedData);
                                if(firstInterceptedRequest == null){
                                    firstInterceptedRequest = interceptedData;
                                    vajraInterceptController.updateRequestText(firstInterceptedRequest);
                                }
                            }

                            // wait until interception is toggled OFF.
                            while(vajraInterceptController.getInterceptionStatus()){
                                interceptCondition.await();
                            }

                            // nullifying firstInterceptedRequest variable again
                            // so that we can detect FIRST request again
                            // don't fucking touch this code. I have wasted my life writing this piece of crap.
                            firstInterceptedRequest = null;
                            System.out.println("Interception is turned OFF, forwarding requests.");

                        }catch (Exception e){
                            // restores the thread's interrupted state
                            Thread.currentThread().interrupt();
                            e.printStackTrace();
                        }finally {
                            System.out.println("unlock() called from InterceptingFilter class.");
                            interceptLock.unlock();
                        }

                    }

                }

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
