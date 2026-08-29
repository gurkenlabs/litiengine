package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.controller.LogHandler;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import javax.swing.JPanel;

public class ConsoleComponent extends JPanel {
  private final LogHandler logHandler;
  private final ConsolePanel consolePanel;

  public ConsoleComponent() {
    super(new BorderLayout());
    this.setBackground(Style.assetExplorerBackground());

    this.consolePanel = new ConsolePanel();
    this.logHandler = this.consolePanel.getLogHandler();
    this.add(this.consolePanel, BorderLayout.CENTER);
  }

  public LogHandler getLogHandler() {
    return this.logHandler;
  }

  public ConsolePanel getConsolePanel() {
    return this.consolePanel;
  }
}
