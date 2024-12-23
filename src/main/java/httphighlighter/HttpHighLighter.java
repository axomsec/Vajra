package httphighlighter;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import org.json.*; // If you have a JSON library. If not, you can do rudimentary parsing yourself.


import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.*; // Ensure you have the org.json library in your classpath

//public class HttpHighLighter {

    /**
     * Highlights an HTTP request/response string in a JEditorPane.
     * 1) Applies a line-based style (request line, header line, or body).
     * 2) For any line identified as a header, further splits the "header key" vs. "header value".
     * 3) Within each line, finds name-value pairs (paramName=paramValue)
     *    and highlights them in different colors.
     */
//    public static void highlightHttp(String httpMessage, JEditorPane editorPane) {
//        // Ensure the JEditorPane uses a StyledDocument
//        if (!(editorPane.getDocument() instanceof StyledDocument)) {
//            editorPane.setEditorKit(new StyledEditorKit());
//        }
//        StyledDocument doc = (StyledDocument) editorPane.getDocument();
//
//        // Remove existing text
//        try {
//            doc.remove(0, doc.getLength());
//        } catch (BadLocationException e) {
//            e.printStackTrace();
//        }
//
//        // Insert the raw text
//        try {
//            doc.insertString(0, httpMessage, null);
//        } catch (BadLocationException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        // Create our styles
//        StyleContext sc = new StyleContext();
//        // Line-based fallback styles
//        Style defaultStyle       = createDefaultStyle(sc);
//        Style requestLineStyle   = createRequestLineStyle(sc);
//        Style headerLineStyle    = createHeaderLineStyle(sc);
//        Style bodyLineStyle      = createBodyLineStyle(sc);
//
//        // Extra style just for the "header key" (the portion before the colon)
//        Style headerKeyStyle     = createHeaderKeyStyle(sc);
//
//        // Param highlight styles
//        Style paramNameStyle     = createParamNameStyle(sc);
//        Style paramValueStyle    = createParamValueStyle(sc);
//
//        // Split into lines
//        String[] lines = httpMessage.split("\r?\n");
//        int offset = 0;
//
//        for (String line : lines) {
//            // 1) Determine "fallback" style for the entire line
//            Style fallbackStyle = determineLineStyle(
//                    line,
//                    requestLineStyle,
//                    headerLineStyle,
//                    bodyLineStyle
//            );
//
//            // Apply that fallback style to the whole line
//            doc.setCharacterAttributes(offset, line.length(), fallbackStyle, true);
//
//            // 2) If this line is recognized as a header, let's do partial highlight
//            //    for the "header key" vs. "header value".
//            if (isHeaderLine(line)) {
//                applyHeaderKeyHighlight(
//                        doc, line, offset,
//                        headerKeyStyle,   // for the part before the colon
//                        fallbackStyle     // for the part after the colon (header value)
//                );
//            }
//
//            // 3) Within the same line, highlight name=value pairs
//            //    (this applies to headers, body, etc.).
//            applyNameValueHighlight(doc, line, offset, paramNameStyle, paramValueStyle);
//
//            offset += line.length() + 1;  // +1 for newline
//        }
//    }
//
//    // ------------------------------------------------------------------------
//    // 1) Determine which line style to apply (request, header, or body)
//    // ------------------------------------------------------------------------
//    private static Style determineLineStyle(String line,
//                                            Style requestLineStyle,
//                                            Style headerLineStyle,
//                                            Style bodyLineStyle) {
//        // e.g., "POST /something HTTP/1.1" or "HTTP/1.1 200 OK"
//        if (line.matches("^(GET|POST|PUT|DELETE|PATCH|OPTIONS|HEAD)\\s.*HTTP/\\d\\.\\d$")
//                || line.matches("^HTTP/\\d\\.\\d\\s+\\d+.*")) {
//            return requestLineStyle;
//        }
//        // e.g., "Host: example.com", "Cookie: JSESSIONID=abc"
//        else if (isHeaderLine(line)) {
//            return headerLineStyle;
//        }
//        // otherwise
//        else {
//            return bodyLineStyle;
//        }
//    }
//
//    /**
//     * Checks if a line matches the pattern of an HTTP header:
//     * someHeaderName: someHeaderValue
//     */
//    private static boolean isHeaderLine(String line) {
//        return line.matches("^[A-Za-z0-9-]+:.*");
//    }
//
//    // ------------------------------------------------------------------------
//    // 2) For lines recognized as headers, highlight the header key vs. value
//    // ------------------------------------------------------------------------
//    /**
//     * Splits a header line at the first colon,
//     * and highlights the header key in a separate color.
//     *
//     * Example: "Cookie: JSESSIONID=abc" =>
//     *  - "Cookie" portion => headerKeyStyle
//     *  - everything after ": " => fallbackHeaderStyle
//     */
//    private static void applyHeaderKeyHighlight(
//            StyledDocument doc,
//            String line,
//            int lineOffset,
//            Style headerKeyStyle,
//            Style fallbackHeaderStyle
//    ) {
//        int colonIndex = line.indexOf(':');
//        if (colonIndex < 0) {
//            return; // no colon found, skip
//        }
//
//        // 1) highlight the header key (from start to the colon)
//        doc.setCharacterAttributes(
//                lineOffset,
//                colonIndex,
//                headerKeyStyle,
//                true
//        );
//
//        // 2) optionally, re-apply fallback style to the colon itself
//        //    (to avoid having the colon appear in the key color)
//        doc.setCharacterAttributes(
//                lineOffset + colonIndex,
//                1,
//                fallbackHeaderStyle,
//                true
//        );
//        // The remainder (after the colon) is already covered by fallbackHeaderStyle
//        // for the entire line, so that’s enough.
//    }
//
//    // ------------------------------------------------------------------------
//    // 3) Within the same line, highlight name=value pairs
//    // ------------------------------------------------------------------------
//    /**
//     * Finds all name=value pairs in the line (using a regex),
//     * then applies paramNameStyle and paramValueStyle.
//     * e.g.: "JSESSIONID=F65A..." =>
//     *   "JSESSIONID" => paramNameStyle,
//     *   "F65A..." => paramValueStyle
//     */
//    private static void applyNameValueHighlight(StyledDocument doc,
//                                                String line,
//                                                int lineOffset,
//                                                Style paramNameStyle,
//                                                Style paramValueStyle) {
//        // Regex to capture name=value pairs in many contexts
//        // e.g. "JSESSIONID=abc123", "foo=bar", etc.
//        // Adjust for your needs (allow underscores, etc.).
//        Pattern pattern = Pattern.compile("([A-Za-z0-9_.-]+)=([^&;\\s]+)");
//        Matcher matcher = pattern.matcher(line);
//
//        while (matcher.find()) {
//            int paramNameStart = matcher.start(1);
//            int paramNameEnd   = matcher.end(1);
//            int paramValueStart= matcher.start(2);
//            int paramValueEnd  = matcher.end(2);
//
//            // highlight the param name
//            doc.setCharacterAttributes(
//                    lineOffset + paramNameStart,
//                    paramNameEnd - paramNameStart,
//                    paramNameStyle,
//                    true
//            );
//
//            // highlight the param value
//            doc.setCharacterAttributes(
//                    lineOffset + paramValueStart,
//                    paramValueEnd - paramValueStart,
//                    paramValueStyle,
//                    true
//            );
//        }
//    }
//
//    // ------------------------------------------------------------------------
//    // Style Creation
//    // ------------------------------------------------------------------------
//    private static Style createDefaultStyle(StyleContext sc) {
//        Style s = sc.addStyle("Default", null);
//        StyleConstants.setFontFamily(s, "Monospaced");
//        StyleConstants.setForeground(s, Color.BLACK);
//        return s;
//    }
//
//    /** Request/Status line style: bold, dark blue. */
//    private static Style createRequestLineStyle(StyleContext sc) {
//        Style s = sc.addStyle("RequestLine", null);
//        StyleConstants.setFontFamily(s, "Monospaced");
//        StyleConstants.setBold(s, false);
//        StyleConstants.setForeground(s, new Color(0, 0, 160));
//        return s;
//    }
//
//    /** Header line style: dark red (fallback for entire header line). */
//    private static Style createHeaderLineStyle(StyleContext sc) {
//        Style s = sc.addStyle("HeaderLine", null);
//        StyleConstants.setFontFamily(s, "Monospaced");
//        StyleConstants.setForeground(s, new Color(128, 0, 0));
//        return s;
//    }
//
//    /** Body style: dark green. */
//    private static Style createBodyLineStyle(StyleContext sc) {
//        Style s = sc.addStyle("BodyLine", null);
//        StyleConstants.setFontFamily(s, "Monospaced");
//        StyleConstants.setForeground(s, Color.GREEN.darker());
//        return s;
//    }
//
//    /** Header key style (the part before the colon), e.g. "Cookie" => dark magenta, bold. */
//    private static Style createHeaderKeyStyle(StyleContext sc) {
//        Style s = sc.addStyle("HeaderKey", null);
//        StyleConstants.setFontFamily(s, "Monospaced");
//        StyleConstants.setForeground(s, new Color(0, 0, 170)); // dark magenta
//        StyleConstants.setBold(s, false);
//        return s;
//    }
//
//    /** Param name style (e.g. "JSESSIONID" or "foo"). */
//    private static Style createParamNameStyle(StyleContext sc) {
//        Style s = sc.addStyle("ParamName", null);
//        StyleConstants.setFontFamily(s, "Monospaced");
//        StyleConstants.setBold(s, false);
//        StyleConstants.setForeground(s, new Color(0,0,238));
//        return s;
//    }
//
//    /** Param value style (e.g. "abc123"). */
//    private static Style createParamValueStyle(StyleContext sc) {
//        Style s = sc.addStyle("ParamValue", null);
//        StyleConstants.setFontFamily(s, "Monospaced");
//        StyleConstants.setForeground(s, new Color(139, 0, 0)); // dark red
//        return s;
//    }
//}



