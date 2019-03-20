package de.gurkenlabs.utiliti.components;

import java.awt.event.MouseEvent;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.GuiComponent;

public abstract class EditorComponent extends GuiComponent {

  public enum ComponentType {
    MAP
  }

  private ComponentType componentType;

  public EditorComponent(final ComponentType componentType) {
    super(0, EditorScreen.instance().getPadding(), Game.window().getResolution().getWidth(), Game.window().getResolution().getHeight() - Game.window().getResolution().getHeight() * 1 / 15);
    this.componentType = componentType;
  }

  public ComponentType getComponentType() {
    return this.componentType;
  }

  public void setComponentType(ComponentType componentType) {
    this.componentType = componentType;
  }
 
  @Override
  protected boolean mouseEventShouldBeForwarded(final MouseEvent e) {
    return this.isForwardMouseEvents() && this.isVisible() && this.isEnabled() && !this.isSuspended() && e != null;
  }
}
