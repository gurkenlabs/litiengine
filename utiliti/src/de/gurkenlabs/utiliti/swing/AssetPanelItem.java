package de.gurkenlabs.utiliti.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.utiliti.EditorScreen;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.swing.dialogs.SpritesheetImportPanel;
import java.awt.GridLayout;

public class AssetPanelItem extends JPanel {
  private static final Border normalBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
  private static final Border focusBorder = BorderFactory.createDashedBorder(UIManager.getDefaults().getColor("Tree.selectionBorderColor"));

  private final JLabel iconLabel;
  private final JTextField textField;
  private final JButton btnEdit;
  private final JButton btnDelete;
  private final JButton btnAdd;

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
        if (getOrigin() instanceof SpriteSheetInfo) {
          btnEdit.setVisible(true);
          btnAdd.setVisible(true);
          btnDelete.setVisible(true);
        }
      }

      @Override
      public void focusLost(FocusEvent e) {
        UIDefaults defaults = UIManager.getDefaults();
        setBackground(Color.DARK_GRAY);
        setForeground(defaults.getColor("Tree.foreground"));
        textField.setForeground(Color.LIGHT_GRAY);
        setBorder(normalBorder);
        if (getOrigin() instanceof SpriteSheetInfo) {
          btnEdit.setVisible(false);
          btnAdd.setVisible(false);
          btnDelete.setVisible(false);
        }
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

    JPanel panel = new JPanel(new GridLayout(1, 2, 0, 0));
    panel.setPreferredSize(new Dimension(54, 20));
    panel.setMinimumSize(new Dimension(54, 20));
    panel.setOpaque(false);
    add(panel, BorderLayout.EAST);

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
      SpritesheetImportPanel spritePanel = new SpritesheetImportPanel((SpriteSheetInfo) this.getOrigin());
      int option = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), spritePanel, Resources.get("menu_assets_editSprite"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (option != JOptionPane.OK_OPTION) {
        return;
      }

      final Collection<SpriteSheetInfo> sprites = spritePanel.getSpriteSheets();
      for (SpriteSheetInfo spriteFile : sprites) {
        EditorScreen.instance().getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(spriteFile.getName()));
        EditorScreen.instance().getGameFile().getSpriteSheets().add(spriteFile);
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

    panel.add(btnAdd);
    panel.add(btnEdit);
    panel.add(btnDelete);
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
      mo.setCustomProperty(MapObjectProperty.COLLISIONBOXWIDTH, (info.getWidth() * 0.4) + "");
      mo.setCustomProperty(MapObjectProperty.COLLISIONBOXHEIGHT, (info.getHeight() * 0.4) + "");
      mo.setCustomProperty(MapObjectProperty.COLLISION, "true");
      mo.setCustomProperty(MapObjectProperty.INDESTRUCTIBLE, "false");
      mo.setCustomProperty(MapObjectProperty.PROP_ADDSHADOW, "true");
      mo.setCustomProperty(MapObjectProperty.SPRITESHEETNAME, propName);

      EditorScreen.instance().getMapComponent().add(mo);
      return true;
    }

    return false;
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
