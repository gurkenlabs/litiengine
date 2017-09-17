package de.gurkenlabs.utiliti.components;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.utiLITI.EditorScreen;

public class EditorComponent extends GuiComponent {

  public enum ComponentType {
    MAP
  }

  private ComponentType componentType;

  public EditorComponent(final ComponentType componentType) {
    super(0, EditorScreen.padding, Game.getScreenManager().getResolution().getWidth(), Game.getScreenManager().getResolution().getHeight() - Game.getScreenManager().getResolution().getHeight() * 1 / 15);
    this.componentType = componentType;
  }

  @Override
  public void render(Graphics2D g) {
    super.render(g);
  }

  @Override
  protected void initializeComponents() {

  }

  @Override
  public void prepare() {
    super.prepare();
  }

  public ComponentType getComponentType() {
    return this.componentType;
  }

  public void setComponentType(ComponentType componentType) {
    this.componentType = componentType;
  }

}
