package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class MainMenuBar extends JMenuBar {
  private final MapMenu mapMenu;
  private final ScriptMenu scriptMenu;
  private final javax.swing.JButton btnRun;
  private final javax.swing.JButton btnStop;

  public MainMenuBar() {
    this.setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
    this.setOpaque(true);
    this.setBackground(Style.COLOR_BG);
    this.setForeground(Style.COLOR_TEXT);
    this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    this.add(new FileMenu());
    this.add(new EditMenu());
    this.add(new ViewMenu());
    this.add(new ResourcesMenu());
    this.mapMenu = new MapMenu();
    this.scriptMenu = new ScriptMenu();
    this.scriptMenu.setVisible(false);
    this.add(this.mapMenu);
    this.add(this.scriptMenu);
    this.add(new RunMenu());
    this.add(new HelpMenu());

    this.add(javax.swing.Box.createHorizontalGlue());

    javax.swing.JPanel runControls = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 4, 0));
    runControls.setOpaque(false);

    boolean initialHasProject = de.gurkenlabs.utiliti.controller.Editor.instance().getProjectPath() != null;

    this.btnRun = new javax.swing.JButton(Icons.GREEN_PLAY_16);
    Style.styleButton(this.btnRun, Style.ButtonVariant.TOOLBAR);
    this.btnRun.setToolTipText("Run Project (Shift+F10)");
    this.btnRun.setPreferredSize(new java.awt.Dimension(28, 22));
    this.btnRun.setEnabled(initialHasProject);
    this.btnRun.addActionListener(e -> {
      if (de.gurkenlabs.utiliti.view.components.UI.getScriptWorkspacePanel() != null) {
        de.gurkenlabs.utiliti.view.components.UI.getScriptWorkspacePanel().runProject();
      }
    });

    this.btnStop = new javax.swing.JButton(Icons.RED_STOP_16);
    Style.styleButton(this.btnStop, Style.ButtonVariant.TOOLBAR);
    this.btnStop.setToolTipText("Stop Project (Ctrl+F2)");
    this.btnStop.setPreferredSize(new java.awt.Dimension(28, 22));
    this.btnStop.setEnabled(false);
    this.btnStop.addActionListener(e -> {
      if (de.gurkenlabs.utiliti.view.components.UI.getScriptWorkspacePanel() != null) {
        de.gurkenlabs.utiliti.view.components.UI.getScriptWorkspacePanel().stopProject();
      }
    });

    runControls.add(this.btnRun);
    runControls.add(this.btnStop);
    this.add(runControls);

    styleTopLevelMenus();
  }

  public void updateRunState(boolean hasProject, boolean isRunning) {
    this.btnRun.setEnabled(hasProject);
    this.btnRun.setToolTipText(isRunning ? "Rerun Project (Shift+F10)" : "Run Project (Shift+F10)");
    this.btnStop.setEnabled(isRunning);
  }

  public void setScriptMode(boolean scriptMode) {
    this.mapMenu.setVisible(!scriptMode);
    this.scriptMenu.setVisible(scriptMode);
    this.updateAccelerators(!scriptMode);
  }

  private void updateAccelerators(boolean enabled) {
    for (Component component : getComponents()) {
      if (component instanceof JMenu menu && !(menu instanceof ScriptMenu || menu instanceof FileMenu || menu instanceof HelpMenu)) {
        setAcceleratorsEnabled(menu, enabled);
      }
    }
  }

  private static void setAcceleratorsEnabled(JMenu menu, boolean enabled) {
    for (int index = 0; index < menu.getItemCount(); index++) {
      javax.swing.JMenuItem item = menu.getItem(index);
      if (item != null) {
        if (item instanceof JMenu subMenu) {
          setAcceleratorsEnabled(subMenu, enabled);
        } else if (item.getClientProperty("utiliti.keyBindingAction") instanceof de.gurkenlabs.utiliti.model.KeyBindings.Command command) {
          item.setAccelerator(enabled ? de.gurkenlabs.utiliti.model.KeyBindings.get(command) : null);
        }
      }
    }
  }

  private void styleTopLevelMenus() {
    for (Component component : getComponents()) {
      if (component instanceof JMenu menu) {
        menu.setFont(menu.getFont().deriveFont(13f));
        menu.setOpaque(false);
        menu.setMargin(new Insets(6, 12, 6, 12));
        menu.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
      }
    }
  }
}
