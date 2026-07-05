package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.Cursor;
import javax.swing.Icon;

public class PointerTool implements Tool {
  @Override
  public String getName() {
    return Resources.strings().get("tool_pointer");
  }

  @Override
  public Icon getIcon() {
    return Icons.POINTER_24;
  }

  @Override
  public Cursor getCursor() {
    return Cursor.getDefaultCursor();
  }

  @Override
  public void activated() {
  }

  @Override
  public void deactivated() {
  }
}
