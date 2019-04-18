package de.gurkenlabs.utiliti.swing.menus;

import javax.swing.JMenuBar;

@SuppressWarnings("serial")
public class MainMenuBar extends JMenuBar {
  public MainMenuBar() {
    this.add(new FileMenu());
    this.add(new EditMenu());
    this.add(new ViewMenu());
    this.add(new ResourcesMenu());
    this.add(new MapMenu());
    this.add(new HelpMenu());
  }
}
