package handlers;

import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import utils.LoggerUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestInterceptorHandler {

    public static String handleRequest(FullHttpRequest request) {
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
        return interceptedData.toString();
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

    public static FullHttpRequest parseModifiedRequest(String modifiedRequest) {
        try {
            String[] sections = modifiedRequest.split("\n\n", 2); // Split into headers and body
            String[] lines = sections[0].split("\n");

            // Parse the request line
            String[] requestLine = lines[0].split(" ");
            HttpMethod method = HttpMethod.valueOf(requestLine[0]);
            String uri = requestLine[1];
            HttpVersion version = HttpVersion.valueOf(requestLine[2]);

            // Create a new FullHttpRequest
            FullHttpRequest newRequest = new DefaultFullHttpRequest(version, method, uri);

            // Parse headers
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.contains(":")) {
                    String[] headerParts = line.split(":", 2);
                    newRequest.headers().set(headerParts[0].trim(), headerParts[1].trim());
                }
            }

            // Parse body
            if (sections.length > 1) {
                String body = sections[1];
                newRequest.content().clear().writeBytes(body.getBytes(CharsetUtil.UTF_8));
                newRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, body.length());
            }

            System.out.println("parseModifiedRequest(): " + newRequest.headers() + "\n -- \n"  + newRequest.content());
            return newRequest;
        } catch (Exception e) {
            LoggerUtil.log("Error parsing modified request: " + e.getMessage());
            return null;
        }
    }


}
