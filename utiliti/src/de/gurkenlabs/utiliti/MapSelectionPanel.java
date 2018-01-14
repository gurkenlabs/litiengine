package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.utiliti.components.JCheckBoxList;

public class MapSelectionPanel extends JSplitPane {
  JList<String> list;
  JCheckBoxList listObjectLayers;
  DefaultListModel<String> model;
  DefaultListModel<JCheckBox> layerModel;
  JScrollPane scrollPane;
  JScrollPane horizontalScrollPane;
  private JPopupMenu popupMenu;
  private JMenuItem mntmExportMap;
  private JMenuItem mntmDeleteMap;

  /**
   * Create the panel.
   */
  public MapSelectionPanel() {
    super(JSplitPane.HORIZONTAL_SPLIT);
    setContinuousLayout(true);

    scrollPane = new JScrollPane();
    scrollPane.setMinimumSize(new Dimension(150, 0));
    this.setLeftComponent(scrollPane);

    model = new DefaultListModel<>();
    layerModel = new DefaultListModel<>();
    list = new JList<>();
    list.setModel(model);
    list.setVisibleRowCount(8);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    list.getSelectionModel().addListSelectionListener(e -> {
      if (list.getSelectedIndex() < EditorScreen.instance().getMapComponent().getMaps().size() && list.getSelectedIndex() >= 0) {
        if (Game.getEnvironment() != null && Game.getEnvironment().getMap().equals(EditorScreen.instance().getMapComponent().getMaps().get(list.getSelectedIndex()))) {
          return;
        }

        EditorScreen.instance().getMapComponent().loadEnvironment(EditorScreen.instance().getMapComponent().getMaps().get(list.getSelectedIndex()));
        initLayerControl();
      }
    });

    scrollPane.setViewportView(list);

    popupMenu = new JPopupMenu();
    addPopup(list, popupMenu);

    mntmExportMap = new JMenuItem(Resources.get("hud_exportMap"));
    mntmExportMap.setIcon(new ImageIcon(RenderEngine.getImage("button-map-exportx16.png")));
    mntmExportMap.addActionListener(a -> EditorScreen.instance().getMapComponent().exportMap());

    popupMenu.add(mntmExportMap);

    mntmDeleteMap = new JMenuItem(Resources.get("hud_deleteMap"));
    mntmDeleteMap.setIcon(new ImageIcon(RenderEngine.getImage("button-deletex16.png")));
    mntmDeleteMap.addActionListener(a -> EditorScreen.instance().getMapComponent().deleteMap());
    popupMenu.add(mntmDeleteMap);
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_maps"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(11f));
    scrollPane.setViewportBorder(border);

    horizontalScrollPane = new JScrollPane();
    TitledBorder border2 = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_mapObjectLayers"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border2.setTitleFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(11f));
    horizontalScrollPane.setViewportBorder(border2);
    horizontalScrollPane.setMinimumSize(new Dimension(150, 0));
    this.setRightComponent(horizontalScrollPane);

    listObjectLayers = new JCheckBoxList();
    listObjectLayers.setVisibleRowCount(5);
    listObjectLayers.setModel(layerModel);
    horizontalScrollPane.setViewportView(listObjectLayers);

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

  private static void addPopup(Component component, final JPopupMenu popup) {
    component.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showMenu(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showMenu(e);
        }
      }

      private void showMenu(MouseEvent e) {
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    });
  }
}
