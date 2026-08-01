package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.view.components.UI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Locale;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class QuickSearchDialog extends JDialog {
  private final JTextField searchField;
  private final JList<SearchItem> resultList;
  private final DefaultListModel<SearchItem> listModel;

  public static void showPalette() {
    Frame parent = (Frame) SwingUtilities.getWindowAncestor(Game.window().getHostControl());
    QuickSearchDialog dialog = new QuickSearchDialog(parent);
    dialog.setVisible(true);
  }

  public QuickSearchDialog(Frame parent) {
    super(parent, "Quick Search", true);
    setUndecorated(true);
    setSize(540, 340);
    if (parent != null) {
      setLocationRelativeTo(parent);
    }

    JPanel contentPane = new JPanel(new BorderLayout(5, 5));
    contentPane.setBorder(new EmptyBorder(8, 8, 8, 8));

    searchField = new JTextField();
    searchField.setFont(searchField.getFont().deriveFont(14f));
    searchField.setColumns(20);

    listModel = new DefaultListModel<>();
    resultList = new JList<>(listModel);
    resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    resultList.setCellRenderer(new SearchItemRenderer());

    JScrollPane scrollPane = new JScrollPane(resultList);

    contentPane.add(searchField, BorderLayout.NORTH);
    contentPane.add(scrollPane, BorderLayout.CENTER);

    setContentPane(contentPane);

    setupEvents();
    populateItems("");
  }

  private void setupEvents() {
    searchField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          if (!listModel.isEmpty()) {
            resultList.setSelectedIndex(Math.min(resultList.getSelectedIndex() + 1, listModel.size() - 1));
            resultList.ensureIndexIsVisible(resultList.getSelectedIndex());
          }
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          if (!listModel.isEmpty()) {
            resultList.setSelectedIndex(Math.max(resultList.getSelectedIndex() - 1, 0));
            resultList.ensureIndexIsVisible(resultList.getSelectedIndex());
          }
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          confirmSelection();
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          dispose();
          return;
        }
        populateItems(searchField.getText());
      }
    });

    resultList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          confirmSelection();
        }
      }
    });

    resultList.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          confirmSelection();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          dispose();
        }
      }
    });
  }

  private static final int MAX_RESULTS = 100;

  private void populateItems(String query) {
    listModel.clear();
    String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

    // Scene Graph Map Objects & Layers
    if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
      IMap activeMap = Game.world().environment().getMap();
      for (IMapObjectLayer objectLayer : activeMap.getMapObjectLayers()) {
        if (objectLayer == null || listModel.size() >= MAX_RESULTS) {
          break;
        }
        for (IMapObject mapObject : objectLayer.getMapObjects()) {
          if (listModel.size() >= MAX_RESULTS) {
            break;
          }
          if (mapObject == null) {
            continue;
          }
          String objName = mapObject.getName();
          String objType = mapObject.getType();
          String objId = String.valueOf(mapObject.getId());
          if ((objName != null && objName.toLowerCase(Locale.ROOT).contains(filter))
            || (objType != null && objType.toLowerCase(Locale.ROOT).contains(filter))
            || (!filter.isEmpty() && objId.contains(filter))) {
            String label = (objName != null && !objName.isBlank() ? objName : "#" + objId)
              + (objType != null && !objType.isBlank() ? " [" + objType + "]" : "");
            listModel.addElement(new SearchItem(ItemType.OBJECT, label, mapObject));
          }
        }
      }

      for (ILayer layer : activeMap.getRenderLayers()) {
        if (listModel.size() >= MAX_RESULTS) {
          break;
        }
        if (layer != null && layer.getName() != null && layer.getName().toLowerCase(Locale.ROOT).contains(filter)) {
          listModel.addElement(new SearchItem(ItemType.LAYER, layer.getName(), layer));
        }
      }
    }

    // Maps
    if (Editor.instance().getMapComponent() != null && listModel.size() < MAX_RESULTS) {
      for (TmxMap map : Editor.instance().getMapComponent().getMaps()) {
        if (listModel.size() >= MAX_RESULTS) {
          break;
        }
        if (map.getName() != null && map.getName().toLowerCase(Locale.ROOT).contains(filter)) {
          listModel.addElement(new SearchItem(ItemType.MAP, map.getName(), map));
        }
      }
    }

    // Sprites
    if (Editor.instance().getGameFile() != null && Editor.instance().getGameFile().getSpriteSheets() != null && listModel.size() < MAX_RESULTS) {
      for (SpritesheetResource sprite : Editor.instance().getGameFile().getSpriteSheets()) {
        if (listModel.size() >= MAX_RESULTS) {
          break;
        }
        if (sprite.getName() != null && sprite.getName().toLowerCase(Locale.ROOT).contains(filter)) {
          listModel.addElement(new SearchItem(ItemType.SPRITE, sprite.getName(), sprite));
        }
      }
    }

    // Sounds
    if (Editor.instance().getGameFile() != null && Editor.instance().getGameFile().getSounds() != null && listModel.size() < MAX_RESULTS) {
      for (SoundResource sound : Editor.instance().getGameFile().getSounds()) {
        if (listModel.size() >= MAX_RESULTS) {
          break;
        }
        if (sound.getName() != null && sound.getName().toLowerCase(Locale.ROOT).contains(filter)) {
          listModel.addElement(new SearchItem(ItemType.SOUND, sound.getName(), sound));
        }
      }
    }

    if (!listModel.isEmpty()) {
      resultList.setSelectedIndex(0);
    }
  }

  private void confirmSelection() {
    SearchItem selected = resultList.getSelectedValue();
    if (selected == null) {
      return;
    }

    dispose();

    if (selected.type() == ItemType.OBJECT && selected.object() instanceof IMapObject mapObject) {
      Editor.instance().getMapComponent().setFocus(mapObject, true);
      Rectangle2D bounds = mapObject.getBoundingBox();
      if (bounds != null) {
        Game.world().camera().setFocus(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()));
      }
      UI.showObjectInspector();
    } else if (selected.type() == ItemType.LAYER && selected.object() instanceof ILayer layer) {
      UI.showLayerProperties(layer);
    } else if (selected.type() == ItemType.MAP && selected.object() instanceof TmxMap map) {
      Editor.instance().getMapComponent().loadEnvironment(map);
    } else if (selected.type() == ItemType.SPRITE || selected.type() == ItemType.SOUND) {
      UI.getAssetController().refresh();
    }
  }

  public enum ItemType {
    OBJECT, LAYER, MAP, SPRITE, SOUND
  }

  public record SearchItem(ItemType type, String name, Object object) {
  }

  private static class SearchItemRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof SearchItem item) {
        label.setText("[" + item.type() + "] " + item.name());
      }
      return label;
    }
  }
}
