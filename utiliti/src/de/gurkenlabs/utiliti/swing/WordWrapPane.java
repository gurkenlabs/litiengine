package de.gurkenlabs.utiliti.swing;

import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;

public class WordWrapPane extends JTextPane {

    private final int rows;
    private final int columns;

    public WordWrapPane(int rows, int columns) {
        super();
        this.setEditorKit(new WrapEditorKit());
        this.rows = rows;
        this.columns = columns;
    }

    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        return new Dimension(columns * getColumnWidth() + insets.left + insets.right, rows * getRowHeight() + insets.top + insets.bottom);
    }

    private class WrapEditorKit extends StyledEditorKit {
        ViewFactory defaultFactory = new WrapColumnFactory();

        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
    }

    private class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            switch (elem.getName()) {
                case AbstractDocument.ContentElementName:
                    return new WrapLabelView(elem);
                case AbstractDocument.ParagraphElementName:
                    return new ParagraphView(elem);
                case AbstractDocument.SectionElementName:
                    return new BoxView(elem, View.Y_AXIS);
                case StyleConstants.ComponentElementName:
                    return new ComponentView(elem);
                case StyleConstants.IconElementName:
                    return new IconView(elem);
                default:
                    return new LabelView(elem);
            }
        }
    }

    private class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

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

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getRowHeight() {
        FontMetrics metrics = getFontMetrics(getFont());
        return metrics.getHeight();
    }

    public int getColumnWidth() {
        FontMetrics metrics = getFontMetrics(getFont());
        return metrics.charWidth('m');
    }
}
