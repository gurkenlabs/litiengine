package de.gurkenlabs.utiliti.swing;

import com.github.weisj.darklaf.components.text.NumberedTextComponent;
import java.util.logging.Logger;
import javax.swing.JTextPane;

@SuppressWarnings("serial")
public class ConsolePanel extends NumberedTextComponent {

  public ConsolePanel() {
    super(new JTextPane());
    this.textComponent.setEditable(false);
    Logger.getLogger("").addHandler(new LogHandler((JTextPane) this.textComponent));
  }
}
