package de.gurkenlabs.utiliti.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

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
import de.gurkenlabs.litiengine.util.io.ImageSerializer;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.swing.dialogs.SpritesheetImportPanel;
import de.gurkenlabs.utiliti.swing.dialogs.XmlExportDialog;
import de.gurkenlabs.utiliti.swing.panels.CreaturePanel;
import de.gurkenlabs.utiliti.swing.panels.PropPanel;

@SuppressWarnings("serial")
public class AssetPanelItem extends JPanel {
  private static final Logger log = Logger.getLogger(AssetPanelItem.class.getName());
  private static final Border normalBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
  private static final Border focusBorder = BorderFactory.createDashedBorder(UIManager.getDefaults().getColor("Tree.selectionBorderColor"));

  private final JLabel iconLabel;
  private final JTextField textField;
  private final JPanel buttonPanel;
  private final JButton btnEdit;
  private final JButton btnDelete;
  private final JButton btnAdd;
  private final JButton btnExport;

  private final transient Object origin;

  public AssetPanelItem() {
    this(null);
  }

  public AssetPanelItem(Object origin) {
    setPreferredSize(new Dimension(100, 100));
    this.origin = origin;
    this.setBackground(Style.COLOR_ASSETPANEL_BACKGROUND);
    this.setBorder(normalBorder);

    this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAsset");
    this.getActionMap().put("deleteAsset", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        deleteAsset();
      }
    });

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        UIDefaults defaults = UIManager.getDefaults();
        setBackground(defaults.getColor("Tree.selectionBackground"));
        setForeground(defaults.getColor("Tree.selectionForeground"));
        textField.setForeground(defaults.getColor("Tree.selectionForeground"));
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

      @Override
      public void focusLost(FocusEvent e) {
        UIDefaults defaults = UIManager.getDefaults();
        setBackground(Style.COLOR_ASSETPANEL_BACKGROUND);
        setForeground(defaults.getColor("Tree.foreground"));
        textField.setForeground(Color.WHITE);
        setBorder(normalBorder);

        btnEdit.setVisible(false);
        btnAdd.setVisible(false);
        btnDelete.setVisible(false);
        btnExport.setVisible(false);
      }
    });

    setLayout(new BorderLayout(0, 0));
    this.setFocusable(true);
    this.setRequestFocusEnabled(true);
    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        requestFocus();
      }
    });

    this.iconLabel = new JLabel("");
    iconLabel.setPreferredSize(new Dimension(64, 64));
    this.iconLabel.setSize(64, 64);
    this.iconLabel.setMinimumSize(new Dimension(64, 64));
    this.iconLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        requestFocus();
      }
    });
    add(this.iconLabel, BorderLayout.NORTH);

    this.iconLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && addEntity()) {
          e.consume();
        }
      }
    });

    this.textField = new JTextField();
    add(this.textField, BorderLayout.SOUTH);
    this.textField.setColumns(10);
    this.textField.setHorizontalAlignment(SwingConstants.CENTER);
    this.textField.setForeground(Color.WHITE);
    this.textField.setBackground(null);
    this.textField.setBorder(null);
    this.textField.setEditable(false);

    this.setMinimumSize(new Dimension(100, 64));

    GridLayout buttonGridLayout = new GridLayout(0, 5, 0, 0);
    buttonPanel = new JPanel(buttonGridLayout);
    buttonPanel.setPreferredSize(new Dimension(100, 20));
    buttonPanel.setMinimumSize(new Dimension(100, 20));
    buttonPanel.setOpaque(false);
    add(buttonPanel, BorderLayout.WEST);

    btnAdd = new JButton("");
    btnAdd.setToolTipText("Add Entity");
    btnAdd.addActionListener(e -> this.addEntity());
    btnAdd.setMaximumSize(new Dimension(16, 16));
    btnAdd.setMinimumSize(new Dimension(16, 16));
    btnAdd.setPreferredSize(new Dimension(16, 16));
    btnAdd.setOpaque(false);
    btnAdd.setIcon(Icons.ADD);
    btnAdd.setVisible(false);
    btnAdd.setEnabled(canAdd());

    btnEdit = new JButton("");
    btnEdit.setToolTipText("Edit Asset");
    btnEdit.addActionListener(e -> {
      if (!(this.getOrigin() instanceof SpritesheetResource)) {
        return;
      }
      SpritesheetImportPanel spritePanel = new SpritesheetImportPanel((SpritesheetResource) this.getOrigin());
      int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), spritePanel, Resources.strings().get("menu_assets_editSprite"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (option != JOptionPane.OK_OPTION) {
        return;
      }

      final Collection<SpritesheetResource> sprites = spritePanel.getSpriteSheets();
      for (SpritesheetResource spriteFile : sprites) {
        int index = -1;
        Optional<SpritesheetResource> old = Editor.instance().getGameFile().getSpriteSheets().stream().filter((x -> x.getName().equals(spriteFile.getName()))).findFirst();
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
    });
    btnEdit.setMaximumSize(new Dimension(16, 16));
    btnEdit.setMinimumSize(new Dimension(16, 16));
    btnEdit.setPreferredSize(new Dimension(16, 16));
    btnEdit.setOpaque(false);
    btnEdit.setIcon(Icons.PENCIL);
    btnEdit.setVisible(false);

    btnDelete = new JButton("");
    btnDelete.setToolTipText("Delete Asset");
    btnDelete.addActionListener(e -> this.deleteAsset());
    btnDelete.setMaximumSize(new Dimension(16, 16));
    btnDelete.setMinimumSize(new Dimension(16, 16));
    btnDelete.setPreferredSize(new Dimension(16, 16));
    btnDelete.setOpaque(false);
    btnDelete.setIcon(Icons.DELETE);
    btnDelete.setVisible(false);

    btnExport = new JButton("");
    btnExport.setToolTipText("Export Asset");
    btnExport.addActionListener(e -> this.export());
    btnExport.setMaximumSize(new Dimension(16, 16));
    btnExport.setMinimumSize(new Dimension(16, 16));
    btnExport.setPreferredSize(new Dimension(16, 16));
    btnExport.setOpaque(false);
    btnExport.setIcon(Icons.EXPORT);
    btnExport.setVisible(false);

    buttonPanel.add(btnEdit);
    buttonPanel.add(btnAdd);
    buttonPanel.add(btnDelete);
    buttonPanel.add(btnExport);
  }

  public AssetPanelItem(Icon icon, String text, Object origin) {
    this(origin);
    this.iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
    this.iconLabel.setIcon(icon);
    this.textField.setText(text);
  }

  public Object getOrigin() {
    return this.origin;
  }

  private void deleteAsset() {
    if (getOrigin() instanceof SpritesheetResource) {
      SpritesheetResource info = (SpritesheetResource) getOrigin();
      int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), "Do you really want to delete the spritesheet [" + info.getName() + "]?\n Entities that use the sprite won't be rendered anymore!", "Delete Spritesheet?", JOptionPane.YES_NO_OPTION);

      if (n == JOptionPane.OK_OPTION) {
        Editor.instance().getGameFile().getSpriteSheets().remove(getOrigin());
        Resources.images().clear();
        Resources.spritesheets().remove(info.getName());
        Editor.instance().getMapComponent().reloadEnvironment();

        UI.getAssetController().refresh();
      }
    } else if (getOrigin() instanceof EmitterData) {
      EmitterData emitter = (EmitterData) getOrigin();
      int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), "Do you really want to delete the emitter [" + emitter.getName() + "]", "Delete Emitter?", JOptionPane.YES_NO_OPTION);

      if (n == JOptionPane.OK_OPTION) {
        Editor.instance().getGameFile().getEmitters().remove(getOrigin());
        Editor.instance().getMapComponent().reloadEnvironment();

        UI.getAssetController().refresh();
      }
    } else if (getOrigin() instanceof Blueprint) {
      Blueprint blueprint = (Blueprint) getOrigin();
      int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), "Do you really want to delete the blueprint [" + blueprint.getName() + "]?", "Delete Blueprint?", JOptionPane.YES_NO_OPTION);
      if (n == JOptionPane.OK_OPTION) {
        Editor.instance().getGameFile().getBluePrints().remove(getOrigin());
        UI.getAssetController().refresh();
      }
    } else if (getOrigin() instanceof SoundResource) {
      SoundResource sound = (SoundResource) getOrigin();
      int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), "Do you really want to delete the sound [" + sound.getName() + "]?", "Delete Sound?", JOptionPane.YES_NO_OPTION);
      if (n == JOptionPane.OK_OPTION) {
        Editor.instance().getGameFile().getSounds().remove(getOrigin());
        UI.getAssetController().refresh();
      }
    }
  }

  private boolean addEntity() {
    if (Game.world().environment() == null || Game.world().camera() == null) {
      return false;
    }

    if (this.getOrigin() instanceof SpritesheetResource) {
      SpritesheetResource info = (SpritesheetResource) this.getOrigin();

      MapObject mo = new MapObject();
      String propName = PropPanel.getIdentifierBySpriteName(info.getName());
      String creatureName = CreaturePanel.getCreatureSpriteName(info.getName());
      if (propName != null) {
        mo.setType(MapObjectType.PROP.name());
        mo.setValue(MapObjectProperty.SPRITESHEETNAME, propName);
      } else if (creatureName != null) {
        mo.setType(MapObjectType.CREATURE.name());
        mo.setValue(MapObjectProperty.SPRITESHEETNAME, creatureName);
      } else {
        return false;
      }

      mo.setX((int) Game.world().camera().getFocus().getX() - info.getWidth() / 2);
      mo.setY((int) Game.world().camera().getFocus().getY() - info.getHeight() / 2);
      mo.setWidth((int) info.getWidth());
      mo.setHeight((int) info.getHeight());
      mo.setId(Game.world().environment().getNextMapId());
      mo.setName("");
      mo.setValue(MapObjectProperty.COLLISIONBOX_WIDTH, info.getWidth() * 0.4);
      mo.setValue(MapObjectProperty.COLLISIONBOX_HEIGHT, info.getHeight() * 0.4);
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
    } else if (this.getOrigin() instanceof Blueprint) {
      Blueprint blueprint = (Blueprint) this.getOrigin();

      UndoManager.instance().beginOperation();
      try {
        List<IMapObject> newObjects = blueprint.build((int) Game.world().camera().getFocus().getX() - blueprint.getWidth() / 2, (int) Game.world().camera().getFocus().getY() - blueprint.getHeight() / 2);
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

  private void export() {
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
    if (this.getOrigin() instanceof SpritesheetResource) {
      SpritesheetResource spriteSheetInfo = (SpritesheetResource) this.getOrigin();

      Spritesheet sprite = Resources.spritesheets().get(spriteSheetInfo.getName());
      if (sprite == null) {
        return;
      }

      ImageFormat format = sprite.getImageFormat() != ImageFormat.UNSUPPORTED ? sprite.getImageFormat() : ImageFormat.PNG;

      Object[] options = { ".xml", format.toFileExtension() };
      int answer = JOptionPane.showOptionDialog(Game.window().getRenderComponent(), "Select an export format:", "Export Spritesheet", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

      try {
        JFileChooser chooser;
        String source = Editor.instance().getProjectPath();
        chooser = new JFileChooser(source != null ? source : new File(".").getCanonicalPath());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle("Export Spritesheet");
        if (answer == 0) {
          XmlExportDialog.export(spriteSheetInfo, "Spritesheet", spriteSheetInfo.getName());
        } else if (answer == 1) {
          FileFilter filter = new FileNameExtensionFilter(format.toString() + " - Image", format.toString());
          chooser.setFileFilter(filter);
          chooser.addChoosableFileFilter(filter);
          chooser.setSelectedFile(new File(spriteSheetInfo.getName() + format.toFileExtension()));

          int result = chooser.showSaveDialog(Game.window().getRenderComponent());
          if (result == JFileChooser.APPROVE_OPTION) {
            ImageSerializer.saveImage(chooser.getSelectedFile().toString(), sprite.getImage(), format);
            log.log(Level.INFO, "exported spritesheet {0} to {1}", new Object[] { spriteSheetInfo.getName(), chooser.getSelectedFile() });
          }
        }
      } catch (IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  private void exportTileset() {
    if (!(this.getOrigin() instanceof Tileset)) {
      return;
    }

    Tileset tileset = (Tileset) this.getOrigin();
    XmlExportDialog.export(tileset, "Tileset", tileset.getName(), Tileset.FILE_EXTENSION);
  }

  private void exportEmitter() {
    if (!(this.getOrigin() instanceof EmitterData)) {
      return;
    }

    EmitterData emitter = (EmitterData) this.getOrigin();
    XmlExportDialog.export(emitter, "Emitter", emitter.getName());
  }

  private void exportBlueprint() {
    if (!(this.getOrigin() instanceof Blueprint)) {
      return;
    }

    Blueprint mapObject = (Blueprint) this.getOrigin();
    XmlExportDialog.export(mapObject, "Blueprint", mapObject.getName(), Blueprint.BLUEPRINT_FILE_EXTENSION);
  }

  private void exportSound() {
    if (!(this.getOrigin() instanceof SoundResource)) {
      return;
    }

    SoundResource sound = (SoundResource) this.getOrigin();
    SoundFormat format = sound.getFormat();
    if (format == SoundFormat.UNSUPPORTED) {
      return;
    }

    FileFilter filter = new FileNameExtensionFilter(format.toString() + " - Sound", format.toString());
    try {
      JFileChooser chooser;
      String source = Editor.instance().getProjectPath();
      chooser = new JFileChooser(source != null ? source : new File(".").getCanonicalPath());
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
          log.log(Level.INFO, "exported sound {0} to {1}", new Object[] { sound.getName(), chooser.getSelectedFile() });
        }
      }
    } catch (IOException ex) {
      log.log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  private boolean canAdd() {
    if (this.getOrigin() instanceof SpritesheetResource) {
      SpritesheetResource info = (SpritesheetResource) this.getOrigin();
      String propName = PropPanel.getIdentifierBySpriteName(info.getName());
      return propName != null && !propName.isEmpty() || CreaturePanel.getCreatureSpriteName(info.getName()) != null;
    }

    return this.getOrigin() instanceof MapObject || this.getOrigin() instanceof EmitterData;
  }
}
