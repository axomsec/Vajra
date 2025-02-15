package handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import utils.LoggerUtil;


import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestInterceptorHandler {


    private static final Logger logger = Logger.getLogger(RequestInterceptorHandler.class.getName());
    private FullHttpRequest request;


    public static class InterceptedRequestData {
        private final String requestLine;
        private final LinkedHashMap<String, String> headers;
        private final String body;



        public InterceptedRequestData(String requestLine, LinkedHashMap<String, String> headers, String body) {
            this.requestLine = requestLine;
            this.headers = headers;
            this.body = body;
        }

        public String getRequestLine() {
            return requestLine;
        }

        public LinkedHashMap<String, String> getHeaders() {
            return headers;
        }

        public String getBody() {
            return body;
        }
    }

    /***
     * @param request
     * @return InterceptedRequestData(requestLine, headers, body)
     * handleRequest takes a FullHttpRequest request as an argument which then segregates 3 parts of the request
     * and returns it, which involves
     *      1. requestLine(itself contains 3 more parts concatenated together)
     *          * HTTP method, HTTP URI & HTTP protocol version
     *      2. headers
     *      3. body
     * This method helps you develop formatted structure for the consumer to play with yet having more control
     * to the output.
     */

    public static InterceptedRequestData handleRequest(FullHttpRequest request) {
        // Extract request line: "METHOD URI PROTOCOL"
        String requestLine = request.method() + " " + request.uri() + " " + request.protocolVersion();

        // Extract headers in order
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        for (Map.Entry<String, String> header : request.headers()) {
            headers.put(header.getKey(), header.getValue());
        }

        // Extract body (raw)
        String body = "";
        if (request.content().isReadable()) {
            body = request.content().toString(CharsetUtil.UTF_8);
        }

        // No logging or formatting here, just return the structured data
        return new InterceptedRequestData(requestLine, headers, body);
    }


    /**
     * @param modifiedRequestData
     * @return (FullHttpRequest) request
     *
     * @description This method creates back a modified FullHttpRequest and returns it, the modified data is constantly
     * from JTextArea of the Intercept tab.
     *
     * @cons If any extra headers data is being added to the requests, this method is not capable of parsing it at the
     * moment. This needs to be addressed later for headers.
     *      1. addition
     *      2. manipulation
     *      3. deletion.
     */
    public FullHttpRequest parseModifiedRequestToFullHttpRequest(String modifiedRequestData, boolean isTLS) {
        // Split into lines
        String[] lines = modifiedRequestData.split("\n");
        if (lines.length == 0) {
            // If no data, return a basic empty GET request to / as a fallback
            return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        }

        // Parse request line
        String requestLine = lines[0].trim();
        String[] requestLineParts = requestLine.split(" ");
        if (requestLineParts.length != 3) {
            // Invalid request line format, fallback or throw error
            throw new IllegalArgumentException("Invalid request line: " + requestLine);
        }

        HttpMethod method = HttpMethod.valueOf(requestLineParts[0]);
        String uri = requestLineParts[1];
        HttpVersion version = HttpVersion.valueOf(requestLineParts[2]);


        logger.log(Level.INFO, "RequestInterceptorHandler: method = {0}", method);
        logger.log(Level.INFO, "RequestInterceptorHandler: uri = {0}", uri);
        logger.log(Level.INFO, "RequestInterceptorHandler: version = {0}", version);


        // Parse headers
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        int i = 1;
        for (; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                // blank line indicates the start of the body
                break;
            }
            int colonIndex = line.indexOf(':');
            if (colonIndex != -1) {
                String headerName = line.substring(0, colonIndex).trim();
                String headerValue = line.substring(colonIndex + 1).trim();
                headers.put(headerName, headerValue);
            }
        }

        // Parse body
        StringBuilder bodyBuilder = new StringBuilder();
        for (i = i + 1; i < lines.length; i++) {
            bodyBuilder.append(lines[i]).append("\n");
        }
        String body = bodyBuilder.toString().trim();



        // Create the FullHttpRequest
        ByteBuf content = Unpooled.EMPTY_BUFFER;
        if (!body.isEmpty()) {
            content = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8);
        }
        FullHttpRequest request = new DefaultFullHttpRequest(version, method, uri, content);

        // Set headers
        for (String headerName : headers.keySet()) {
            request.headers().set(headerName, headers.get(headerName));
        }

        // Update Content-Length if body is present
        if (!body.isEmpty()) {
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        } else {
            request.headers().remove(HttpHeaderNames.CONTENT_LENGTH);
        }

        return request;
    }


    /**
     * Converts any URI (absolute or relative) into a purely relative path + query.
     * For example:
     * http://example.com/foo -> /foo
     * /foo                  -> /foo
     * /foo?bar=baz          -> /foo?bar=baz
     */
    public static String extractRelativePath(String rawUri) {
        try {
            URI uriObj = new URI(rawUri);

            // Get path; default to "/" if empty
            String path = uriObj.getRawPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }

            // Append query if present
            String query = uriObj.getRawQuery();
            if (query != null && !query.isEmpty()) {
                path += "?" + query;
            }

            return path;
        } catch (URISyntaxException e) {
            // If it's not a valid URI (or if it doesn't have a scheme/host),
            // we treat it as an already relative path. Ensure it starts with '/'.
            logger.log(Level.WARNING, "URI parse error or relative URI. Falling back to raw: {0}", rawUri);

            // Force a leading slash if not present
            if (!rawUri.startsWith("/")) {
                return "/" + rawUri;
            }
            return rawUri;
        }
    }

    // Decode URL-encoded form data into a key-value map
    private static Map<String, String> decodeFormData(String body) {
        Map<String, String> formData = new HashMap<>();
        try {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    formData.put(key, value);
                }
            }
        } catch (Exception e) {
            LoggerUtil.log("Error decoding form data: " + e.getMessage());
        }
        return formData;
    }

}
