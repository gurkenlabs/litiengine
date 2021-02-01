package com.litiengine.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import com.github.weisj.darklaf.ui.togglebutton.ToggleButtonConstants;

import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectProperty;
import com.litiengine.graphics.Spritesheet;
import com.litiengine.graphics.emitters.xml.EmitterData;
import com.litiengine.resources.Resources;
import com.litiengine.utiliti.swing.LabelListCellRenderer;

@SuppressWarnings("serial")
public class EmitterSpritePanel extends PropertyPanel {
  private final JComboBox<JLabel> spritesheet;
  private final JToggleButton animateSprite;
  private final JToggleButton loopSprite;

  public EmitterSpritePanel() {
    super();
    spritesheet = new JComboBox<>();
    spritesheet.setRenderer(new LabelListCellRenderer());
    animateSprite = new JToggleButton();
    animateSprite.putClientProperty(ToggleButtonConstants.KEY_VARIANT, ToggleButtonConstants.VARIANT_SLIDER);
    loopSprite = new JToggleButton();
    loopSprite.putClientProperty(ToggleButtonConstants.KEY_VARIANT, ToggleButtonConstants.VARIANT_SLIDER);
    setLayout(createLayout());
    setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    spritesheet.removeAllItems();
    spritesheet.setSelectedItem(null);
    animateSprite.setSelected(EmitterData.DEFAULT_ANIMATE_SPRITE);
    loopSprite.setSelected(EmitterData.DEFAULT_LOOP_SPRITE);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    loadSpritesheets();
    selectSpriteSheet(spritesheet, mapObject);
    animateSprite.setSelected(mapObject.getBoolValue(MapObjectProperty.Particle.ANIMATESPRITE, EmitterData.DEFAULT_ANIMATE_SPRITE));
    loopSprite.setSelected(mapObject.getBoolValue(MapObjectProperty.Particle.LOOPSPRITE, EmitterData.DEFAULT_LOOP_SPRITE));

  }

  protected LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("particle_spritesheet", spritesheet), new LayoutItem("particle_animatesprite", animateSprite), new LayoutItem("particle_loopsprite", loopSprite) };
    return this.createLayout(layoutItems);
  }

  private void setupChangedListeners() {
    setupL(spritesheet, MapObjectProperty.SPRITESHEETNAME);
    setup(animateSprite, MapObjectProperty.Particle.ANIMATESPRITE);
    animateSprite.addItemListener(e -> loopSprite.setEnabled(animateSprite.isSelected()));
    setup(loopSprite, MapObjectProperty.Particle.LOOPSPRITE);
  }

  private void loadSpritesheets() {
    spritesheet.removeAllItems();
    spritesheet.setSelectedItem(null);
    for (Spritesheet s : Resources.spritesheets().getAll()) {
      JLabel label = new JLabel();
      label.setText(s.getName());
      label.setIcon(new ImageIcon(s.getPreview(CONTROL_HEIGHT)));
      spritesheet.addItem(label);
    }
  }
}
