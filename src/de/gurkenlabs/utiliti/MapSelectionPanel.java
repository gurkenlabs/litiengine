package de.gurkenlabs.utiliti;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.utiliti.components.JCheckBoxList;

public class MapSelectionPanel extends JPanel {
  JList list;
  JCheckBoxList listObjectLayers;
  DefaultListModel<String> model;
  DefaultListModel<JCheckBox> layerModel;
  JScrollPane scrollPane;

  /**
   * Create the panel.
   */
  public MapSelectionPanel() {
    setLayout(new BorderLayout(0, 0));

    JScrollPane scrollPane = new JScrollPane();
    add(scrollPane, BorderLayout.CENTER);

    model = new DefaultListModel<>();
    layerModel = new DefaultListModel<>();
    list = new JList<String>();
    list.setModel(model);
    list.setVisibleRowCount(8);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent e) {

        if (list.getSelectedIndex() < EditorScreen.instance().getMapComponent().getMaps().size() && list.getSelectedIndex() >= 0) {
          if (Game.getEnvironment() != null && Game.getEnvironment().getMap().equals(EditorScreen.instance().getMapComponent().getMaps().get(list.getSelectedIndex()))) {
            return;
          }

          EditorScreen.instance().getMapComponent().loadEnvironment(EditorScreen.instance().getMapComponent().getMaps().get(list.getSelectedIndex()));
          initLayerControl();
        }
      }
    });

    scrollPane.setViewportView(list);
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_maps"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(11f));
    scrollPane.setViewportBorder(border);

    JScrollPane scrollPane_1 = new JScrollPane();
    TitledBorder border2 = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_mapObjectLayers"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border2.setTitleFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(11f));
    scrollPane_1.setViewportBorder(border2);
    add(scrollPane_1, BorderLayout.SOUTH);

    listObjectLayers = new JCheckBoxList();
    listObjectLayers.setVisibleRowCount(5);
    listObjectLayers.setModel(layerModel);
    scrollPane_1.setViewportView(listObjectLayers);

  }

  public void bind(List<Map> maps) {
    model.clear();
    for (int i = 0; i < maps.size(); i++) {
      model.addElement(maps.get(i).getFileName());
    }
    list.setVisible(false);
    list.setVisible(true);
  }

  public void setSelection(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      list.clearSelection();
      return;
    }

    if (model.contains(mapName)) {
      list.setSelectedValue(mapName, true);
    }
    this.initLayerControl();
  }

  public boolean isSelectedMapObjectLayer(String name) {

    // Get all the selected items using the indices
    for (int i = 0; i < listObjectLayers.getModel().getSize(); i++) {
      if (i >= listObjectLayers.getModel().getSize()) {
        return false;
      }
      Object sel = listObjectLayers.getModel().getElementAt(i);
      JCheckBox check = (JCheckBox) sel;
      if (check.getText().equals(name) && check.isSelected()) {
        return true;
      }
    }
    return false;
  }

  private void initLayerControl() {
    Map map = EditorScreen.instance().getMapComponent().getMaps().get(list.getSelectedIndex());
    layerModel.clear();
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      JCheckBox newBox = new JCheckBox(layer.getName());
      newBox.setSelected(true);
      layerModel.addElement(newBox);
    }

    int start = 0;
    int end = list.getModel().getSize() - 1;
    if (end >= 0) {
      listObjectLayers.setSelectionInterval(start, end);
    }
  }

  public int getSelectedLayerIndex() {
    return listObjectLayers.getSelectedIndex();
  }
}
