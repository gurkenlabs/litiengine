package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.utiliti.controller.LogHandler;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JTextPane;
import org.junit.jupiter.api.Test;

class ConsoleActionPanelTest {

  @Test
  void testConsoleActionPanelButtonsHaveDisabledIcons() {
    LogHandler logHandler = new LogHandler(new JTextPane());
    ConsoleActionPanel panel = new ConsoleActionPanel(logHandler);

    for (Component comp : panel.getComponents()) {
      if (comp instanceof JButton button) {
        assertNotNull(button.getIcon(), "Console button must have an icon");
        assertNotNull(button.getDisabledIcon(), "Console button must have a disabled icon");
        assertTrue(button.getDisabledIcon() instanceof Style.DisabledVectorIcon,
            "Console button must use Style.DisabledVectorIcon for proper disabled rendering");
      }
    }
  }
}
