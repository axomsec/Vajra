package filters;

import handlers.RequestInterceptorHandler;
import handlers.ResponseInterceptorHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

public class InterceptingFilter extends HttpFiltersSourceAdapter {

    @Override
    public HttpFiltersAdapter filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        return new HttpFiltersAdapter(originalRequest) {

            @Override
            public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                if (httpObject instanceof FullHttpRequest) {
                    RequestInterceptorHandler.handleRequest((FullHttpRequest) httpObject);
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
