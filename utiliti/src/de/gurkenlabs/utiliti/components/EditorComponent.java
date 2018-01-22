package de.gurkenlabs.utiliti.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.utiliti.EditorScreen;

public abstract class EditorComponent extends GuiComponent {

  public enum ComponentType {
    MAP
  }

  private ComponentType componentType;

  public EditorComponent(final ComponentType componentType) {
    super(0, EditorScreen.instance().getPadding(), Game.getScreenManager().getResolution().getWidth(), Game.getScreenManager().getResolution().getHeight() - Game.getScreenManager().getResolution().getHeight() * 1 / 15);
    this.componentType = componentType;
  }

  public ComponentType getComponentType() {
    return this.componentType;
  }

  public void setComponentType(ComponentType componentType) {
    this.componentType = componentType;
  }
}
