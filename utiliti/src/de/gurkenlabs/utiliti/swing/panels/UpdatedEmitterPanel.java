package de.gurkenlabs.utiliti.swing.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;

import com.github.weisj.darklaf.components.ClosableTabbedPane;
import com.github.weisj.darklaf.components.tabframe.JTabFrame;
import com.github.weisj.darklaf.components.tabframe.TabbedPopup;
import com.github.weisj.darklaf.components.text.NonWrappingTextPane;
import com.github.weisj.darklaf.components.text.NumberedTextComponent;
import com.github.weisj.darklaf.components.text.NumberingPane;
import com.github.weisj.darklaf.util.Alignment;
import com.github.weisj.darklaf.util.DarkUIUtil;
import com.github.weisj.darklaf.util.StringUtil;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.graphics.emitters.xml.CustomEmitter;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.JCheckBoxList;
import de.gurkenlabs.utiliti.swing.PropertyListCellRenderer;

import javax.swing.GroupLayout;
import javax.swing.JRadioButton;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.GridBagConstraints;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;

import java.awt.FlowLayout;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.JSplitPane;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;

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
    list.setCellRenderer(new PropertyListCellRenderer());

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
