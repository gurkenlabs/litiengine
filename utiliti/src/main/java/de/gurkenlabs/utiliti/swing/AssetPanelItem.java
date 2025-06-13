package de.gurkenlabs.utiliti.swing;

import com.github.weisj.darklaf.components.border.DarkBorders;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.EmitterMapObjectLoader;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundFormat;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.io.Codec;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.swing.dialogs.SpritesheetImportPanel;
import de.gurkenlabs.utiliti.swing.dialogs.XmlExportDialog;
import de.gurkenlabs.utiliti.swing.panels.CreaturePanel;
import de.gurkenlabs.utiliti.swing.panels.PropPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AssetPanelItem extends JPanel {
  private static final Logger log = Logger.getLogger(AssetPanelItem.class.getName());
  private static final Border normalBorder = DarkBorders.createLineBorder(1, 1, 1, 1);
  private static final Border focusBorder = BorderFactory.createDashedBorder(UIManager.getDefaults().getColor("Tree.selectionBorderColor"));

  private static final Dimension BUTTON_MIN = new Dimension(16, 16);
  private static final Dimension BUTTON_MAX = new Dimension(64, 64);
  private static final Dimension BUTTON_PREF = new Dimension(24, 24);

  private final JLabel iconLabel;
  private final JTextArea textField;
  private final JPanel buttonPanel;
  private final JButton btnEdit;
  private final JButton btnDelete;
  private final JButton btnAdd;
  private final JButton btnExport;

  private final transient Object origin;

  public AssetPanelItem(Object origin) {
    setPreferredSize(new Dimension(100, 135));
    this.origin = origin;
    setBorder(normalBorder);

    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAsset");
    getActionMap().put("deleteAsset", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent ae) {
        deleteAsset();
      }
    });

    addFocusListener(new FocusAdapter() {
      @Override public void focusGained(FocusEvent e) {
        setBorder(focusBorder);
        if (getOrigin() instanceof SpritesheetResource || getOrigin() instanceof EmitterData) {
          btnEdit.setVisible(true);
          btnAdd.setVisible(true);
          btnDelete.setVisible(true);
          btnExport.setVisible(true);
        } else if (getOrigin() instanceof Tileset) {
          btnEdit.setVisible(false);
          btnAdd.setVisible(false);
          btnDelete.setVisible(false);
          btnExport.setVisible(true);
        } else if (getOrigin() instanceof MapObject) {
          btnEdit.setVisible(false);
          btnAdd.setVisible(true);
          btnDelete.setVisible(true);
          btnExport.setVisible(true);
        } else if (getOrigin() instanceof SoundResource) {
          btnEdit.setVisible(false);
          btnAdd.setVisible(false);
          btnDelete.setVisible(true);
          btnExport.setVisible(true);
        }
      }

      @Override public void focusLost(FocusEvent e) {
        setBorder(normalBorder);
        btnEdit.setVisible(false);
        btnAdd.setVisible(false);
        btnDelete.setVisible(false);
        btnExport.setVisible(false);
      }
    });

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setFocusable(true);
    setRequestFocusEnabled(true);
    addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        requestFocus();
      }
    });

    iconLabel = new JLabel();

    JPanel iconAlignPanel = new JPanel();
    iconAlignPanel.setLayout(new BoxLayout(iconAlignPanel, BoxLayout.X_AXIS));
    iconAlignPanel.add(Box.createHorizontalGlue());
    iconAlignPanel.add(iconLabel);
    iconAlignPanel.add(Box.createHorizontalGlue());
    iconAlignPanel.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        requestFocus();
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && addEntity()) {
          e.consume();
        }
      }

      @Override public void mouseReleased(MouseEvent e) {
        requestFocus();
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && addEntity()) {
          e.consume();
        }
      }
    });

    textField = new RowLimitedTextArea(2, 10);
    textField.setLineWrap(true);
    textField.setWrapStyleWord(true);
    textField.setBorder(null);
    textField.setEditable(false);
    textField.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        requestFocus();
      }

      @Override public void mouseReleased(MouseEvent e) {
        requestFocus();
      }
    });

    buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setOpaque(false);

    buttonPanel.add(Box.createHorizontalGlue());
    btnAdd = new JButton();
    initButton(btnAdd, "assetpanel_add", Icons.ADD, e -> this.addEntity());
    btnAdd.setEnabled(canAdd());
    btnEdit = new JButton();
    initButton(btnEdit, "assetpanel_edit", Icons.PENCIL, e -> this.editAsset());
    btnDelete = new JButton();
    initButton(btnDelete, "assetpanel_delete", Icons.DELETE, e -> this.deleteAsset());
    btnExport = new JButton();
    initButton(btnExport, "assetpanel_export", Icons.EXPORT, e -> this.exportAsset());
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        requestFocus();
      }
    });

    add(iconAlignPanel);
    add(textField);
    add(buttonPanel);
  }

  public AssetPanelItem(Icon icon, String text, Object origin) {
    this(origin);
    iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
    iconLabel.setIcon(icon);
    textField.setText(text);
    setToolTipText(
      String.format("%s:\t%s%n%s:\t%s", Resources.strings().get("assetpanel_assetname"), text, Resources.strings().get("assetpanel_assetdetails"),
        origin));
    StringBuilder sb = new StringBuilder();
    sb.append("<html>");
    sb.append(String.format("<b>%s:</b>\t", Resources.strings().get("assetpanel_assetname")));
    sb.append(text);
    for (Map.Entry<String, String> entry : getDetails(origin).entrySet()) {
      sb.append("<br>");
      sb.append(entry.getKey());
      sb.append(":\t");
      sb.append(entry.getValue());
    }
    sb.append("</html>");
    String tooltip = sb.toString();
    for (Component component : this.getComponents()) {
      if (component instanceof JComponent jcomponent) {
        jcomponent.setToolTipText(tooltip);
      }
    }
  }

  public Object getOrigin() {
    return this.origin;
  }

  private static Map<String, String> getDetails(Object origin) {
    Map<String, String> details = new ConcurrentHashMap<>();

    if (origin instanceof SpritesheetResource spritesheetResource) {
      details.put("Size", spritesheetResource.getWidth() + "x" + spritesheetResource.getHeight() + "px");
    }

    return details;
  }

  private void initButton(JButton button, String tooltipStringIdentifier, Icon icon, ActionListener action) {
    button.setToolTipText(Resources.strings().get(tooltipStringIdentifier));
    button.setMinimumSize(BUTTON_MIN);
    button.setMaximumSize(BUTTON_MAX);
    button.setPreferredSize(BUTTON_PREF);
    button.setOpaque(false);
    button.setIcon(icon);
    button.setVisible(false);
    button.addActionListener(action);
    buttonPanel.add(button);
  }

  private void deleteAsset() {
    if (getOrigin() instanceof SpritesheetResource spritesheetResource) {
      int n = getDeleteDialog("spritesheet", spritesheetResource.getName());

      if (n == JOptionPane.OK_OPTION) {
        Editor.instance().getGameFile().getSpriteSheets().remove(spritesheetResource);
        Resources.images().clear();
        Resources.spritesheets().remove(spritesheetResource.getName());
        Editor.instance().getMapComponent().reloadEnvironment();
        UI.getAssetController().refresh();
      }
    } else if (getOrigin() instanceof EmitterData emitterData) {
      int n = getDeleteDialog("emitter", emitterData.getName());

      if (n == JOptionPane.OK_OPTION) {
        Editor.instance().getGameFile().getEmitters().remove(emitterData);
        Editor.instance().getMapComponent().reloadEnvironment();
        UI.getAssetController().refresh();
      }
    } else if (getOrigin() instanceof Blueprint blueprint) {
      int n = getDeleteDialog("blueprint", blueprint.getName());

      if (n == JOptionPane.OK_OPTION) {
        Editor.instance().getGameFile().getBluePrints().remove(blueprint);
        Resources.blueprints().remove(blueprint.getName());
        UI.getAssetController().refresh();
      }
    } else if (getOrigin() instanceof SoundResource soundResource) {
      int n = getDeleteDialog("sound", soundResource.getName());

      if (n == JOptionPane.OK_OPTION) {
        Editor.instance().getGameFile().getSounds().remove(soundResource);
        Resources.sounds().remove(soundResource.getName());
        UI.getAssetController().refresh();
      }
    }
  }

  private boolean addEntity() {
    if (Game.world().environment() == null || Game.world().camera() == null) {
      return false;
    }

    if (this.getOrigin() instanceof SpritesheetResource spritesheetResource) {
      MapObject mo = new MapObject();
      String propName = PropPanel.getIdentifierBySpriteName(spritesheetResource.getName());
      String creatureName = CreaturePanel.getCreatureSpriteName(spritesheetResource.getName());
      if (propName != null) {
        mo.setType(MapObjectType.PROP.name());
        mo.setValue(MapObjectProperty.SPRITESHEETNAME, propName);
      } else if (creatureName != null) {
        mo.setType(MapObjectType.CREATURE.name());
        mo.setValue(MapObjectProperty.SPRITESHEETNAME, creatureName);
      } else {
        return false;
      }

      mo.setX((int) Game.world().camera().getFocus().getX() - spritesheetResource.getWidth() / 2f);
      mo.setY((int) Game.world().camera().getFocus().getY() - spritesheetResource.getHeight() / 2f);
      mo.setWidth(spritesheetResource.getWidth());
      mo.setHeight(spritesheetResource.getHeight());
      mo.setId(Game.world().environment().getNextMapId());
      mo.setName("");
      mo.setValue(MapObjectProperty.COLLISIONBOX_WIDTH, spritesheetResource.getWidth() * 0.4);
      mo.setValue(MapObjectProperty.COLLISIONBOX_HEIGHT, spritesheetResource.getHeight() * 0.4);
      mo.setValue(MapObjectProperty.COLLISION, true);
      mo.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false);
      mo.setValue(MapObjectProperty.PROP_ADDSHADOW, true);

      Editor.instance().getMapComponent().add(mo);
      return true;
    } else if (this.getOrigin() instanceof EmitterData) {
      MapObject newEmitter = (MapObject) EmitterMapObjectLoader.createMapObject((EmitterData) this.getOrigin());
      newEmitter.setX((int) (Game.world().camera().getFocus().getX() - newEmitter.getWidth()));
      newEmitter.setY((int) (Game.world().camera().getFocus().getY() - newEmitter.getHeight()));
      newEmitter.setId(Game.world().environment().getNextMapId());
      Editor.instance().getMapComponent().add(newEmitter);
    } else if (this.getOrigin() instanceof Blueprint blueprint) {
      UndoManager.instance().beginOperation();
      try {
        List<IMapObject> newObjects = blueprint.build((int) Game.world().camera().getFocus().getX() - blueprint.getWidth() / 2,
          (int) Game.world().camera().getFocus().getY() - blueprint.getHeight() / 2);
        for (IMapObject newMapObject : newObjects) {
          Editor.instance().getMapComponent().add(newMapObject);
        }

        // separately select the added objects because this cannot be done in
        // the
        // previous loop because it gets overwritten every time a map object
        // gets added
        for (IMapObject newMapObject : newObjects) {
          Editor.instance().getMapComponent().setSelection(newMapObject, false);
        }
      } finally {
        UndoManager.instance().endOperation();
      }
    }

    return false;
  }

  private void editAsset() {
    if (!(this.getOrigin() instanceof SpritesheetResource)) {
      return;
    }
    SpritesheetImportPanel spritePanel = new SpritesheetImportPanel((SpritesheetResource) this.getOrigin());
    int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), spritePanel, Resources.strings().get("menu_assets_editSprite"),
      JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (option != JOptionPane.OK_OPTION) {
      return;
    }

    final Collection<SpritesheetResource> sprites = spritePanel.getSpriteSheets();
    for (SpritesheetResource spriteFile : sprites) {
      int index = -1;
      Optional<SpritesheetResource> old =
        Editor.instance().getGameFile().getSpriteSheets().stream().filter((x -> x.getName().equals(spriteFile.getName()))).findFirst();
      if (old.isPresent()) {
        index = Editor.instance().getGameFile().getSpriteSheets().indexOf(old.get());
        Editor.instance().getGameFile().getSpriteSheets().remove(index);
      }

      Editor.instance().getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(spriteFile.getName()));
      if (index != -1) {
        Editor.instance().getGameFile().getSpriteSheets().add(index, spriteFile);
      } else {
        Editor.instance().getGameFile().getSpriteSheets().add(spriteFile);
      }
    }

    Editor.instance().loadSpriteSheets(Editor.instance().getGameFile().getSpriteSheets(), true);
  }

  private void exportAsset() {
    if (this.getOrigin() instanceof Tileset) {
      this.exportTileset();
    } else if (this.getOrigin() instanceof SpritesheetResource) {
      this.exportSpritesheet();
    } else if (this.getOrigin() instanceof EmitterData) {
      this.exportEmitter();
    } else if (this.getOrigin() instanceof MapObject) {
      this.exportBlueprint();
    } else if (this.getOrigin() instanceof SoundResource) {
      this.exportSound();
    }
  }

  private void exportSpritesheet() {
    if (this.getOrigin() instanceof SpritesheetResource spritesheetResource) {
      Spritesheet sprite = Resources.spritesheets().get(spritesheetResource.getName());
      if (sprite == null) {
        return;
      }

      ImageFormat format = sprite.getImageFormat() != ImageFormat.UNSUPPORTED ? sprite.getImageFormat() : ImageFormat.PNG;

      Object[] options = {".xml", format.toFileExtension()};
      int answer =
        JOptionPane.showOptionDialog(Game.window().getRenderComponent(), "Select an export format:", "Export Spritesheet", JOptionPane.DEFAULT_OPTION,
          JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

      JFileChooser chooser;
      Path source = Editor.instance().getProjectPath();
      chooser = new JFileChooser(source.toFile());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setDialogType(JFileChooser.SAVE_DIALOG);
      chooser.setDialogTitle("Export Spritesheet");
      if (answer == 0) {
        XmlExportDialog.export(spritesheetResource, "Spritesheet", spritesheetResource.getName());
      } else if (answer == 1) {
        FileFilter filter = new FileNameExtensionFilter(format + " - Image", format.toString());
        chooser.setFileFilter(filter);
        chooser.addChoosableFileFilter(filter);
        chooser.setSelectedFile(new File(spritesheetResource.getName() + format.toFileExtension()));

        int result = chooser.showSaveDialog(Game.window().getRenderComponent());
        if (result == JFileChooser.APPROVE_OPTION) {
          try {
            ImageIO.write(sprite.getImage(), format.toFileExtension(), chooser.getSelectedFile());
          } catch (IOException e) {
            log.log(Level.SEVERE, e.getLocalizedMessage(), e);
          }
          log.log(Level.INFO, "exported spritesheet {0} to {1}", new Object[] {spritesheetResource.getName(), chooser.getSelectedFile()});
        }
      }
    }
  }

  private void exportTileset() {
    if (!(this.getOrigin() instanceof Tileset tileset)) {
      return;
    }
    XmlExportDialog.export(tileset, "Tileset", tileset.getName(), Tileset.FILE_EXTENSION);
  }

  private void exportEmitter() {
    if (!(this.getOrigin() instanceof EmitterData emitter)) {
      return;
    }
    XmlExportDialog.export(emitter, "Emitter", emitter.getName());
  }

  private void exportBlueprint() {
    if (!(this.getOrigin() instanceof Blueprint mapObject)) {
      return;
    }
    XmlExportDialog.export(mapObject, "Blueprint", mapObject.getName(), Blueprint.BLUEPRINT_FILE_EXTENSION);
  }

  private void exportSound() {
    if (!(this.getOrigin() instanceof SoundResource sound)) {
      return;
    }
    SoundFormat format = sound.getFormat();
    if (format == SoundFormat.UNSUPPORTED) {
      return;
    }

    FileFilter filter = new FileNameExtensionFilter(format.toString() + " - Sound", format.toString());
    try {
      JFileChooser chooser;
      Path source = Editor.instance().getProjectPath();
      chooser = new JFileChooser(source.toFile());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setDialogType(JFileChooser.SAVE_DIALOG);
      chooser.setDialogTitle("Export Sound");
      chooser.setFileFilter(filter);
      chooser.addChoosableFileFilter(filter);
      chooser.setSelectedFile(new File(sound.getName() + format.toFileExtension()));

      int result = chooser.showSaveDialog(Game.window().getRenderComponent());
      if (result == JFileChooser.APPROVE_OPTION) {
        try (FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile().toString())) {
          fos.write(Codec.decode(sound.getData()));
          log.log(Level.INFO, "exported sound {0} to {1}", new Object[] {sound.getName(), chooser.getSelectedFile()});
        }
      }
    } catch (IOException ex) {
      log.log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  private boolean canAdd() {
    if (this.getOrigin() instanceof SpritesheetResource spritesheetResource) {
      String propName = PropPanel.getIdentifierBySpriteName(spritesheetResource.getName());
      return propName != null && !propName.isEmpty() || CreaturePanel.getCreatureSpriteName(spritesheetResource.getName()) != null;
    }

    return this.getOrigin() instanceof MapObject || this.getOrigin() instanceof EmitterData;
  }

  private static int getDeleteDialog(String assetType, String assetName) {
    return JOptionPane.showConfirmDialog(Game.window().getRenderComponent(),
      Resources.strings().get(String.format("assetpanel_confirmdelete_%s", assetType), assetName),
      Resources.strings().get(String.format("assetpanel_confirmdelete_%s_title", assetType)), JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE);
  }
}
