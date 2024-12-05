package filters;



import controller.proxy.VajraInterceptController;
import handlers.RequestInterceptorHandler;
import handlers.ResponseInterceptorHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InterceptingFilter extends HttpFiltersSourceAdapter {

    private final VajraInterceptController vajraInterceptController;

     final Lock interceptLock;
     final Condition interceptCondition;


    private boolean interceptionStatus;


//    private final Lock interceptLock = new ReentrantLock();
//    private final Condition interceptCondition = interceptLock.newCondition();

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

                    if(vajraInterceptController.getInterceptionStatus()){
                        interceptLock.lock();
                        try{
                            // passing the intercepted data to the controller.
                            vajraInterceptController.updateRequestText(interceptedData);

                            // wait until interception is toggled OFF.
                            while(vajraInterceptController.getInterceptionStatus()){
                                interceptCondition.await();
                            }

                            System.out.println("Interception is turned OFF, forwarding requests.");

                        }catch (Exception e){

                            // restores the thread's interrupted state
                            Thread.currentThread().interrupt();
                            e.printStackTrace();

                        }finally {
                            interceptLock.unlock();
                        }
                    }

                }
                return null;  // Let the request continue unmodified
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

}
