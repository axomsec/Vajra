package httphighlighter;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class HttpRequestResponseHighLighter {

    private Style requestLineStyle;
    private Style headerStyle;
    private Style bodyStyle;
    private Style defaultStyle;

    /**
     * Initialize the highlighter with predefined styles.
     */
    public void HttpRequestHighlighter() {
        StyleContext sc = new StyleContext();

        // Default style for resetting
        defaultStyle = sc.addStyle("Default", null);
        StyleConstants.setForeground(defaultStyle, Color.BLACK);
        StyleConstants.setFontFamily(defaultStyle, "Monospaced");

        // Request line style (e.g., "POST /exchange/member/playerService... HTTP/1.1")
        requestLineStyle = sc.addStyle("RequestLine", null);
        StyleConstants.setForeground(requestLineStyle, new Color(0, 0, 160)); // dark-ish blue
        StyleConstants.setBold(requestLineStyle, true);

        // Header style (e.g., "Host: example.com", "User-Agent: ...")
        headerStyle = sc.addStyle("Header", null);
        StyleConstants.setForeground(headerStyle, new Color(128, 0, 0)); // dark red

        // Body style (for the POST body or query strings)
        bodyStyle = sc.addStyle("Body", null);
        StyleConstants.setForeground(bodyStyle, new Color(0, 128, 0)); // dark green
    }

    public HttpRequestResponseHighLighter(Style requestLineStyle, Style headerStyle, Style bodyStyle, Style defaultStyle) {
        this.requestLineStyle = requestLineStyle;
        this.headerStyle = headerStyle;
        this.bodyStyle = bodyStyle;
        this.defaultStyle = defaultStyle;
    }

    /**
     * Apply syntax highlighting to the given JEditorPane text.
     */
    public void highlight(JEditorPane editorPane) {
        // Ensure the editorPane uses a StyledDocument
        if (!(editorPane.getDocument() instanceof StyledDocument)) {
            return;
        }

        StyledDocument doc = (StyledDocument) editorPane.getDocument();

        try {
            // Retrieve all text
            String text = doc.getText(0, doc.getLength());

            // Clear any existing styles
            doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);

            // Split text into lines
            String[] lines = text.split("\r?\n");
            int offset = 0;

            for (String line : lines) {
                // Decide which style to apply
                Style styleToApply = determineStyle(line);

                // Apply that style to this line
                doc.setCharacterAttributes(offset, line.length(), styleToApply, true);

                // Advance offset by the line length + 1 (for newline)
                offset += line.length() + 1;
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Determine which style to use based on a line's content.
     */
    private Style determineStyle(String line) {
        // Very simple detection logic:
        // 1) Request Line: starts with a known HTTP method and has HTTP/x.x at the end
        if (line.matches("^(GET|POST|PUT|DELETE|PATCH|OPTIONS|HEAD)\\s.*HTTP/\\d\\.\\d$")) {
            return requestLineStyle;
        }
        // 2) Header: SomeHeaderName: SomeValue
        else if (line.matches("^[A-Za-z0-9-]+:.*")) {
            return headerStyle;
        }
        // 3) Otherwise treat as body or unknown
        else {
            return bodyStyle;
        }
    }
}
