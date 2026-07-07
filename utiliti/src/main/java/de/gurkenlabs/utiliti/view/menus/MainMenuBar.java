package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.utiliti.model.Style;
import javax.swing.BorderFactory;
import javax.swing.JMenuBar;

public class MainMenuBar extends JMenuBar {
  public MainMenuBar() {
    this.setOpaque(true);
    this.setBackground(Style.COLOR_BG);
    this.setForeground(Style.COLOR_TEXT);
    this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.COLOR_BORDER));
    this.add(new FileMenu());
    this.add(new EditMenu());
    this.add(new ViewMenu());
    this.add(new ResourcesMenu());
    this.add(new MapMenu());
    this.add(new HelpMenu());
  }
}