/**
 * A utility class for syntax highlighting HTTP requests in a JEditorPane.
 */
//public class HttpHighLighter {
//    /**
//     * Applies syntax highlighting to the provided HTTP request and displays it in the JEditorPane.
//     *
//     * @param httpRequest The full HTTP request as a string.
//     * @param editorPane  The JEditorPane component where the highlighted request will be displayed.
//     */
//    public static void createStyledHttpView(String httpRequest, JEditorPane editorPane) {
//        // Ensure the EditorKit is set to StyledEditorKit for styled text support
//        editorPane.setEditorKit(new StyledEditorKit());
//        editorPane.setContentType("text/plain"); // Use "text/plain" to work with StyledDocument
//        editorPane.setEditable(false); // Make it read-only if desired
//
//        // Cast the document to StyledDocument
//        StyledDocument doc;
//        try {
//            doc = (StyledDocument) editorPane.getDocument();
//        } catch (ClassCastException e) {
//            // If the document is not a StyledDocument, replace it with one
//            doc = new DefaultStyledDocument();
//            editorPane.setDocument(doc);
//        }
//
//        // Clear the document before adding the new request
//        try {
//            doc.remove(0, doc.getLength());
//        } catch (BadLocationException e) {
//            e.printStackTrace();
//        }
//
//        // Define styles
//        StyleContext sc = StyleContext.getDefaultStyleContext();
//        Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
//
//        // Request line style
//        Style requestLineStyle = doc.addStyle("requestLine", defaultStyle);
//        StyleConstants.setBold(requestLineStyle, false);
//        StyleConstants.setForeground(requestLineStyle, new Color(0, 0, 0)); // Black
//
//        // Header key style
//        Style headerKeyStyle = doc.addStyle("headerKey", defaultStyle);
//        StyleConstants.setBold(headerKeyStyle, false);
//        StyleConstants.setForeground(headerKeyStyle, new Color(0, 0, 170)); // Dark Blue
//
//        // Header value style
//        Style headerValueStyle = doc.addStyle("headerValue", defaultStyle);
//        StyleConstants.setForeground(headerValueStyle, new Color(0, 0, 0)); // Black
//
//        // Body key style
//        Style bodyKeyStyle = doc.addStyle("bodyKey", defaultStyle);
//        StyleConstants.setForeground(bodyKeyStyle, new Color(0, 0, 170)); // Dark Blue
//
//        // Body value style
//        Style bodyValueStyle = doc.addStyle("bodyValue", defaultStyle);
//        StyleConstants.setForeground(bodyValueStyle, Color.GREEN.darker());
//
//        // Param key/value styles for request line parameters
//        Style paramKeyStyle = doc.addStyle("paramKey", defaultStyle);
//        StyleConstants.setForeground(paramKeyStyle, new Color(0, 0, 238)); // Blue
//
//        Style paramValueStyle = doc.addStyle("paramValue", defaultStyle);
//        StyleConstants.setForeground(paramValueStyle, Color.RED.darker());
//
//        // Parse the request into components
//        String[] lines = httpRequest.split("\r\n|\n");
//        String requestLine = (lines.length > 0) ? lines[0] : "";
//
//        // Insert the request line with parameter highlighting
//        insertRequestLineWithParams(doc, requestLine, requestLineStyle, paramKeyStyle, paramValueStyle);
//
//        // Parse headers
//        int i = 1;
//        for (; i < lines.length; i++) {
//            String line = lines[i];
//            if (line.trim().isEmpty()) {
//                // Reached blank line separating headers and body
//                i++;
//                break;
//            }
//            insertHeaderLine(doc, line, headerKeyStyle, headerValueStyle);
//        }
//
//        // Parse body (if any lines remain)
//        StringBuilder body = new StringBuilder();
//        for (; i < lines.length; i++) {
//            body.append(lines[i]).append("\n");
//        }
//
//        if (body.length() > 0) {
//            insertBody(doc, body.toString(), bodyKeyStyle, bodyValueStyle);
//        }
//    }
//
//    /**
//     * Inserts the HTTP request line into the document with parameter highlighting.
//     *
//     * @param doc             The StyledDocument to insert text into.
//     * @param requestLine     The HTTP request line.
//     * @param requestLineStyle The style to apply to the request line.
//     * @param paramKeyStyle   The style to apply to parameter keys.
//     * @param paramValueStyle The style to apply to parameter values.
//     */
//    private static void insertRequestLineWithParams(StyledDocument doc, String requestLine,
//                                                    Style requestLineStyle,
//                                                    Style paramKeyStyle,
//                                                    Style paramValueStyle) {
//        // Split by spaces: METHOD, URI, VERSION
//        String[] parts = requestLine.split(" ", 3);
//        if (parts.length < 3) {
//            // If the request line doesn't have exactly 3 parts, insert as is
//            insertStyledText(doc, requestLine + "\n", requestLineStyle);
//            return;
//        }
//
//        String method = parts[0];
//        String uri = parts[1];
//        String version = parts[2];
//
//        // Insert the METHOD and a space
//        insertStyledText(doc, method + " ", requestLineStyle);
//
//        // Check if URI has query parameters
//        int questionMarkIndex = uri.indexOf('?');
//        if (questionMarkIndex == -1) {
//            // No query parameters, insert as normal
//            insertStyledText(doc, uri + " ", requestLineStyle);
//        } else {
//            // Insert the path part before the '?'
//            String pathPart = uri.substring(0, questionMarkIndex);
//            insertStyledText(doc, pathPart, requestLineStyle);
//
//            // Insert the '?'
//            insertStyledText(doc, "?", requestLineStyle);
//
//            // Extract query string
//            String queryString = uri.substring(questionMarkIndex + 1);
//
//            // Split into key-value pairs
//            String[] pairs = queryString.split("&");
//            boolean firstParam = true;
//            for (String pair : pairs) {
//                // Insert '&' between parameters if not the first
//                if (!firstParam) {
//                    insertStyledText(doc, "&", requestLineStyle);
//                }
//                firstParam = false;
//
//                // Each pair should be "key=value"
//                int eqIndex = pair.indexOf('=');
//                if (eqIndex == -1) {
//                    // No '=' found, insert as-is
//                    insertStyledText(doc, pair, requestLineStyle);
//                } else {
//                    String key = pair.substring(0, eqIndex);
//                    String value = pair.substring(eqIndex + 1);
//
//                    // Insert the key in key style
//                    insertStyledText(doc, key, paramKeyStyle);
//
//                    // Insert '=' symbol in request line style
//                    insertStyledText(doc, "=", requestLineStyle);
//
//                    // Insert the value in value style
//                    insertStyledText(doc, value, paramValueStyle);
//                }
//            }
//            // Add a trailing space after parameters
//            insertStyledText(doc, " ", requestLineStyle);
//        }
//
//        // Insert the VERSION and a newline
//        insertStyledText(doc, version + "\n", requestLineStyle);
//    }
//
//    /**
//     * Inserts a header line into the document with key and value styling.
//     *
//     * @param doc        The StyledDocument to insert text into.
//     * @param headerLine The header line string.
//     * @param keyStyle   The style to apply to header keys.
//     * @param valueStyle The style to apply to header values.
//     */
//    private static void insertHeaderLine(StyledDocument doc, String headerLine, Style keyStyle, Style valueStyle) {
//        int colonIndex = headerLine.indexOf(":");
//        if (colonIndex != -1) {
//            String key = headerLine.substring(0, colonIndex).trim();
//            String value = headerLine.substring(colonIndex + 1).trim();
//            insertStyledText(doc, key + ": ", keyStyle);
//            insertStyledText(doc, value + "\n", valueStyle);
//        } else {
//            // Malformed header line, just insert as normal text
//            insertStyledText(doc, headerLine + "\n", valueStyle);
//        }
//    }
//
//    /**
//     * Inserts the body of the HTTP request into the document, applying JSON styling if applicable.
//     *
//     * @param doc         The StyledDocument to insert text into.
//     * @param body        The body content as a string.
//     * @param keyStyle    The style to apply to JSON keys.
//     * @param valueStyle  The style to apply to JSON values.
//     */
//    private static void insertBody(StyledDocument doc, String body, Style keyStyle, Style valueStyle) {
//        // Attempt to parse as JSON
//        // If parsing fails, insert as plain text
//        String trimmedBody = body.trim();
//        if (trimmedBody.startsWith("{") || trimmedBody.startsWith("[")) {
//            try {
//                // Use JSON library to parse
//                // If object:
//                if (trimmedBody.startsWith("{")) {
//                    JSONObject jsonObject = new JSONObject(trimmedBody);
//                    insertJsonObject(doc, jsonObject, keyStyle, valueStyle, 0);
//                } else if (trimmedBody.startsWith("[")) {
//                    JSONArray jsonArray = new JSONArray(trimmedBody);
//                    insertJsonArray(doc, jsonArray, keyStyle, valueStyle, 0);
//                }
//            } catch (JSONException e) {
//                // Not valid JSON, insert as plain text
//                insertStyledText(doc, "\n" + body, valueStyle);
//            }
//        } else {
//            // Not JSON-like, just insert as plain text
//            insertStyledText(doc, "\n" + body, valueStyle);
//        }
//    }
//
//    /**
//     * Recursively inserts a JSONObject into the document with proper indentation and styling.
//     *
//     * @param doc         The StyledDocument to insert text into.
//     * @param jsonObject  The JSONObject to insert.
//     * @param keyStyle    The style to apply to JSON keys.
//     * @param valueStyle  The style to apply to JSON values.
//     * @param indent      The current indentation level.
//     */
//    private static void insertJsonObject(StyledDocument doc, JSONObject jsonObject, Style keyStyle, Style valueStyle, int indent) {
//        insertStyledText(doc, "\n" + spaces(indent) + "{\n", valueStyle);
//        String[] keys = JSONObject.getNames(jsonObject);
//        if (keys != null) {
//            for (int i = 0; i < keys.length; i++) {
//                String k = keys[i];
//                Object v = jsonObject.get(k);
//                insertStyledText(doc, spaces(indent + 2) + "\"" + k + "\": ", keyStyle);
//                insertJsonValue(doc, v, keyStyle, valueStyle, indent + 2);
//                if (i < keys.length - 1) {
//                    insertStyledText(doc, ",\n", valueStyle);
//                } else {
//                    insertStyledText(doc, "\n", valueStyle);
//                }
//            }
//        }
//        insertStyledText(doc, spaces(indent) + "}\n", valueStyle);
//    }
//
//    /**
//     * Recursively inserts a JSONArray into the document with proper indentation and styling.
//     *
//     * @param doc         The StyledDocument to insert text into.
//     * @param jsonArray   The JSONArray to insert.
//     * @param keyStyle    The style to apply to JSON keys.
//     * @param valueStyle  The style to apply to JSON values.
//     * @param indent      The current indentation level.
//     */
//    private static void insertJsonArray(StyledDocument doc, JSONArray jsonArray, Style keyStyle, Style valueStyle, int indent) {
//        insertStyledText(doc, "\n" + spaces(indent) + "[\n", valueStyle);
//        for (int i = 0; i < jsonArray.length(); i++) {
//            Object v = jsonArray.get(i);
//            insertJsonValue(doc, v, keyStyle, valueStyle, indent + 2);
//            if (i < jsonArray.length() - 1) {
//                insertStyledText(doc, ",\n", valueStyle);
//            } else {
//                insertStyledText(doc, "\n", valueStyle);
//            }
//        }
//        insertStyledText(doc, spaces(indent) + "]\n", valueStyle);
//    }
//
//    /**
//     * Inserts a JSON value into the document, handling nested objects and arrays.
//     *
//     * @param doc         The StyledDocument to insert text into.
//     * @param v           The JSON value to insert.
//     * @param keyStyle    The style to apply to JSON keys.
//     * @param valueStyle  The style to apply to JSON values.
//     * @param indent      The current indentation level.
//     */
//    private static void insertJsonValue(StyledDocument doc, Object v, Style keyStyle, Style valueStyle, int indent) {
//        if (v instanceof JSONObject) {
//            insertJsonObject(doc, (JSONObject) v, keyStyle, valueStyle, indent);
//        } else if (v instanceof JSONArray) {
//            insertJsonArray(doc, (JSONArray) v, keyStyle, valueStyle, indent);
//        } else if (v instanceof String) {
//            insertStyledText(doc, "\"" + v.toString() + "\"", valueStyle);
//        } else {
//            // For numbers, booleans, null
//            insertStyledText(doc, v.toString(), valueStyle);
//        }
//    }
//
//    /**
//     * Inserts styled text into the document.
//     *
//     * @param doc   The StyledDocument to insert text into.
//     * @param text  The text to insert.
//     * @param style The Style to apply to the inserted text.
//     */
//    private static void insertStyledText(StyledDocument doc, String text, Style style) {
//        try {
//            doc.insertString(doc.getLength(), text, style);
//        } catch (BadLocationException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Generates a string of spaces for indentation.
//     *
//     * @param count The number of spaces.
//     * @return A string consisting of the specified number of spaces.
//     */
//    private static String spaces(int count) {
//        StringBuilder sb = new StringBuilder();
//        for(int i = 0; i < count; i++) {
//            sb.append(' ');
//        }
//        return sb.toString();
//    }
//}



