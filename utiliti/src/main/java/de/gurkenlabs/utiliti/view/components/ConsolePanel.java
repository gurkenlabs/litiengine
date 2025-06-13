package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.components.text.NumberedTextComponent;
import de.gurkenlabs.utiliti.controller.LogHandler;
import java.util.logging.Logger;
import javax.swing.JTextPane;

public class ConsolePanel extends NumberedTextComponent {

  public ConsolePanel() {
    super(new JTextPane());
    this.textComponent.setEditable(false);
    Logger.getLogger("").addHandler(new LogHandler((JTextPane) this.textComponent));
  }
}
