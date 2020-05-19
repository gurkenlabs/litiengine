package de.gurkenlabs.utiliti.swing.panels;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.graphics.emitters.xml.CustomEmitter;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.JCheckBoxList;

public class UpdatedEmitterPanel extends PropertyPanel {
  private transient CustomEmitter emitter;
  private final DefaultListModel<JCheckBox> layerModel;

  private final String[] listItems = { "emission", "shape", "color", "size", "offset", "velocity", "acceleration", "collision" };
  private JPanel controlPanel;

  public UpdatedEmitterPanel() {
    super("panel_emitter", Icons.EMITTER);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JSplitPane splitPane = new JSplitPane();

    JList<JCheckBox> list = new JCheckBoxList();
    this.layerModel = new DefaultListModel<>();

    list.setModel(this.layerModel);
    this.initEmitterPropertyList();
    list.setSelectedIndex(0);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    splitPane.setLeftComponent(list);
    this.controlPanel = new JPanel();
    splitPane.setRightComponent(this.controlPanel);

    splitPane.setEnabled(true);
    splitPane.setDividerSize(1);
    add(splitPane);

    // this.setupChangedListeners();

  }

  private void initEmitterPropertyList() {
    for (String propertyGroup : listItems) {
      String localized = Resources.strings().get(String.format("emitter_%s", propertyGroup));
      JCheckBox newBox = new JCheckBox(localized);
      newBox.setName(localized);
      newBox.addItemListener(sel -> {

      });
      this.layerModel.addElement(newBox);
    }
  }

  private JPanel getGroup(String groupName) {
    return new JPanel();
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