public class HttpHighLighter {
    /**
     * Creates a styled JTextPane containing the highlighted HTTP request.
     *
     * @param httpRequest The full HTTP request as a string.
     * @return A JTextPane component with syntax highlighting applied.
     */
    public static String createStyledHttpView(String httpRequest, JTextPane textPane) {


        StyledDocument doc = textPane.getStyledDocument();

        // Clear the document before adding the new request
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Define styles
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        // Request line style
        Style requestLineStyle = doc.addStyle("requestLine", defaultStyle);
        StyleConstants.setBold(requestLineStyle, false);
        StyleConstants.setForeground(requestLineStyle, new Color(0, 0, 0)); // black

        // Header key style
        Style headerKeyStyle = doc.addStyle("headerKey", defaultStyle);
        StyleConstants.setBold(headerKeyStyle, false);
        StyleConstants.setForeground(headerKeyStyle, new Color(0, 0, 170));

        // Header value style
        Style headerValueStyle = doc.addStyle("headerValue", defaultStyle);
        StyleConstants.setForeground(headerValueStyle, new Color(0, 0, 0)); // black

        // Body key style
        Style bodyKeyStyle = doc.addStyle("bodyKey", defaultStyle);
        StyleConstants.setForeground(bodyKeyStyle, new Color(0, 0, 170));

        // Body value style
        Style bodyValueStyle = doc.addStyle("bodyValue", defaultStyle);
        StyleConstants.setForeground(bodyValueStyle, Color.GREEN.darker());

        // Param key/value styles for request line parameters
        Style paramKeyStyle = doc.addStyle("paramKey", defaultStyle);
        StyleConstants.setForeground(paramKeyStyle, new Color(0, 0, 238));

        Style paramValueStyle = doc.addStyle("paramValue", defaultStyle);
        StyleConstants.setForeground(paramValueStyle, Color.RED.darker());

        // Parse the request into components
        String[] lines = httpRequest.split("\r\n|\n");
        String requestLine = (lines.length > 0) ? lines[0] : "";

        // Insert the request line with parameter highlighting
        insertRequestLineWithParams(doc, requestLine, requestLineStyle, paramKeyStyle, paramValueStyle);

        // Parse headers
        int i = 1;
        for (; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().isEmpty()) {
                // Reached blank line separating headers and body
                i++;
                break;
            }
            insertHeaderLine(doc, line, headerKeyStyle, headerValueStyle);
        }

