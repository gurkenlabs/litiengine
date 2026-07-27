package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.view.components.UI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
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
    setSize(520, 320);
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

  private void populateItems(String query) {
    listModel.clear();
    String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

    // Maps
    if (Editor.instance().getMapComponent() != null) {
      for (TmxMap map : Editor.instance().getMapComponent().getMaps()) {
        if (map.getName() != null && map.getName().toLowerCase(Locale.ROOT).contains(filter)) {
          listModel.addElement(new SearchItem(ItemType.MAP, map.getName(), map));
        }
      }
    }

    // Sprites
    if (Editor.instance().getGameFile() != null && Editor.instance().getGameFile().getSpriteSheets() != null) {
      for (SpritesheetResource sprite : Editor.instance().getGameFile().getSpriteSheets()) {
        if (sprite.getName() != null && sprite.getName().toLowerCase(Locale.ROOT).contains(filter)) {
          listModel.addElement(new SearchItem(ItemType.SPRITE, sprite.getName(), sprite));
        }
      }
    }

    // Sounds
    if (Editor.instance().getGameFile() != null && Editor.instance().getGameFile().getSounds() != null) {
      for (SoundResource sound : Editor.instance().getGameFile().getSounds()) {
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

    if (selected.type() == ItemType.MAP && selected.object() instanceof TmxMap map) {
      Editor.instance().getMapComponent().loadEnvironment(map);
    } else if (selected.type() == ItemType.SPRITE || selected.type() == ItemType.SOUND) {
      UI.getAssetController().refresh();
    }
  }

  public enum ItemType {
    MAP, SPRITE, SOUND
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
