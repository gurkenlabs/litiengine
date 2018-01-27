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
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.particles.xml.EmitterData;
import de.gurkenlabs.util.io.ImageSerializer;
import de.gurkenlabs.util.io.XmlUtilities;
import de.gurkenlabs.utiliti.EditorScreen;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.swing.dialogs.SpritesheetImportPanel;

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

  private final Object origin;

  public AssetPanelItem() {
    this(null);
  }

  public AssetPanelItem(Object origin) {
    setPreferredSize(new Dimension(64, 100));
    this.origin = origin;
    this.setBackground(Color.DARK_GRAY);
    this.setBorder(normalBorder);

    this.getInputMap(JPanel.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAsset");
    this.getActionMap().put("deleteAsset", new AbstractAction() {
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

        // TODO: We might need to provide multiple JPanels that contain the buttons for
        // a certain usage and swap them out
        if (getOrigin() instanceof SpriteSheetInfo) {
          btnEdit.setVisible(true);
          btnAdd.setVisible(true);
          btnDelete.setVisible(true);
          btnExport.setVisible(true);
        } else if (getOrigin() instanceof Tileset) {
          btnEdit.setVisible(false);
          btnAdd.setVisible(false);
          btnDelete.setVisible(false);
          btnExport.setVisible(true);
        } else if (getOrigin() instanceof EmitterData) {
          btnEdit.setVisible(true);
          btnAdd.setVisible(true);
          btnDelete.setVisible(true);
          btnExport.setVisible(true);
        }
      }

      @Override
      public void focusLost(FocusEvent e) {
        UIDefaults defaults = UIManager.getDefaults();
        setBackground(Color.DARK_GRAY);
        setForeground(defaults.getColor("Tree.foreground"));
        textField.setForeground(Color.LIGHT_GRAY);
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
        if (e.getClickCount() == 2 && addEntity()) {
          e.consume();
        }
      }
    });

    this.textField = new JTextField();
    add(this.textField, BorderLayout.SOUTH);
    this.textField.setColumns(10);
    this.textField.setHorizontalAlignment(JTextField.CENTER);
    this.textField.setForeground(Color.LIGHT_GRAY);
    this.textField.setBackground(null);
    this.textField.setBorder(null);
    this.textField.setEditable(false);

    this.setMinimumSize(new Dimension(this.iconLabel.getWidth(), this.iconLabel.getHeight() + this.textField.getHeight()));

    GridLayout buttonGridLayout = new GridLayout(0, 4, 0, 0);
    buttonPanel = new JPanel(buttonGridLayout);
    buttonPanel.setPreferredSize(new Dimension(64, 20));
    buttonPanel.setMinimumSize(new Dimension(64, 20));
    buttonPanel.setOpaque(false);
    add(buttonPanel, BorderLayout.EAST);

    btnAdd = new JButton("");
    btnAdd.setToolTipText("Add Entity");
    btnAdd.addActionListener(e -> this.addEntity());
    btnAdd.setMaximumSize(new Dimension(16, 16));
    btnAdd.setMinimumSize(new Dimension(16, 16));
    btnAdd.setPreferredSize(new Dimension(16, 16));
    btnAdd.setOpaque(false);
    btnAdd.setIcon(new ImageIcon(Resources.getImage("addx12.png")));
    btnAdd.setVisible(false);
    btnAdd.setEnabled(canAdd());

    btnEdit = new JButton("");
    btnEdit.setToolTipText("Edit Asset");
    btnEdit.addActionListener(e -> {
      if (!(this.getOrigin() instanceof SpriteSheetInfo)) {
        return;
      }
      SpritesheetImportPanel spritePanel = new SpritesheetImportPanel((SpriteSheetInfo) this.getOrigin());
      int option = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), spritePanel, Resources.get("menu_assets_editSprite"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (option != JOptionPane.OK_OPTION) {
        return;
      }

      final Collection<SpriteSheetInfo> sprites = spritePanel.getSpriteSheets();
      for (SpriteSheetInfo spriteFile : sprites) {
        int index = -1;
        Optional<SpriteSheetInfo> old = EditorScreen.instance().getGameFile().getSpriteSheets().stream().filter((x -> x.getName().equals(spriteFile.getName()))).findFirst();
        if (old.isPresent()) {
          index = EditorScreen.instance().getGameFile().getSpriteSheets().indexOf(old.get());
          EditorScreen.instance().getGameFile().getSpriteSheets().remove(index);
        }

        EditorScreen.instance().getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(spriteFile.getName()));
        if (index != -1) {
          EditorScreen.instance().getGameFile().getSpriteSheets().add(index, spriteFile);
        } else {
          EditorScreen.instance().getGameFile().getSpriteSheets().add(spriteFile);
        }
      }

      // TODO: in case the asset has been renamed: update all props that uses the
      // asset to use the new name (assets are treated as reference by name)
      EditorScreen.instance().loadSpriteSheets(EditorScreen.instance().getGameFile().getSpriteSheets(), true);
    });
    btnEdit.setMaximumSize(new Dimension(16, 16));
    btnEdit.setMinimumSize(new Dimension(16, 16));
    btnEdit.setPreferredSize(new Dimension(16, 16));
    btnEdit.setOpaque(false);
    btnEdit.setIcon(new ImageIcon(Resources.getImage("pencil.png")));
    btnEdit.setVisible(false);

    btnDelete = new JButton("");
    btnDelete.setToolTipText("Delete Asset");
    btnDelete.addActionListener(e -> this.deleteAsset());
    btnDelete.setMaximumSize(new Dimension(16, 16));
    btnDelete.setMinimumSize(new Dimension(16, 16));
    btnDelete.setPreferredSize(new Dimension(16, 16));
    btnDelete.setOpaque(false);
    btnDelete.setIcon(new ImageIcon(Resources.getImage("button-deletex12.png")));
    btnDelete.setVisible(false);

    btnExport = new JButton("");
    btnExport.setToolTipText("Export Asset");
    btnExport.addActionListener(e -> this.export());
    btnExport.setMaximumSize(new Dimension(16, 16));
    btnExport.setMinimumSize(new Dimension(16, 16));
    btnExport.setPreferredSize(new Dimension(16, 16));
    btnExport.setOpaque(false);
    btnExport.setIcon(new ImageIcon(Resources.getImage("export.png")));
    btnExport.setVisible(false);

    buttonPanel.add(btnAdd);
    buttonPanel.add(btnEdit);
    buttonPanel.add(btnDelete);
    buttonPanel.add(btnExport);
  }

  public AssetPanelItem(Icon icon, String text, Object origin) {
    this(origin);
    this.iconLabel.setHorizontalAlignment(JLabel.CENTER);
    this.iconLabel.setIcon(icon);
    this.textField.setText(text);
  }

  public Object getOrigin() {
    return this.origin;
  }

  private void deleteAsset() {
    if (getOrigin() instanceof SpriteSheetInfo) {
      SpriteSheetInfo info = (SpriteSheetInfo) getOrigin();
      int n = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), "Do you really want to delete the spritesheet [" + info.getName() + "]?\n Entities that use the sprite won't be rendered anymore!", "Delete Spritesheet?", JOptionPane.YES_NO_OPTION);

      if (n == JOptionPane.OK_OPTION) {
        EditorScreen.instance().getGameFile().getSpriteSheets().remove(getOrigin());
        ImageCache.clearAll();
        Spritesheet.remove(info.getName());
        EditorScreen.instance().getMapComponent().reloadEnvironment();

        Program.getAssetTree().forceUpdate();
      }
    } else if (getOrigin() instanceof EmitterData) {
      EmitterData emitter = (EmitterData) getOrigin();
      int n = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), "Do you really want to delete the emitter [" + emitter.getName() + "]?\n Entities that use the emitter won't be rendered anymore!", "Delete Emitter?", JOptionPane.YES_NO_OPTION);

      if (n == JOptionPane.OK_OPTION) {
        EditorScreen.instance().getGameFile().getEmitters().remove(getOrigin());
        EditorScreen.instance().getMapComponent().reloadEnvironment();

        Program.getAssetTree().forceUpdate();
      }
    }
  }

  private boolean addEntity() {
    // TODO: experimental code... this needs to be refactored with issue #66
    if (this.getOrigin() instanceof SpriteSheetInfo) {
      SpriteSheetInfo info = (SpriteSheetInfo) this.getOrigin();
      String propName = Prop.getNameBySpriteName(info.getName());
      if (propName == null) {
        return false;
      }

      MapObject mo = new MapObject();
      mo.setType(MapObjectType.PROP.name());
      mo.setX((int) Game.getCamera().getFocus().getX());
      mo.setY((int) Game.getCamera().getFocus().getY());
      mo.setWidth((int) info.getWidth());
      mo.setHeight((int) info.getHeight());
      mo.setId(Game.getEnvironment().getNextMapId());
      mo.setName("");
      mo.setCustomProperty(MapObjectProperty.COLLISIONBOX_WIDTH, (info.getWidth() * 0.4) + "");
      mo.setCustomProperty(MapObjectProperty.COLLISIONBOX_HEIGHT, (info.getHeight() * 0.4) + "");
      mo.setCustomProperty(MapObjectProperty.COLLISION, "true");
      mo.setCustomProperty(MapObjectProperty.PROP_INDESTRUCTIBLE, "false");
      mo.setCustomProperty(MapObjectProperty.PROP_ADDSHADOW, "true");
      mo.setCustomProperty(MapObjectProperty.SPRITESHEETNAME, propName);

      EditorScreen.instance().getMapComponent().add(mo);
      return true;
    } else if (this.getOrigin() instanceof EmitterData) {
      // TODO @matthias: implement this when the emitter tool is fully integrated and
      // there is a way to add a map object from EmitterData
    }

    return false;
  }

  private void export() {
    if (this.getOrigin() instanceof Tileset) {
      this.exportTileset();
      return;
    } else if (this.getOrigin() instanceof SpriteSheetInfo) {
      this.exportSpritesheet();
      return;
    } else if (this.getOrigin() instanceof EmitterData) {
      this.exportEmitter();
      return;
    }
  }

  private void exportSpritesheet() {
    if (this.getOrigin() instanceof SpriteSheetInfo) {
      SpriteSheetInfo spriteSheetInfo = (SpriteSheetInfo) this.getOrigin();

      Spritesheet sprite = Spritesheet.find(spriteSheetInfo.getName());
      if (sprite == null) {
        return;
      }

      ImageFormat format = sprite.getImageFormat() != ImageFormat.UNDEFINED ? sprite.getImageFormat() : ImageFormat.PNG;

      Object[] options = { ".xml", format.toExtension() };
      int answer = JOptionPane.showOptionDialog(Game.getScreenManager().getRenderComponent(), "Select an export format:", "Export Spritesheet", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

      try {
        JFileChooser chooser;
        String source = EditorScreen.instance().getProjectPath();
        chooser = new JFileChooser(source != null ? source : new File(".").getCanonicalPath());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle("Export Spritesheet");
        if (answer == 0) {
          FileFilter filter = new FileNameExtensionFilter(".xml - Spritesheet XML", "xml");
          chooser.setFileFilter(filter);
          chooser.addChoosableFileFilter(filter);
          chooser.setSelectedFile(new File(spriteSheetInfo.getName() + ".xml"));
          int result = chooser.showSaveDialog(Game.getScreenManager().getRenderComponent());
          if (result == JFileChooser.APPROVE_OPTION) {
            String newFile = XmlUtilities.save(spriteSheetInfo, chooser.getSelectedFile().toString(), "xml");
            log.log(Level.INFO, "exported spritesheet {0} to {1}", new Object[] { spriteSheetInfo.getName(), newFile });
          }
        } else if (answer == 1) {
          FileFilter filter = new FileNameExtensionFilter(format.toString() + " - Image", format.toString());
          chooser.setFileFilter(filter);
          chooser.addChoosableFileFilter(filter);
          chooser.setSelectedFile(new File(spriteSheetInfo.getName() + format.toExtension()));

          int result = chooser.showSaveDialog(Game.getScreenManager().getRenderComponent());
          if (result == JFileChooser.APPROVE_OPTION) {
            ImageSerializer.saveImage(chooser.getSelectedFile().toString(), sprite.getImage(), format);
            log.log(Level.INFO, "exported spritesheet {0} to {1}", new Object[] { spriteSheetInfo.getName(), chooser.getSelectedFile().toString() });
          }
        }
      } catch (IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  private void exportTileset() {
    if (this.getOrigin() instanceof Tileset) {
      Tileset tileset = (Tileset) this.getOrigin();
      JFileChooser chooser;
      try {
        String source = EditorScreen.instance().getProjectPath();
        chooser = new JFileChooser(source != null ? source : new File(".").getCanonicalPath());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle("Export Tileset");
        FileFilter filter = new FileNameExtensionFilter("tsx - Tileset XML", Tileset.FILE_EXTENSION);
        chooser.setFileFilter(filter);
        chooser.addChoosableFileFilter(filter);
        chooser.setSelectedFile(new File(tileset.getName() + "." + Tileset.FILE_EXTENSION));

        int result = chooser.showSaveDialog(Game.getScreenManager().getRenderComponent());
        if (result == JFileChooser.APPROVE_OPTION) {
          String newFile = XmlUtilities.save(tileset, chooser.getSelectedFile().toString(), Tileset.FILE_EXTENSION);
          log.log(Level.INFO, "exported tileset {0} to {1}", new Object[] { tileset.getName(), newFile });
        }
      } catch (IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  private void exportEmitter() {
    if (this.getOrigin() instanceof EmitterData) {
      EmitterData emitter = (EmitterData) this.getOrigin();
      JFileChooser chooser;
      try {
        String source = EditorScreen.instance().getProjectPath();
        chooser = new JFileChooser(source != null ? source : new File(".").getCanonicalPath());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle("Export Emitter");
        FileFilter filter = new FileNameExtensionFilter("Emitter XML", "xml");
        chooser.setFileFilter(filter);
        chooser.addChoosableFileFilter(filter);
        chooser.setSelectedFile(new File(emitter.getName() + ".xml"));

        int result = chooser.showSaveDialog(Game.getScreenManager().getRenderComponent());
        if (result == JFileChooser.APPROVE_OPTION) {
          String newFile = XmlUtilities.save(emitter, chooser.getSelectedFile().toString(), "xml");
          log.log(Level.INFO, "exported emitter {0} to {1}", new Object[] { emitter.getName(), newFile });
        }
      } catch (IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  private boolean canAdd() {
    if (this.getOrigin() != null && this.getOrigin() instanceof SpriteSheetInfo) {
      SpriteSheetInfo info = (SpriteSheetInfo) this.getOrigin();
      String propName = Prop.getNameBySpriteName(info.getName());
      if (propName == null) {
        return false;
      }

      return true;
    }

    return false;
  }
}
