package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.*;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import java.awt.Color;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
public class JavaCodeSnippetPaneTest {

  @Test
  void testCodeSnippetHighlightsTokensCorrectly() {
    JavaCodeSnippetPane pane = new JavaCodeSnippetPane();
    String code = """
        // Entity behavior
        @Override
        public void onLoaded() {
          spawnProjectile()
            .speed(400)
            .message("Boom!");
          Creature target = null;
        }""";

    pane.setCode(code);
    StyledDocument doc = pane.getStyledDocument();
    assertEquals(code.replace("\r\n", "\n"), pane.getText().replace("\r\n", "\n"));

    // 1. Comment "// Entity behavior"
    int commentIdx = code.indexOf("// Entity behavior");
    AttributeSet commentAttr = doc.getCharacterElement(commentIdx).getAttributes();
    assertEquals(JavaCodeSnippetPane.COLOR_COMMENT, StyleConstants.getForeground(commentAttr));
    assertTrue(StyleConstants.isItalic(commentAttr));

    // 2. Annotation "@Override"
    int annotationIdx = code.indexOf("@Override");
    AttributeSet annotationAttr = doc.getCharacterElement(annotationIdx).getAttributes();
    assertEquals(JavaCodeSnippetPane.COLOR_ANNOTATION, StyleConstants.getForeground(annotationAttr));

    // 3. Keyword "public"
    int publicIdx = code.indexOf("public");
    AttributeSet publicAttr = doc.getCharacterElement(publicIdx).getAttributes();
    assertEquals(JavaCodeSnippetPane.COLOR_KEYWORD, StyleConstants.getForeground(publicAttr));
    assertTrue(StyleConstants.isBold(publicAttr));

    // 4. Method call "spawnProjectile"
    int methodIdx = code.indexOf("spawnProjectile");
    AttributeSet methodAttr = doc.getCharacterElement(methodIdx).getAttributes();
    assertEquals(JavaCodeSnippetPane.COLOR_METHOD, StyleConstants.getForeground(methodAttr));

    // 5. Number "400"
    int numIdx = code.indexOf("400");
    AttributeSet numAttr = doc.getCharacterElement(numIdx).getAttributes();
    assertEquals(JavaCodeSnippetPane.COLOR_NUMBER, StyleConstants.getForeground(numAttr));

    // 6. String "\"Boom!\""
    int strIdx = code.indexOf("\"Boom!\"");
    AttributeSet strAttr = doc.getCharacterElement(strIdx).getAttributes();
    assertEquals(JavaCodeSnippetPane.COLOR_STRING, StyleConstants.getForeground(strAttr));

    // 7. Type "Creature"
    int typeIdx = code.indexOf("Creature");
    AttributeSet typeAttr = doc.getCharacterElement(typeIdx).getAttributes();
    assertEquals(JavaCodeSnippetPane.COLOR_TYPE, StyleConstants.getForeground(typeAttr));

    // 8. Keyword "null"
    int nullIdx = code.indexOf("null");
    AttributeSet nullAttr = doc.getCharacterElement(nullIdx).getAttributes();
    assertEquals(JavaCodeSnippetPane.COLOR_KEYWORD, StyleConstants.getForeground(nullAttr));
  }

  @Test
  void testNullOrEmptyCodeHandledGracefully() {
    JavaCodeSnippetPane pane = new JavaCodeSnippetPane();
    assertDoesNotThrow(() -> pane.setCode(null));
    assertEquals("", pane.getText());

    assertDoesNotThrow(() -> pane.setCode(""));
    assertEquals("", pane.getText());
  }
}
