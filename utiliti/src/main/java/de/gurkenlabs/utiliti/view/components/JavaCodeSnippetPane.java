package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * A syntax-highlighted code viewer pane styled with the Tokyo Night syntax palette.
 */
public class JavaCodeSnippetPane extends JTextPane {

  // Tokyo Night syntax token palette
  public static final Color COLOR_KEYWORD = new Color(187, 154, 247); // #bb9af7 (purple)
  public static final Color COLOR_TYPE = new Color(42, 195, 222);    // #2ac3de (cyan)
  public static final Color COLOR_METHOD = new Color(122, 162, 247);  // #7aa2f7 (blue)
  public static final Color COLOR_STRING = new Color(158, 206, 106);  // #9ece6a (green)
  public static final Color COLOR_NUMBER = new Color(255, 158, 100);  // #ff9e64 (orange)
  public static final Color COLOR_COMMENT = new Color(108, 118, 166); // #6c76a6 (muted slate/blue)
  public static final Color COLOR_ANNOTATION = new Color(224, 175, 104); // #e0af68 (gold)
  public static final Color COLOR_DEFAULT = new Color(200, 208, 245); // #c8d0f5 (light text)
  public static final Color COLOR_BG = new Color(22, 22, 28);        // #16161c
  public static final Color COLOR_BORDER = new Color(48, 48, 58);    // #30303a

  private static final Pattern TOKEN_PATTERN = Pattern.compile(
      "(?<COMMENT>//[^\n]*|/\\*.*?\\*/)"
      + "|(?<STRING>\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*')"
      + "|(?<ANNOTATION>@[A-Za-z_$][\\w$]*)"
      + "|(?<KEYWORD>\\b(?:public|protected|private|static|final|abstract|class|interface|enum|record|extends|implements|new|return|if|else|switch|case|default|for|while|do|break|continue|try|catch|finally|throw|throws|this|super|instanceof|import|package|var|void|int|boolean|double|float|long|short|byte|char|true|false|null)\\b)"
      + "|(?<NUMBER>\\b0x[0-9a-fA-F]+\\b|\\b\\d+(?:\\.\\d+)?[fFdDlL]?\\b)"
      + "|(?<METHOD>\\b[A-Za-z_$][\\w$]*(?=\\s*\\())"
      + "|(?<TYPE>\\b[A-Z][A-Za-z0-9_$]*\\b)",
      Pattern.DOTALL
  );

  private final SimpleAttributeSet defaultStyle;
  private final SimpleAttributeSet keywordStyle;
  private final SimpleAttributeSet typeStyle;
  private final SimpleAttributeSet methodStyle;
  private final SimpleAttributeSet stringStyle;
  private final SimpleAttributeSet numberStyle;
  private final SimpleAttributeSet commentStyle;
  private final SimpleAttributeSet annotationStyle;

  public JavaCodeSnippetPane() {
    this.setEditable(false);
    this.setFont(new Font(Style.FONTNAME_CONSOLE, Font.PLAIN, 12));
    this.setBackground(COLOR_BG);
    this.setForeground(COLOR_DEFAULT);
    this.setCaretColor(Color.WHITE);
    this.setSelectionColor(new Color(51, 65, 105));
    this.setSelectedTextColor(Color.WHITE);
    this.setMargin(new Insets(8, 10, 8, 10));
    this.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(COLOR_BORDER),
        BorderFactory.createEmptyBorder(6, 8, 6, 8)));

    String fontFam = Style.FONTNAME_CONSOLE;
    int fontSize = 12;

    this.defaultStyle = createStyle(COLOR_DEFAULT, false, false, fontFam, fontSize);
    this.keywordStyle = createStyle(COLOR_KEYWORD, true, false, fontFam, fontSize);
    this.typeStyle = createStyle(COLOR_TYPE, false, false, fontFam, fontSize);
    this.methodStyle = createStyle(COLOR_METHOD, false, false, fontFam, fontSize);
    this.stringStyle = createStyle(COLOR_STRING, false, false, fontFam, fontSize);
    this.numberStyle = createStyle(COLOR_NUMBER, false, false, fontFam, fontSize);
    this.commentStyle = createStyle(COLOR_COMMENT, false, true, fontFam, fontSize);
    this.annotationStyle = createStyle(COLOR_ANNOTATION, true, false, fontFam, fontSize);
  }

  private static SimpleAttributeSet createStyle(Color color, boolean bold, boolean italic, String fontFamily, int size) {
    SimpleAttributeSet set = new SimpleAttributeSet();
    StyleConstants.setForeground(set, color);
    StyleConstants.setBold(set, bold);
    StyleConstants.setItalic(set, italic);
    StyleConstants.setFontFamily(set, fontFamily);
    StyleConstants.setFontSize(set, size);
    return set;
  }

  public void setCode(String code) {
    if (code == null) {
      this.setText("");
      return;
    }
    String normalized = code.replace("\r\n", "\n");

    DefaultStyledDocument doc = new DefaultStyledDocument();
    try {
      doc.insertString(0, normalized, this.defaultStyle);
      Matcher matcher = TOKEN_PATTERN.matcher(normalized);
      while (matcher.find()) {
        int start = matcher.start();
        int len = matcher.end() - start;
        if (matcher.group("COMMENT") != null) {
          doc.setCharacterAttributes(start, len, this.commentStyle, false);
        } else if (matcher.group("STRING") != null) {
          doc.setCharacterAttributes(start, len, this.stringStyle, false);
        } else if (matcher.group("ANNOTATION") != null) {
          doc.setCharacterAttributes(start, len, this.annotationStyle, false);
        } else if (matcher.group("KEYWORD") != null) {
          doc.setCharacterAttributes(start, len, this.keywordStyle, false);
        } else if (matcher.group("NUMBER") != null) {
          doc.setCharacterAttributes(start, len, this.numberStyle, false);
        } else if (matcher.group("METHOD") != null) {
          doc.setCharacterAttributes(start, len, this.methodStyle, false);
        } else if (matcher.group("TYPE") != null) {
          doc.setCharacterAttributes(start, len, this.typeStyle, false);
        }
      }
    } catch (BadLocationException e) {
      this.setText(code);
      return;
    }

    this.setStyledDocument(doc);
    this.setCaretPosition(0);
  }
}
