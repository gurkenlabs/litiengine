package de.gurkenlabs.utiliti.swing.panels;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.graphics.emitters.xml.CustomEmitter;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.JCheckBoxList;
import java.awt.Component;

public class UpdatedEmitterPanel extends PropertyPanel {
  private transient CustomEmitter emitter;
  private final DefaultListModel<JCheckBox> layerModel;

  private JPanel controlPanel;

  public enum EmitterPropertyGroup {
    EMISSION, STYLE, COLOR, SIZE, OFFSET, VELOCITY, ACCELERATION, COLLISION
  }

  public UpdatedEmitterPanel() {
    super("panel_emitter", Icons.EMITTER);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JSplitPane splitPane = new JSplitPane();

    JList<JCheckBox> list = new JCheckBoxList();
    this.layerModel = new DefaultListModel<>();

    JTabbedPane propertyGrouptabs = new JTabbedPane();
    propertyGrouptabs.setAlignmentX(Component.LEFT_ALIGNMENT);
    propertyGrouptabs.setTabPlacement(JTabbedPane.LEFT);
    for (EmitterPropertyGroup e : EmitterPropertyGroup.values()) {
      String localized = Resources.strings().get(String.format("emitter_%s", e.name().toLowerCase()));
      propertyGrouptabs.add(new JPanel());
      propertyGrouptabs.setTabComponentAt(e.ordinal(), new JLabel(String.format("<html><p style=\"text-align: left; width: %spx\">%s</p></html>", LABEL_WIDTH * 1.5, localized), SwingConstants.LEFT));
    }

    this.add(propertyGrouptabs);

    // this.setupChangedListeners();

  }

  // private void setupChangedListeners() {
  // this.btnPause.addActionListener(a -> {
  // if (this.emitter != null) {
  // this.emitter.togglePaused();
  // }
  //
  // if (!btnPause.isSelected()) {
  // this.btnPause.setIcon(Icons.PLAY);
  // } else {
  // this.btnPause.setIcon(Icons.PAUSE);
  // }
  // });
  // }

  @Override
  protected void clearControls() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.emitter = (CustomEmitter) Game.world().environment().getEmitter(mapObject.getId());

  }

}
