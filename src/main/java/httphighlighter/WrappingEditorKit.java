package httphighlighter;

import javax.swing.text.*;
import java.awt.*;


public class WrappingEditorKit extends StyledEditorKit {
    private ViewFactory defaultFactory = new WrappingViewFactory();

    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

    static class WrappingViewFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new WrapLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            // Default to text display
            return new LabelView(elem);
        }
    }

    static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }
    }
}