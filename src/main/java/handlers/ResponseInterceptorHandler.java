package handlers;

import com.google.common.primitives.Chars;
import filters.InterceptingFilter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;
import utils.LoggerUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResponseInterceptorHandler {

    // Add a logger instance
    private static final Logger logger = Logger.getLogger(InterceptingFilter.class.getName());

    public static String handleResponse(FullHttpResponse response) {
        StringBuilder formattedResponse = new StringBuilder();


        // 1. Append the status line

        // HTTP/1.1
        String protocolVersion  = response.protocolVersion().text();

        // 200
        int statusCode          = response.status().code();

        // OK
        String reasonPhrase     = response.status().reasonPhrase();

        formattedResponse.append(protocolVersion)
                        .append(" ")
                        .append(statusCode)
                        .append(" ")
                        .append(reasonPhrase)
                        .append("\n");

        // 2. Append all headers
        HttpHeaders headers = response.headers();
        for(Map.Entry<String, String> header: headers){
            formattedResponse.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\n");
        }

        // 3. Append a blank line to separate headers from the body
        formattedResponse.append("\n");

        // 4. Append the body content
        ByteBuf content = response.content();
        if(content.isReadable()){
            // determine the charset from the Content-Type header if available
            Charset charset = getCharsetFromHeaders(headers);

            // Convert ByteBuf to String using the determined charset
            String body = content.toString(charset);
            formattedResponse.append(body);
        }




        logger.log(Level.INFO, "ResponseInterceptorHandler: formattedResponse = " + formattedResponse.toString());
        return formattedResponse.toString();

//        LoggerUtil.log("Intercepted Response:");
//        LoggerUtil.log("Status: " + response.getStatus());
//        LoggerUtil.log("Headers: " + response.headers().toString());
//
//        // Print body of the response
//        String responseBody = response.content().toString(CharsetUtil.UTF_8);
//        LoggerUtil.log("Body: " + responseBody);
    }


    /**
     * Determines the character set from the Content-Type header.
     * Defaults to UTF-8 if not specified or unrecognized.
     *
     * @param headers The HttpHeaders from the response.
     * @return The Charset to use for decoding the body.
     */
    private static Charset getCharsetFromHeaders(HttpHeaders headers) {
        String contentType = headers.get("Content-Type");
        if (contentType != null) {
            // Example Content-Type: "text/html; charset=UTF-8"
            String[] parts = contentType.split(";");
            for (String part : parts) {
                part = part.trim();
                if (part.toLowerCase().startsWith("charset=")) {
                    String charsetName = part.substring("charset=".length());
                    try {
                        return Charset.forName(charsetName);
                    } catch (Exception e) {
                        // Unsupported charset; fallback to UTF-8
                        break;
                    }
                }
            }
        }
        // Default charset
        return StandardCharsets.UTF_8;
    }
}
