package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.utiliti.model.Icons;
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
    entityScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.CREATURE_ENTITY));

    JMenuItem gameScript = new JMenuItem("Game Script...");
    gameScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.GAME_LOGIC));

    JMenuItem behaviorScript = new JMenuItem("Behavior Script...");
    behaviorScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.BEHAVIOR_CONTROLLER));

    JMenuItem abilityScript = new JMenuItem("Ability Script...");
    abilityScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ABILITY));

    newSub.add(entityScript);
    newSub.add(gameScript);
    newSub.add(behaviorScript);
    newSub.add(abilityScript);

    JMenuItem save = new JMenuItem("Save", Icons.SAVE_16);
    save.addActionListener(e -> UI.getScriptWorkspacePanel().saveActive());

    JMenuItem compileReload = new JMenuItem("Compile & reload", Icons.REWIND_16);
    compileReload.addActionListener(e -> UI.getScriptWorkspacePanel().reloadActive());

    JMenuItem reloadDisk = new JMenuItem("Reload from disk", Icons.REWIND_16);
    reloadDisk.addActionListener(e -> UI.getScriptWorkspacePanel().reloadActiveFromDisk());

    JMenuItem openIde = new JMenuItem("Open in IDE", Icons.EXTERNAL_16);
    openIde.addActionListener(e -> UI.getScriptWorkspacePanel().openActiveExternally());

    JMenuItem configureIntelliJ = new JMenuItem("Configure IntelliJ");
    configureIntelliJ.addActionListener(e -> UI.getScriptWorkspacePanel().configureProjectForIntellij());

    this.add(newSub);
    this.addSeparator();
    this.add(save);
    this.add(compileReload);
    this.add(reloadDisk);
    this.addSeparator();
    this.add(openIde);
    this.add(configureIntelliJ);
  }
}
