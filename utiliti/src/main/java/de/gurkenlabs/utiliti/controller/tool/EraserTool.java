package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.gui.ComponentMouseEvent;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.Cursor;
import javax.swing.Icon;

public class EraserTool implements Tool {
  @Override
  public String getName() {
    return Resources.strings().get("tool_eraser");
  }

  @Override
  public Icon getIcon() {
    return Icons.DELETE_24;
  }

  @Override
  public Cursor getCursor() {
    return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  }

  @Override
  public void mousePressed(ComponentMouseEvent event) {
  }

  @Override
  public void mouseDragged(ComponentMouseEvent event) {
  }

  @Override
  public void mouseReleased(ComponentMouseEvent event) {
  }
}
