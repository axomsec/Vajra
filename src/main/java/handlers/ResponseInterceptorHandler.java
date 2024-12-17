package handlers;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import utils.LoggerUtil;

public class ResponseInterceptorHandler {

    public static void handleResponse(FullHttpResponse response) {
        LoggerUtil.log("Intercepted Response:");
        LoggerUtil.log("Status: " + response.getStatus());
        LoggerUtil.log("Headers: " + response.headers().toString());

        // Print body of the response
        String responseBody = response.content().toString(CharsetUtil.UTF_8);
        LoggerUtil.log("Body: " + responseBody);
    }
}
