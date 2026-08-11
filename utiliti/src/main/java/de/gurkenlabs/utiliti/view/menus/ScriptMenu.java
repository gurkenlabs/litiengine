package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.model.KeyBindings.Command;
import de.gurkenlabs.utiliti.view.components.ScriptWorkspacePanel.ScriptKind;
import de.gurkenlabs.utiliti.view.components.UI;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ScriptMenu extends JMenu {
  public ScriptMenu() {
    super("Script");

    JMenu newSub = new JMenu("New Script");
    newSub.setIcon(Icons.ADD_16);

    JMenuItem entityScript = new JMenuItem("Entity Script...");
    entityScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ENTITY));

    JMenuItem gameScript = new JMenuItem("Game Script...");
    gameScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.GAME));

    JMenuItem envScript = new JMenuItem("Environment Script...");
    envScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ENVIRONMENT));

    newSub.add(entityScript);
    newSub.add(gameScript);
    newSub.add(envScript);

    JMenuItem save = new JMenuItem("Save", Icons.SAVE_16);
    save.addActionListener(e -> UI.getScriptWorkspacePanel().saveActive());
    KeyBindings.bind(save, Command.SCRIPT_SAVE);

    JMenuItem formatCode = new JMenuItem("Format code", Icons.FORMAT_CODE_16);
    formatCode.addActionListener(e -> UI.getScriptWorkspacePanel().formatActive());
    KeyBindings.bind(formatCode, Command.SCRIPT_FORMAT);

    JMenuItem compile = new JMenuItem("Compile", Icons.COMPILE_16);
    compile.addActionListener(e -> UI.getScriptWorkspacePanel().reloadActive());
    KeyBindings.bind(compile, Command.SCRIPT_COMPILE);

    JMenuItem reload = new JMenuItem("Reload", Icons.RELOAD_16);
    reload.addActionListener(e -> UI.getScriptWorkspacePanel().reloadActiveFromDisk());
    KeyBindings.bind(reload, Command.SCRIPT_RELOAD);

    JMenuItem openIde = new JMenuItem("Open in IDE", Icons.EXTERNAL_16);
    openIde.addActionListener(e -> UI.getScriptWorkspacePanel().openActiveExternally());
    KeyBindings.bind(openIde, Command.SCRIPT_OPEN_IDE);

    JMenuItem runProject = new JMenuItem("Run Project", Icons.PLAY_16);
    runProject.addActionListener(e -> UI.getScriptWorkspacePanel().runProject());

    JMenuItem stopProject = new JMenuItem("Stop Project", Icons.POWER_16);
    stopProject.addActionListener(e -> UI.getScriptWorkspacePanel().stopProject());

    JMenuItem restartProject = new JMenuItem("Restart Project", Icons.RELOAD_16);
    restartProject.addActionListener(e -> UI.getScriptWorkspacePanel().restartProject());

    this.add(newSub);
    this.addSeparator();
    this.add(save);
    this.add(formatCode);
    this.add(compile);
    this.add(reload);
    this.addSeparator();
    this.add(openIde);
    this.addSeparator();
    this.add(runProject);
    this.add(stopProject);
    this.add(restartProject);
  }
}
