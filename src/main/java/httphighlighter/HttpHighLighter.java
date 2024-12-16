package httphighlighter;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import org.json.*; // If you have a JSON library. If not, you can do rudimentary parsing yourself.

public class HttpHighLighter {
    /**
     * Creates a styled JTextPane containing the highlighted HTTP request.
     *
     * @param httpRequest The full HTTP request as a string.
     * @return A JTextPane component with syntax highlighting applied.
     */
//    public static String createStyledHttpView(String httpRequest, JTextPane textPane) {
//        StyledDocument doc = textPane.getStyledDocument();
//
//        // Clear the document before adding the new request
//        try {
//            doc.remove(0, doc.getLength());
//        } catch (BadLocationException e) {
//            e.printStackTrace();
//        }
//
//        // Define styles
//        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
//
//        // Request line style
//        Style requestLineStyle = doc.addStyle("requestLine", defaultStyle);
//        StyleConstants.setBold(requestLineStyle, false);
//        StyleConstants.setForeground(requestLineStyle, new Color(0, 0, 0)); // teal
//
//        // Header key style
//        Style headerKeyStyle = doc.addStyle("headerKey", defaultStyle);
//        StyleConstants.setBold(headerKeyStyle, false);
//        StyleConstants.setForeground(headerKeyStyle, new Color(2, 135, 234)); // purple
//
//        // Header value style
//        Style headerValueStyle = doc.addStyle("headerValue", defaultStyle);
//        StyleConstants.setForeground(headerValueStyle, new Color(0, 0, 0)); // dark green
//
//        // Body key style
//        Style bodyKeyStyle = doc.addStyle("bodyKey", defaultStyle);
//        StyleConstants.setForeground(bodyKeyStyle, Color.BLUE);
//
//        // Body value style
//        Style bodyValueStyle = doc.addStyle("bodyValue", defaultStyle);
//        StyleConstants.setForeground(bodyValueStyle, Color.GREEN.darker());
//
//        // Parse the request into components
//        String[] lines = httpRequest.split("\r\n|\n");
//        String requestLine = (lines.length > 0) ? lines[0] : "";
//
//        // Insert the request line
//        insertStyledText(doc, requestLine + "\n", requestLineStyle);
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
//
//        return httpRequest;
//    }

    public static String createStyledHttpView(String httpRequest, JTextPane textPane) {

        textPane.setEditorKit(new WrapEditorKit());

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
        StyleConstants.setForeground(bodyKeyStyle, Color.BLUE);

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


    // Custom EditorKit that wraps text
    static class WrapEditorKit extends StyledEditorKit {
        private ViewFactory defaultFactory = new WrapColumnFactory();

        @Override
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
    }

    // A factory that creates wrap views
    static class WrapColumnFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new WrapLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem) {
                        @Override
                        public void layout(int width, int height) {
                            super.layout(Integer.MAX_VALUE, height);
                        }

                        @Override
                        public float getMinimumSpan(int axis) {
                            return super.getPreferredSpan(axis);
                        }
                    };
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }

    // A label view that wraps lines
    static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                default:
                    return super.getMinimumSpan(axis);
            }
        }
    }
}
