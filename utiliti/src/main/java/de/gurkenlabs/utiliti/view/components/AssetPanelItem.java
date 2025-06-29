package de.gurkenlabs.utiliti.view.components;

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

  // Enhanced visual constants
  private static final int CORNER_RADIUS = 8;
  private static final int PADDING = 12;
  private static final int ICON_SIZE = 48;
  private static final int BUTTON_SIZE = 32;
  private static final Color HOVER_COLOR = new Color(255, 255, 255, 20);
  private static final Color SELECTED_COLOR = new Color(100, 150, 255, 40);
  private static final BasicStroke FOCUS_STROKE = new BasicStroke(2.0f);

  private static final Dimension PREFERRED_SIZE = new Dimension(140, 160);
  private static final Dimension BUTTON_SIZE_DIM = new Dimension(BUTTON_SIZE, BUTTON_SIZE);

  private JLabel iconLabel;
  private JLabel nameLabel;
  private JPanel buttonPanel;
  private JButton btnEdit;
  private JButton btnDelete;
  private JButton btnAdd;
  private JButton btnExport;

  private final transient Object origin;
  private boolean isHovered = false;
  private boolean isSelected = false;

  public AssetPanelItem(Object origin) {
    this.origin = origin;
    initializeComponent();
    setupLayout();
    setupEventHandlers();
  }

  public AssetPanelItem(Icon icon, String text, Object origin) {
    this(origin);
    setAssetData(icon, text);
  }

  private void initializeComponent() {
    setPreferredSize(PREFERRED_SIZE);
    setMinimumSize(PREFERRED_SIZE);
    setOpaque(false);
    setFocusable(true);
    setRequestFocusEnabled(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // Initialize components
    iconLabel = new JLabel();
    iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
    iconLabel.setVerticalAlignment(SwingConstants.CENTER);

    nameLabel = new JLabel();
    nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
    nameLabel.setVerticalAlignment(SwingConstants.TOP);
    nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 11f));

    buttonPanel = new JPanel();
    buttonPanel.setOpaque(false);
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));

    // Initialize buttons with improved styling
    btnAdd = createStyledButton(Icons.ADD_16, "assetpanel_add");
    btnEdit = createStyledButton(Icons.PENCIL_16, "assetpanel_edit");
    btnDelete = createStyledButton(Icons.DELETE_16, "assetpanel_delete");
    btnExport = createStyledButton(Icons.EXPORT_16, "assetpanel_export");

    btnAdd.setEnabled(canAdd());
  }

  private JButton createStyledButton(Icon icon, String tooltipKey) {
    JButton button = new JButton(icon);
    button.setToolTipText(Resources.strings().get(tooltipKey));
    button.setOpaque(false);
    button.setContentAreaFilled(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setVisible(false);


    // Add hover effect
    button.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        button.setContentAreaFilled(true);
        button.setBackground(new Color(255, 255, 255, 30));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        button.setContentAreaFilled(false);
      }
    });

    buttonPanel.add(button);
    return button;
  }

  private void setupLayout() {
    setLayout(new BorderLayout());

    // Main content panel with padding
    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.setOpaque(false);
    contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING / 2, PADDING));

    // Icon panel - centered and with proper sizing
    JPanel iconPanel = new JPanel(new BorderLayout());
    iconPanel.setOpaque(false);
    iconPanel.setPreferredSize(new Dimension(ICON_SIZE + 16, ICON_SIZE + 8));
    iconPanel.add(iconLabel, BorderLayout.CENTER);

    // Text panel with proper sizing
    JPanel textPanel = new JPanel(new BorderLayout());
    textPanel.setOpaque(false);
    textPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
    textPanel.add(nameLabel, BorderLayout.CENTER);

    // Button panel at bottom
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setOpaque(false);
    buttonPanel.setPreferredSize(new Dimension(getPreferredSize().width, BUTTON_SIZE));
    bottomPanel.add(buttonPanel, BorderLayout.CENTER);

    contentPanel.add(iconPanel, BorderLayout.NORTH);
    contentPanel.add(textPanel, BorderLayout.CENTER);
    contentPanel.add(bottomPanel, BorderLayout.SOUTH);

    add(contentPanel, BorderLayout.CENTER);
  }

  private void setupEventHandlers() {
    // Keyboard shortcuts
    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAsset");
    getActionMap().put("deleteAsset", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        deleteAsset();
      }
    });

    // Focus handling
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        isSelected = true;
        updateButtonVisibility(true);
        repaint();
      }

      @Override
      public void focusLost(FocusEvent e) {
        isSelected = false;
        updateButtonVisibility(false);
        repaint();
      }
    });

    // Mouse handling
    MouseAdapter mouseHandler = new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        isHovered = true;
        requestFocus();
        repaint();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        isHovered = false;
        repaint();
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        requestFocus();
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
          if (addEntity()) {
            e.consume();
          }
        }
      }
    };

    addMouseListener(mouseHandler);
    iconLabel.addMouseListener(mouseHandler);
    nameLabel.addMouseListener(mouseHandler);

    // Button actions
    btnAdd.addActionListener(e -> addEntity());
    btnEdit.addActionListener(e -> editAsset());
    btnDelete.addActionListener(e -> deleteAsset());
    btnExport.addActionListener(e -> exportAsset());
  }

  private void setAssetData(Icon icon, String text) {
    iconLabel.setIcon(icon);
    nameLabel.setText(wrapText(text, 16));

    // Create detailed tooltip
    StringBuilder tooltip = new StringBuilder("<html>");
    tooltip.append(String.format("<b>%s:</b> %s<br>",
      Resources.strings().get("assetpanel_assetname"), text));

    Map<String, String> details = getDetails(origin);
    for (Map.Entry<String, String> entry : details.entrySet()) {
      tooltip.append(String.format("<b>%s:</b> %s<br>", entry.getKey(), entry.getValue()));
    }
    tooltip.append("</html>");

    String tooltipText = tooltip.toString();
    setToolTipText(tooltipText);
    iconLabel.setToolTipText(tooltipText);
    nameLabel.setToolTipText(tooltipText);
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
        if (line.length() > 0) {
          wrapped.append(line).append("<br>");
          line = new StringBuilder();
        }
      }
      if (line.length() > 0) {
        line.append(" ");
      }
      line.append(word);
    }

    if (line.length() > 0) {
      wrapped.append(line);
    }

    wrapped.append("</center></html>");
    return wrapped.toString();
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth();
    int height = getHeight();

    // Create rounded rectangle shape
    RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, width - 1, height - 1,
      CORNER_RADIUS, CORNER_RADIUS);

    // Background
    if (isSelected) {
      g2d.setColor(SELECTED_COLOR);
      g2d.fill(roundRect);
    } else if (isHovered) {
      g2d.setColor(HOVER_COLOR);
      g2d.fill(roundRect);
    }

    // Border
    if (isSelected) {
      g2d.setColor(UIManager.getColor("Tree.selectionBorderColor"));
      g2d.setStroke(FOCUS_STROKE);
    } else {
      g2d.setColor(UIManager.getColor("Component.borderColor"));
      g2d.setStroke(new BasicStroke(1.0f));
    }
    g2d.draw(roundRect);

    g2d.dispose();
    super.paintComponent(g);
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

  // [Keep all the existing methods: deleteAsset(), addEntity(), editAsset(), exportAsset(), etc.]
  // [The implementation remains the same as in your original code]

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

  private void updateButtonVisibility(boolean visible) {
    if (getOrigin() instanceof SpritesheetResource || getOrigin() instanceof EmitterData) {
      btnEdit.setVisible(false);
      btnAdd.setVisible(visible);
      btnDelete.setVisible(visible);
      btnExport.setVisible(visible);
    } else if (getOrigin() instanceof Tileset) {
      btnEdit.setVisible(false);
      btnAdd.setVisible(false);
      btnDelete.setVisible(false);
      btnExport.setVisible(visible);
    } else if (getOrigin() instanceof MapObject) {
      btnEdit.setVisible(false);
      btnAdd.setVisible(visible);
      btnDelete.setVisible(visible);
      btnExport.setVisible(visible);
    } else if (getOrigin() instanceof SoundResource) {
      btnEdit.setVisible(false);
      btnAdd.setVisible(false);
      btnDelete.setVisible(visible);
      btnExport.setVisible(visible);
    }
  }
}
