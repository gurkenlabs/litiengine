package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.components.UI;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class RunMenu extends JMenu {
  public RunMenu() {
    super("Run");

    JMenuItem runProject = new JMenuItem("Run Project", Icons.PLAY_16);
    runProject.addActionListener(e -> UI.getScriptWorkspacePanel().runProject());

    JMenuItem stopProject = new JMenuItem("Stop Project", Icons.POWER_16);
    stopProject.addActionListener(e -> UI.getScriptWorkspacePanel().stopProject());

    JMenuItem restartProject = new JMenuItem("Restart Project", Icons.RELOAD_16);
    restartProject.addActionListener(e -> UI.getScriptWorkspacePanel().restartProject());

    this.add(runProject);
    this.add(stopProject);
    this.add(restartProject);
  }
}
