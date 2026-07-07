package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class MainMenuBar extends JMenuBar {
  public MainMenuBar() {
    this.setOpaque(true);
    this.setBackground(Style.COLOR_BG);
    this.setForeground(Style.COLOR_TEXT);
    this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    this.add(new FileMenu());
    this.add(new EditMenu());
    this.add(new ViewMenu());
    this.add(new ResourcesMenu());
    this.add(new MapMenu());
    this.add(new HelpMenu());
    styleTopLevelMenus();
  }

  private void styleTopLevelMenus() {
    for (Component component : getComponents()) {
      if (component instanceof JMenu menu) {
        menu.setFont(menu.getFont().deriveFont(13f));
        menu.setMargin(new Insets(6, 12, 6, 12));
        menu.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
      }
    }
  }
}
