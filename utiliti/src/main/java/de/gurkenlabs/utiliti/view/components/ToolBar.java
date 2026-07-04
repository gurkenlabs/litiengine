package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.controller.tool.Tool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class ToolBar extends JToolBar {
  public ToolBar() {
    super("Tools");
    setFloatable(false);
    setMargin(new Insets(2, 4, 2, 4));
    setFont(Style.getDefaultFont());

    ButtonGroup group = new ButtonGroup();

    for (Tool tool : ToolManager.instance().getTools()) {
      JToggleButton btn = new JToggleButton(tool.getIcon());
      btn.setToolTipText(tool.getName());
      btn.setSelected(tool.equals(ToolManager.instance().getActiveTool()));
      btn.addActionListener(e -> ToolManager.instance().setActiveTool(tool));
      group.add(btn);
      add(btn);
    }

    ToolManager.instance().addListener(this::updateButtonSelection);
  }

  private void updateButtonSelection() {
    Tool active = ToolManager.instance().getActiveTool();
    for (java.awt.Component comp : getComponents()) {
      if (comp instanceof JToggleButton btn) {
        int index = -1;
        for (int i = 0; i < getComponentCount(); i++) {
          if (getComponent(i) == btn) {
            index = i;
            break;
          }
        }
        if (index >= 0 && index < ToolManager.instance().getTools().size()) {
          btn.setSelected(ToolManager.instance().getTools().get(index).equals(active));
        }
      }
    }
  }
}
