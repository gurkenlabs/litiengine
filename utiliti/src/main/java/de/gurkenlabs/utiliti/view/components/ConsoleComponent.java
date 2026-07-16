package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.LayoutManager;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class ConsoleComponent extends JPanel {

  public ConsoleComponent() {
    super();
    LayoutManager layout = new BoxLayout(this, BoxLayout.X_AXIS);
    this.setLayout(layout);
    this.setBackground(Style.assetExplorerBackground());

    ConsolePanel consolePanel = new ConsolePanel();

    this.add(new ConsoleActionPanel());
    this.add(consolePanel);
  }
}