        // Parse body (if any lines remain)
        StringBuilder body = new StringBuilder();
        for (; i < lines.length; i++) {
            body.append(lines[i]).append("\n");
        }

        if (body.length() > 0) {
            insertBody(doc, body.toString(), bodyKeyStyle, bodyValueStyle);
        }

        return httpRequest;
    }

    // Modified insertRequestLineWithParams method:
    private static void insertRequestLineWithParams(StyledDocument doc, String requestLine,
                                                    Style requestLineStyle,
                                                    Style paramKeyStyle,
                                                    Style paramValueStyle) {
        // Split by spaces: METHOD, URI, VERSION
        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 3) {
            // If the request line doesn't have exactly 3 parts, insert as is
            insertStyledText(doc, requestLine + "\n", requestLineStyle);
            return;
        }

        String method = parts[0];
        String uri = parts[1];
        String version = parts[2];

        // Insert the METHOD and a space
        insertStyledText(doc, method + " ", requestLineStyle);

        // Check if URI has query parameters
        int questionMarkIndex = uri.indexOf('?');
        if (questionMarkIndex == -1) {
            // No query parameters, insert as normal
            insertStyledText(doc, uri + " ", requestLineStyle);
        } else {
            // Insert the path part before the '?'
            String pathPart = uri.substring(0, questionMarkIndex);
            insertStyledText(doc, pathPart, requestLineStyle);

            // Insert the '?'
            insertStyledText(doc, "?", requestLineStyle);

            // Extract query string
            String queryString = uri.substring(questionMarkIndex + 1);

            // Split into key-value pairs
            String[] pairs = queryString.split("&");
            boolean firstParam = true;
            for (String pair : pairs) {
                // Insert '&' between parameters if not the first
                if (!firstParam) {
                    insertStyledText(doc, "&", requestLineStyle);
                }
                firstParam = false;

                // Each pair should be "key=value"
                int eqIndex = pair.indexOf('=');
                if (eqIndex == -1) {
                    // No '=' found, insert as-is
                    insertStyledText(doc, pair, requestLineStyle);
                } else {
                    String key = pair.substring(0, eqIndex);
                    String value = pair.substring(eqIndex + 1);

                    // Insert the key in key style
                    insertStyledText(doc, key, paramKeyStyle);

                    // Insert '=' symbol in request line style
                    insertStyledText(doc, "=", requestLineStyle);

                    // Insert the value in value style
                    insertStyledText(doc, value, paramValueStyle);
                }
            }
            // Add a trailing space after parameters
            insertStyledText(doc, " ", requestLineStyle);
        }

        // Insert the VERSION and a newline
        insertStyledText(doc, version + "\n", requestLineStyle);
    }


    private static void insertStyledText(StyledDocument doc, String text, Style style) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private static void insertHeaderLine(StyledDocument doc, String headerLine, Style keyStyle, Style valueStyle) {
        int colonIndex = headerLine.indexOf(":");
        if (colonIndex != -1) {
            String key = headerLine.substring(0, colonIndex).trim();
            String value = headerLine.substring(colonIndex + 1).trim();
            insertStyledText(doc, key + ": ", keyStyle);
            insertStyledText(doc, value + "\n", valueStyle);
        } else {
            // Malformed header line, just insert as normal text
            insertStyledText(doc, headerLine + "\n", valueStyle);
        }
    }

    private static void insertBody(StyledDocument doc, String body, Style keyStyle, Style valueStyle) {
        // Attempt to parse as JSON
        // If parsing fails, insert as plain text
        String trimmedBody = body.trim();
        if (trimmedBody.startsWith("{") || trimmedBody.startsWith("[")) {
            try {
                // Use JSON library to parse
                // If object:
                if (trimmedBody.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(trimmedBody);
                    insertJsonObject(doc, jsonObject, keyStyle, valueStyle, 0);
                } else if (trimmedBody.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(trimmedBody);
                    insertJsonArray(doc, jsonArray, keyStyle, valueStyle, 0);
                }
            } catch (JSONException e) {
                // Not valid JSON, insert as plain text
                insertStyledText(doc, "\n" + body, valueStyle);
            }
        } else {
            // Not JSON-like, just insert as plain text
            insertStyledText(doc, "\n" + body, valueStyle);
        }
    }

    private static void insertJsonObject(StyledDocument doc, JSONObject jsonObject, Style keyStyle, Style valueStyle, int indent) {
        insertStyledText(doc, "\n" + spaces(indent) + "{\n", valueStyle);
        String[] keys = JSONObject.getNames(jsonObject);
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                Object v = jsonObject.get(k);
                insertStyledText(doc, spaces(indent + 2) + "\"" + k + "\": ", keyStyle);
                insertJsonValue(doc, v, keyStyle, valueStyle, indent + 2);
                if (i < keys.length - 1) {
                    insertStyledText(doc, ",\n", valueStyle);
                } else {
                    insertStyledText(doc, "\n", valueStyle);
                }
            }
        }
        insertStyledText(doc, spaces(indent) + "}\n", valueStyle);
    }

    private static void insertJsonArray(StyledDocument doc, JSONArray jsonArray, Style keyStyle, Style valueStyle, int indent) {
        insertStyledText(doc, "\n" + spaces(indent) + "[\n", valueStyle);
        for (int i = 0; i < jsonArray.length(); i++) {
            Object v = jsonArray.get(i);
            insertJsonValue(doc, v, keyStyle, valueStyle, indent + 2);
            if (i < jsonArray.length() - 1) {
                insertStyledText(doc, ",\n", valueStyle);
            } else {
                insertStyledText(doc, "\n", valueStyle);
            }
        }
        insertStyledText(doc, spaces(indent) + "]\n", valueStyle);
    }

    private static void insertJsonValue(StyledDocument doc, Object v, Style keyStyle, Style valueStyle, int indent) {
        if (v instanceof JSONObject) {
            insertJsonObject(doc, (JSONObject) v, keyStyle, valueStyle, indent);
        } else if (v instanceof JSONArray) {
            insertJsonArray(doc, (JSONArray) v, keyStyle, valueStyle, indent);
        } else if (v instanceof String) {
            insertStyledText(doc, "\"" + v.toString() + "\"", valueStyle);
        } else {
            // For numbers, booleans, null
            insertStyledText(doc, v.toString(), valueStyle);
        }
    }

    private static String spaces(int count) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < count; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

}
