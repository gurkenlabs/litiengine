package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

public class EmitterPanel extends PropertyPanel {

  public enum EmitterPropertyGroup {
    EMISSION,
    STYLE,
    SIZE,
    ORIGIN,
    ROTATION,
    MOTION,
    COLLISION
  }

  JTabbedPane propertyGrouptabs;

  public EmitterPanel() {
    super("panel_emitter", Icons.EMITTER_24);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.propertyGrouptabs = new JTabbedPane();
    this.propertyGrouptabs.setAlignmentX(Component.LEFT_ALIGNMENT);
    this.propertyGrouptabs.setTabPlacement(SwingConstants.TOP);
    for (EmitterPropertyGroup e : EmitterPropertyGroup.values()) {
      String localized =
          Resources.strings().get(String.format("emitter_%s", e.name().toLowerCase()));
      this.propertyGrouptabs.addTab(localized, EmitterPropertyPanel.getEmitterPropertyPanel(e));
    }

    this.add(this.propertyGrouptabs);
  }

  @Override
  public void bind(IMapObject mapObject) {
    for (EmitterPropertyGroup e : EmitterPropertyGroup.values()) {
      ((EmitterPropertyPanel) this.propertyGrouptabs.getComponent(e.ordinal())).bind(mapObject);
    }
  }



  @Override
  protected void clearControls() {
    // do nothing
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    // do nothing
  }
}
