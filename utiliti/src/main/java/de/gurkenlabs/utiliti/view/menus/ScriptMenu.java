package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.model.KeyBindings.Command;
import de.gurkenlabs.utiliti.view.components.ScriptWorkspacePanel.ScriptKind;
import de.gurkenlabs.utiliti.view.components.UI;
import de.gurkenlabs.utiliti.view.dialogs.GameScriptsDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ScriptMenu extends JMenu {
  public ScriptMenu() {
    super("Script");

    JMenu newSub = new JMenu("New Script");
    newSub.setIcon(Icons.ADD_16);

    JMenu entitySub = new JMenu("Entity Script");

    JMenuItem creatureScript = new JMenuItem("Creature Script...");
    creatureScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ENTITY, de.gurkenlabs.litiengine.entities.Creature.class));

    JMenuItem propScript = new JMenuItem("Prop Script...");
    propScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ENTITY, de.gurkenlabs.litiengine.entities.Prop.class));

    JMenuItem triggerScript = new JMenuItem("Trigger Script...");
    triggerScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ENTITY, de.gurkenlabs.litiengine.entities.Trigger.class));

    JMenuItem emitterScript = new JMenuItem("Emitter Script...");
    emitterScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ENTITY, de.gurkenlabs.litiengine.graphics.emitters.Emitter.class));

    JMenuItem collisionBoxScript = new JMenuItem("CollisionBox Script...");
    collisionBoxScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ENTITY, de.gurkenlabs.litiengine.entities.CollisionBox.class));

    JMenuItem lightSourceScript = new JMenuItem("LightSource Script...");
    lightSourceScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ENTITY, de.gurkenlabs.litiengine.entities.LightSource.class));

    JMenuItem genericEntityScript = new JMenuItem("Generic Entity Script...");
    genericEntityScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ENTITY, de.gurkenlabs.litiengine.entities.IEntity.class));

    entitySub.add(creatureScript);
    entitySub.add(propScript);
    entitySub.add(triggerScript);
    entitySub.add(emitterScript);
    entitySub.add(collisionBoxScript);
    entitySub.add(lightSourceScript);
    entitySub.add(genericEntityScript);

    JMenuItem gameScript = new JMenuItem("Game Script...");
    gameScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.GAME));

    JMenuItem envScript = new JMenuItem("Environment Script...");
    envScript.addActionListener(e -> UI.getScriptWorkspacePanel().createScript(ScriptKind.ENVIRONMENT));

    newSub.add(entitySub);
    newSub.add(gameScript);
    newSub.add(envScript);

    JMenuItem save = new JMenuItem("Save", Icons.SAVE_16);
    save.addActionListener(e -> UI.getScriptWorkspacePanel().saveActive());
    KeyBindings.bind(save, Command.SCRIPT_SAVE);

    JMenuItem formatCode = new JMenuItem("Format code", Icons.FORMAT_CODE_16);
    formatCode.addActionListener(e -> UI.getScriptWorkspacePanel().formatActive());
    KeyBindings.bind(formatCode, Command.SCRIPT_FORMAT);

    JMenuItem build = new JMenuItem("Build", Icons.COMPILE_16);
    build.addActionListener(e -> UI.getScriptWorkspacePanel().build());
    KeyBindings.bind(build, Command.SCRIPT_COMPILE);

    JMenuItem reload = new JMenuItem("Reload", Icons.RELOAD_16);
    reload.addActionListener(e -> UI.getScriptWorkspacePanel().reloadActiveFromDisk());
    KeyBindings.bind(reload, Command.SCRIPT_RELOAD);

    JMenuItem exploreEvents = new JMenuItem("Script Events & API Explorer...", Icons.API_16);
    exploreEvents.addActionListener(e -> de.gurkenlabs.utiliti.view.dialogs.ScriptEventExplorerDialog.showDialog());

    JMenuItem configGameScripts = new JMenuItem("Configure Game Scripts...", Icons.SETTINGS_16);
    configGameScripts.addActionListener(e -> GameScriptsDialog.showDialog());

    JMenuItem guideItem = new JMenuItem("Scripting Guide & Getting Started...", Icons.DOCUMENTATION_16);
    guideItem.addActionListener(e -> de.gurkenlabs.utiliti.view.dialogs.ScriptEventExplorerDialog.showGuide());

    this.add(newSub);
    this.add(configGameScripts);
    this.add(exploreEvents);
    this.add(guideItem);
    this.addSeparator();
    this.add(save);
    this.add(formatCode);
    this.add(build);
    this.add(reload);
  }
}
