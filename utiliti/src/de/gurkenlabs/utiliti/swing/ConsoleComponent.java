package de.gurkenlabs.utiliti.swing;

import java.util.logging.Logger;

import com.github.weisj.darklaf.components.text.NumberedTextComponent;

import javax.swing.*;

@SuppressWarnings("serial")
public class ConsoleComponent extends NumberedTextComponent {

  public ConsoleComponent() {
    super(new JTextPane());
    this.textComponent.setEditable(false);
    Logger.getLogger("").addHandler(new LogHandler((JTextPane) this.textComponent));
  }
}
