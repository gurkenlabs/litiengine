package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.model.KeyBindings.Command;
import de.gurkenlabs.utiliti.view.components.UI;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class RunMenu extends JMenu {
  public RunMenu() {
    super("Run");

    JMenuItem runProject = new JMenuItem("Run Project", Icons.PLAY_16);
    KeyBindings.bind(runProject, Command.RUN_PROJECT);
    runProject.addActionListener(e -> UI.getScriptWorkspacePanel().runProject());

    JMenuItem debugProject = new JMenuItem("Debug Project", Icons.BUG_16);
    KeyBindings.bind(debugProject, Command.DEBUG_PROJECT);
    debugProject.addActionListener(e -> UI.getScriptWorkspacePanel().debugProject());

    JMenuItem stopProject = new JMenuItem("Stop Project", Icons.POWER_16);
    KeyBindings.bind(stopProject, Command.STOP_PROJECT);
    stopProject.addActionListener(e -> UI.getScriptWorkspacePanel().stopProject());

    JMenuItem restartProject = new JMenuItem("Restart Project", Icons.RELOAD_16);
    restartProject.addActionListener(e -> UI.getScriptWorkspacePanel().restartProject());

    this.add(runProject);
    this.add(debugProject);
    this.add(stopProject);
    this.add(restartProject);
  }
}
