package de.gurkenlabs.utiliti.view.components;

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
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.dialogs.XmlExportDialog;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AssetPanelItem extends JPanel {
  private static final Logger log = Logger.getLogger(AssetPanelItem.class.getName());
  private static final int CORNER_RADIUS = 8;
  private static final int PADDING = 12;
  private static final int ICON_SIZE = 48;
  private static final int BUTTON_SIZE = 32;
  private static final Color HOVER_COLOR = new Color(255, 255, 255, 20);
  private static final Color SELECTED_COLOR = new Color(100, 150, 255, 40);
  private static final BasicStroke FOCUS_STROKE = new BasicStroke(2.0f);
  private static final Dimension PREFERRED_SIZE = new Dimension(140, 160);

  private final JLabel iconLabel;
  private final JLabel nameLabel;
  private final JPanel buttonPanel;
  private final JButton btnEdit;
  private final JButton btnDelete;
  private final JButton btnAdd;
  private final JButton btnExport;
  private final Object origin;
  private boolean isHovered;
  private boolean isSelected;

  public AssetPanelItem(Object origin) {
    this.origin = origin;
    this.iconLabel = createIconLabel();
    this.nameLabel = createNameLabel();
    this.buttonPanel = createButtonPanel();

    this.btnAdd = createStyledButton(Icons.ADD_16, "assetpanel_add");
    this.btnEdit = createStyledButton(Icons.PENCIL_16, "assetpanel_edit");
    this.btnDelete = createStyledButton(Icons.DELETE_16, "assetpanel_delete");
    this.btnExport = createStyledButton(Icons.EXPORT_16, "assetpanel_export");

    initializeComponent();
    setupLayout();
    setupEventHandlers();
    updateButtonVisibility(false);
  }

  public AssetPanelItem(Icon icon, String text, Object origin) {
    this(origin);
    setAssetData(icon, text);
  }

  private JLabel createIconLabel() {
    JLabel label = new JLabel();
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setVerticalAlignment(SwingConstants.CENTER);
    return label;
  }

  private JLabel createNameLabel() {
    JLabel label = new JLabel();
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setVerticalAlignment(SwingConstants.TOP);
    label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
    return label;
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
    return panel;
  }

  private void initializeComponent() {
    setPreferredSize(PREFERRED_SIZE);
    setMinimumSize(PREFERRED_SIZE);
    setOpaque(false);
    setFocusable(true);
    setRequestFocusEnabled(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btnAdd.setEnabled(canAdd());
  }

  private JButton createStyledButton(Icon icon, String tooltipKey) {
    JButton button = new JButton();
    buttonPanel.add(button);
    button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
    button.setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
    button.setIcon(icon);
    button.setToolTipText(Resources.strings().get(tooltipKey));
    button.setOpaque(false);
    button.setContentAreaFilled(false);
    button.setVisible(false);
    button.setBorder(DarkBorders.createLineBorder(1, 1, 1, 1));

    button.addMouseListener(new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        button.setContentAreaFilled(true);
        button.setFocusPainted(true);
      }

      @Override public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
      }

      @Override public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

      }
    });

    return button;
  }

  private void setupLayout() {
    setLayout(new BorderLayout());
    JPanel contentPanel = createContentPanel();
    add(contentPanel, BorderLayout.CENTER);
  }

  private JPanel createContentPanel() {
    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.setOpaque(false);
    contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING / 2, PADDING));

    contentPanel.add(createIconPanel(), BorderLayout.NORTH);
    contentPanel.add(createTextPanel(), BorderLayout.CENTER);
    contentPanel.add(createBottomPanel(), BorderLayout.SOUTH);

    return contentPanel;
  }

  private JPanel createIconPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.setPreferredSize(new Dimension(ICON_SIZE + 16, ICON_SIZE + 8));
    panel.add(iconLabel, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createTextPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
    panel.add(nameLabel, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createBottomPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    buttonPanel.setPreferredSize(new Dimension(getPreferredSize().width, BUTTON_SIZE));
    panel.add(buttonPanel, BorderLayout.CENTER);
    return panel;
  }

  private void setupEventHandlers() {
    setupKeyboardShortcuts();
    setupFocusHandling();
    setupMouseHandling();
    setupButtonActions();
  }

  private void setupKeyboardShortcuts() {
    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAsset");
    getActionMap().put("deleteAsset", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent ae) {
        deleteAsset();
      }
    });
  }

  private void setupFocusHandling() {
    addFocusListener(new FocusAdapter() {
      @Override public void focusGained(FocusEvent e) {
        isSelected = true;
        updateButtonVisibility(true);
        repaint();
      }

      @Override public void focusLost(FocusEvent e) {
        isSelected = false;
        updateButtonVisibility(false);
        repaint();
      }
    });
  }

  private void setupMouseHandling() {
    MouseAdapter mouseHandler = new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        isHovered = true;
        requestFocus();
        repaint();
      }

      @Override public void mouseExited(MouseEvent e) {
        isHovered = false;
        repaint();
      }

      @Override public void mouseClicked(MouseEvent e) {
        requestFocus();
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
          addEntity();
        }
      }
    };

    addMouseListener(mouseHandler);
    iconLabel.addMouseListener(mouseHandler);
    nameLabel.addMouseListener(mouseHandler);
  }

  private void setupButtonActions() {
    btnAdd.addActionListener(e -> addEntity());
    btnEdit.addActionListener(e -> editAsset());
    btnDelete.addActionListener(e -> deleteAsset());
    btnExport.addActionListener(e -> exportAsset());
  }

  private void setAssetData(Icon icon, String text) {
    iconLabel.setIcon(icon);
    nameLabel.setText(wrapText(text, 16));
    String tooltip = createTooltip(text);
    setToolTipText(tooltip);
    iconLabel.setToolTipText(tooltip);
    nameLabel.setToolTipText(tooltip);
  }

  private String createTooltip(String text) {
    StringBuilder tooltip = new StringBuilder("<html>");
    tooltip.append(String.format("<b>%s:</b> %s<br>", Resources.strings().get("assetpanel_assetname"), text));

    getDetails(origin).forEach((key, value) -> tooltip.append(String.format("<b>%s:</b> %s<br>", key, value)));

    tooltip.append("</html>");
    return tooltip.toString();
  }

  private String wrapText(String text, int maxLength) {
    if (text.length() <= maxLength) {
      return text;
    }

    StringBuilder wrapped = new StringBuilder("<html><center>");
    String[] words = text.split("\\s+");
    StringBuilder line = new StringBuilder();

    for (String word : words) {
      if (line.length() + word.length() + 1 > maxLength) {
        if (!line.isEmpty()) {
          wrapped.append(line).append("<br>");
          line = new StringBuilder();
        }
      }
      if (!line.isEmpty()) {
        line.append(" ");
      }
      line.append(word);
    }

    if (!line.isEmpty()) {
      wrapped.append(line);
    }
    wrapped.append("</center></html>");
    return wrapped.toString();
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, CORNER_RADIUS, CORNER_RADIUS);

    if (isSelected) {
      g2d.setColor(SELECTED_COLOR);
      g2d.fill(roundRect);
    } else if (isHovered) {
      g2d.setColor(HOVER_COLOR);
      g2d.fill(roundRect);
    }

    g2d.setColor(isSelected ? UIManager.getColor("Tree.selectionBorderColor") : UIManager.getColor("Component.borderColor"));
    g2d.setStroke(isSelected ? FOCUS_STROKE : new BasicStroke(1.0f));
    g2d.draw(roundRect);

    g2d.dispose();
    super.paintComponent(g);
  }

  private static Map<String, String> getDetails(Object origin) {
    Map<String, String> details = new ConcurrentHashMap<>();
    if (origin instanceof SpritesheetResource spritesheetResource) {
      details.put("Size", spritesheetResource.getWidth() + "x" + spritesheetResource.getHeight() + "px");
    }
    return details;
  }

  private void deleteAsset() {
    if (origin == null) {
      return;
    }

    String assetType = "";
    String assetName = "";

    switch (origin) {
      case SpritesheetResource spritesheetResource -> {
        assetType = "spritesheet";
        assetName = spritesheetResource.getName();
        if (confirmDelete(assetType, assetName)) {
          Editor.instance().getGameFile().getSpriteSheets().remove(spritesheetResource);
          Resources.images().clear();
          Resources.spritesheets().remove(assetName);
        }
      }
      case EmitterData emitterData -> {
        assetType = "emitter";
        assetName = emitterData.getName();
        if (confirmDelete(assetType, assetName)) {
          Editor.instance().getGameFile().getEmitters().remove(emitterData);
        }
      }
      case Blueprint blueprint -> {
        assetType = "blueprint";
        assetName = blueprint.getName();
        if (confirmDelete(assetType, assetName)) {
          Editor.instance().getGameFile().getBluePrints().remove(blueprint);
          Resources.blueprints().remove(assetName);
        }
      }
      case SoundResource soundResource -> {
        assetType = "sound";
        assetName = soundResource.getName();
        if (confirmDelete(assetType, assetName)) {
          Editor.instance().getGameFile().getSounds().remove(soundResource);
          Resources.sounds().remove(assetName);
        }
      }
      default -> {
      }
    }

    if (!assetName.isEmpty()) {
      Editor.instance().getMapComponent().reloadEnvironment();
      UI.getAssetController().refresh();
    }
  }

  private boolean confirmDelete(String assetType, String assetName) {
    return JOptionPane.OK_OPTION == getDeleteDialog(assetType, assetName);
  }

  private void addEntity() {
    if (Game.world().environment() == null || Game.world().camera() == null) {
      return;
    }

    if (origin instanceof SpritesheetResource spritesheetResource) {
      addSpriteEntity(spritesheetResource);
    } else if (origin instanceof EmitterData) {
      addEmitterEntity();
    } else if (origin instanceof Blueprint blueprint) {
      addBlueprintEntity(blueprint);
    }
  }

  private void addSpriteEntity(SpritesheetResource spritesheetResource) {
    String propName = PropPanel.getIdentifierBySpriteName(spritesheetResource.getName());
    String creatureName = CreaturePanel.getCreatureSpriteName(spritesheetResource.getName());
    if (propName == null && creatureName == null) {
      return;
    }

    MapObject mo = new MapObject();
    mo.setType(propName != null ? MapObjectType.PROP.name() : MapObjectType.CREATURE.name());
    mo.setValue(MapObjectProperty.SPRITESHEETNAME, propName != null ? propName : creatureName);

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
  }

  private void addEmitterEntity() {
    MapObject newEmitter = (MapObject) EmitterMapObjectLoader.createMapObject((EmitterData) origin);
    newEmitter.setX((int) (Game.world().camera().getFocus().getX() - newEmitter.getWidth()));
    newEmitter.setY((int) (Game.world().camera().getFocus().getY() - newEmitter.getHeight()));
    newEmitter.setId(Game.world().environment().getNextMapId());
    Editor.instance().getMapComponent().add(newEmitter);
  }

  private void addBlueprintEntity(Blueprint blueprint) {
    UndoManager.instance().beginOperation();
    try {
      List<IMapObject> newObjects = blueprint.build((int) Game.world().camera().getFocus().getX() - blueprint.getWidth() / 2,
        (int) Game.world().camera().getFocus().getY() - blueprint.getHeight() / 2);

      newObjects.forEach(obj -> Editor.instance().getMapComponent().add(obj));
      newObjects.forEach(obj -> Editor.instance().getMapComponent().setSelection(obj, false));
    } finally {
      UndoManager.instance().endOperation();
    }
  }

  private void editAsset() {
    if (!(origin instanceof SpritesheetResource)) {
      return;
    }

    SpritesheetImportPanel spritePanel = new SpritesheetImportPanel((SpritesheetResource) origin);
    int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), spritePanel, Resources.strings().get("menu_assets_editSprite"),
      JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (option != JOptionPane.OK_OPTION) {
      return;
    }

    spritePanel.getSpriteSheets().forEach(spriteFile -> {
      Editor.instance().getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(spriteFile.getName()));
      Editor.instance().getGameFile().getSpriteSheets().add(spriteFile);
    });

    Editor.instance().loadSpriteSheets(Editor.instance().getGameFile().getSpriteSheets(), true);
  }

  private void exportAsset() {
    if (origin instanceof Tileset tileset) {
      exportTileset(tileset);
    } else if (origin instanceof SpritesheetResource spritesheetResource) {
      exportSpritesheet(spritesheetResource);
    } else if (origin instanceof EmitterData emitterData) {
      exportEmitter(emitterData);
    } else if (origin instanceof Blueprint blueprint) {
      exportBlueprint(blueprint);
    } else if (origin instanceof SoundResource soundResource) {
      exportSound(soundResource);
    }
  }

  private void exportSpritesheet(SpritesheetResource spritesheetResource) {
    Spritesheet sprite = Resources.spritesheets().get(spritesheetResource.getName());
    if (sprite == null) {
      return;
    }

    ImageFormat format = sprite.getImageFormat() != ImageFormat.UNSUPPORTED ? sprite.getImageFormat() : ImageFormat.PNG;
    Object[] options = {".xml", format.toFileExtension()};

    int answer =
      JOptionPane.showOptionDialog(Game.window().getRenderComponent(), "Select an export format:", "Export Spritesheet", JOptionPane.DEFAULT_OPTION,
        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

    if (answer == 0) {
      XmlExportDialog.export(spritesheetResource, "Spritesheet", spritesheetResource.getName());
    } else if (answer == 1) {
      exportImage(sprite, format, spritesheetResource.getName());
    }
  }

  private void exportImage(Spritesheet sprite, ImageFormat format, String name) {
    JFileChooser chooser = createFileChooser(format.toString(), format.toFileExtension(), name + format.toFileExtension());
    chooser.setDialogTitle("Export Spritesheet");

    if (chooser.showSaveDialog(Game.window().getRenderComponent()) == JFileChooser.APPROVE_OPTION) {
      try {
        ImageIO.write(sprite.getImage(), format.toFileExtension(), chooser.getSelectedFile());
        log.log(Level.INFO, "exported spritesheet {0} to {1}", new Object[] {name, chooser.getSelectedFile()});
      } catch (IOException e) {
        log.log(Level.SEVERE, e.getLocalizedMessage(), e);
      }
    }
  }

  private void exportTileset(Tileset tileset) {
    XmlExportDialog.export(tileset, "Tileset", tileset.getName(), Tileset.FILE_EXTENSION);
  }

  private void exportEmitter(EmitterData emitter) {
    XmlExportDialog.export(emitter, "Emitter", emitter.getName());
  }

  private void exportBlueprint(Blueprint blueprint) {
    XmlExportDialog.export(blueprint, "Blueprint", blueprint.getName(), Blueprint.BLUEPRINT_FILE_EXTENSION);
  }

  private void exportSound(SoundResource sound) {
    if (sound.getFormat() == SoundFormat.UNSUPPORTED) {
      return;
    }

    try {
      JFileChooser chooser =
        createFileChooser(sound.getFormat().toString(), sound.getFormat().toString(), sound.getName() + sound.getFormat().toFileExtension());
      chooser.setDialogTitle("Export Sound");

      if (chooser.showSaveDialog(Game.window().getRenderComponent()) == JFileChooser.APPROVE_OPTION) {
        try (FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile().toString())) {
          fos.write(Codec.decode(sound.getData()));
          log.log(Level.INFO, "exported sound {0} to {1}", new Object[] {sound.getName(), chooser.getSelectedFile()});
        }
      }
    } catch (IOException ex) {
      log.log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  private JFileChooser createFileChooser(String description, String extension, String defaultFileName) {
    JFileChooser chooser = new JFileChooser(Editor.instance().getProjectPath().toFile());
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);

    FileFilter filter = new FileNameExtensionFilter(description + " - File", extension);
    chooser.setFileFilter(filter);
    chooser.addChoosableFileFilter(filter);
    chooser.setSelectedFile(new File(defaultFileName));

    return chooser;
  }

  private boolean canAdd() {
    if (origin instanceof SpritesheetResource spritesheetResource) {
      return PropPanel.getIdentifierBySpriteName(spritesheetResource.getName()) != null
        || CreaturePanel.getCreatureSpriteName(spritesheetResource.getName()) != null;
    }
    return origin instanceof MapObject || origin instanceof EmitterData;
  }

  private static int getDeleteDialog(String assetType, String assetName) {
    return JOptionPane.showConfirmDialog(Game.window().getRenderComponent(),
      Resources.strings().get(String.format("assetpanel_confirmdelete_%s", assetType), assetName),
      Resources.strings().get(String.format("assetpanel_confirmdelete_%s_title", assetType)), JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE);
  }

  private void updateButtonVisibility(boolean visible) {
    if (origin instanceof SpritesheetResource || origin instanceof EmitterData || origin instanceof MapObject) {
      btnAdd.setVisible(visible);
      btnDelete.setVisible(visible);
    } else if (origin instanceof Tileset || origin instanceof SoundResource) {
      btnAdd.setVisible(false);
      btnDelete.setVisible(origin instanceof SoundResource && visible);
    }
    btnEdit.setVisible(false);
    btnExport.setVisible(visible);
    btnExport.setVisible(visible);
  }
}
