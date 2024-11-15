package handlers;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.CharsetUtil;
import utils.LoggerUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestInterceptorHandler {

    public static void handleRequest(FullHttpRequest request) {
        StringBuilder interceptedData = new StringBuilder();

        // Format request line
        interceptedData.append(request.method())
                .append(" ")
                .append(request.uri())
                .append(" ")
                .append(request.protocolVersion())
                .append("\n");

        // Format headers
        for (Map.Entry<String, String> header : request.headers()) {
            interceptedData.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\n");
        }

        // Add a separating newline between headers and body
        interceptedData.append("\n");

        // Print body if it's a POST request or contains data
        if (request.method().name().equals("POST") || request.content().isReadable()) {
            String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
            String body = request.content().toString(CharsetUtil.UTF_8);

            // Handle URL-encoded form data
            if ("application/x-www-form-urlencoded".equals(contentType)) {
                Map<String, String> postParams = decodeFormData(body);
                interceptedData.append("Body (Form Data):\n");
                postParams.forEach((key, value) ->
                        interceptedData.append(key).append("=").append(value).append("\n"));
            }
            // Handle JSON data
            else if ("application/json".equals(contentType)) {
                interceptedData.append("Body (JSON):\n")
                        .append(body)
                        .append("\n");
            }
            // Default: show raw body
            else {
                interceptedData.append("Body:\n")
                        .append(body)
                        .append("\n");
            }
        }

        // Log the formatted intercepted data
        LoggerUtil.log("Intercepted Request:\n" + interceptedData.toString());
    }


    // Decode URL-encoded form data into a key-value map
    private static Map<String, String> decodeFormData(String body) {
        Map<String, String> formData = new HashMap<>();
        try {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                    formData.put(key, value);
                }
            }
        } catch (Exception e) {
            LoggerUtil.log("Error decoding form data: " + e.getMessage());
        }
        return formData;
    }

}
