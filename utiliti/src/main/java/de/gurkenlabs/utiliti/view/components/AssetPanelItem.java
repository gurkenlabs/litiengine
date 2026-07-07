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
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundFormat;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.io.Codec;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.tool.AssetTransferable;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.datatransfer.Transferable;
import de.gurkenlabs.utiliti.view.dialogs.XmlExportDialog;
import de.gurkenlabs.utiliti.view.menus.AssetPanelItemPopupMenu;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AssetPanelItem extends JPanel {
  private static final Logger log = Logger.getLogger(AssetPanelItem.class.getName());
  private static final int CORNER_RADIUS = 8;
  private static final int PADDING = 8;
  private static final int ICON_SIZE = 64;
  private static final int BUTTON_SIZE = 24;
  private static final Color HOVER_COLOR = Style.COLOR_CARD_HOVER;
  private static final Color SELECTED_COLOR = Style.COLOR_CARD_SELECTED;
  private static final BasicStroke FOCUS_STROKE = new BasicStroke(2.0f);
  private static final BasicStroke BORDER_STROKE = new BasicStroke(1.0f);
  private static final Dimension PREFERRED_SIZE = new Dimension(118, 118);

  private final JLabel iconLabel;
  private final JLabel nameLabel;
  private final JLabel detailsLabel;
  private final JPanel iconPanel;
  private final JPanel buttonPanel;
  private final JButton btnEdit;
  private final JButton btnDelete;
  private final JButton btnAdd;
  private final JButton btnExport;
  private final Object origin;
  private String assetName;
  private boolean isHovered;
  private boolean isSelected;
  private boolean compact;
  private Icon rawIcon;
  private int cardSize = PREFERRED_SIZE.width;
  private Consumer<AssetPanelItem> focusCallback;

  public AssetPanelItem(Object origin) {
    this.origin = origin;
    this.iconLabel = createIconLabel();
    this.nameLabel = createNameLabel();
    this.detailsLabel = createDetailsLabel();
    this.iconPanel = createIconPanel();
    this.buttonPanel = createButtonPanel();

    this.btnAdd = createStyledButton(Icons.ADD_16, "assetpanel_add");
    this.btnEdit = createStyledButton(Icons.PENCIL_16, "assetpanel_edit");
    this.btnExport = createStyledButton(Icons.EXPORT_16, "assetpanel_export");
    this.btnDelete = createStyledButton(Icons.DELETE_16, "assetpanel_delete");

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
    label.setForeground(Style.COLOR_TEXT);
    return label;
  }

  private JLabel createDetailsLabel() {
    JLabel label = new JLabel();
    label.setHorizontalAlignment(SwingConstants.LEFT);
    label.setFont(label.getFont().deriveFont(Font.PLAIN, 10f));
    label.setForeground(Style.COLOR_SUBTEXT);
    return label;
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
    return panel;
  }

  private void initializeComponent() {
    setPreferredSize(new Dimension(cardSize, cardSize));
    setMinimumSize(new Dimension(cardSize, cardSize));
    setOpaque(false);
    setFocusable(true);
    setRequestFocusEnabled(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    setTransferHandler(new TransferHandler() {
      @Override public int getSourceActions(JComponent c) {
        return COPY;
      }
      @Override protected Transferable createTransferable(JComponent c) {
        return new AssetTransferable(origin);
      }
    });
    btnAdd.setEnabled(canAdd());
  }

  private JButton createStyledButton(Icon icon, String tooltipKey) {
    JButton button = Style.iconButton(icon);
    buttonPanel.add(button);
    button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
    button.setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
    button.setToolTipText(Resources.strings().get(tooltipKey));
    button.setVisible(false);
    return button;
  }

  private void setupLayout() {
    if (compact) {
      setLayout(new BorderLayout(6, 0));
      iconLabel.setHorizontalAlignment(SwingConstants.LEFT);
      iconLabel.setPreferredSize(new Dimension(40, 40));
      iconLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
      nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
      JPanel centerPanel = new JPanel(new BorderLayout());
      centerPanel.setOpaque(false);
      centerPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));
      centerPanel.add(nameLabel, BorderLayout.CENTER);
      centerPanel.add(detailsLabel, BorderLayout.SOUTH);
      add(iconLabel, BorderLayout.WEST);
      add(centerPanel, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.EAST);
    } else {
      setLayout(new BorderLayout());
      iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
      iconLabel.setPreferredSize(null);
      iconLabel.setBorder(null);
      nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
      if (iconLabel.getParent() != null) {
        iconLabel.getParent().remove(iconLabel);
      }
      iconPanel.add(iconLabel, BorderLayout.CENTER);
      JPanel contentPanel = createContentPanel();
      add(contentPanel, BorderLayout.CENTER);
    }
  }

  private JPanel createContentPanel() {
    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.setOpaque(false);
    contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING / 2, PADDING));

    contentPanel.add(iconPanel, BorderLayout.NORTH);
    contentPanel.add(createTextPanel(), BorderLayout.CENTER);
    contentPanel.add(createBottomPanel(), BorderLayout.SOUTH);

    return contentPanel;
  }

  private JPanel createIconPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    int iconArea = Math.max(8, cardSize - 2 * PADDING - 20);
    panel.setPreferredSize(new Dimension(iconArea + 8, iconArea + 2));
    panel.add(iconLabel, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createTextPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));
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

    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "addAsset");
    getActionMap().put("addAsset", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent ae) {
        if (canAdd()) {
          addEntity();
        }
      }
    });

    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editAsset");
    getActionMap().put("editAsset", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent ae) {
        editAsset();
      }
    });

    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "exportAsset");
    getActionMap().put("exportAsset", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent ae) {
        exportAsset();
      }
    });
  }

  private void setupFocusHandling() {
    addFocusListener(new FocusAdapter() {
      @Override public void focusGained(FocusEvent e) {
        isSelected = true;
        updateButtonVisibility(true);
        if (focusCallback != null) {
          focusCallback.accept(AssetPanelItem.this);
        }
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
        updateButtonVisibility(true);
        repaint();
      }

      @Override public void mouseExited(MouseEvent e) {
        Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), AssetPanelItem.this);
        if (contains(p)) {
          return;
        }
        isHovered = false;
        updateButtonVisibility(false);
        repaint();
      }

      @Override public void mousePressed(MouseEvent e) {
        requestFocus();
        maybeShowPopup(e);
      }

      @Override public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
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
    buttonPanel.addMouseListener(mouseHandler);
    btnAdd.addMouseListener(mouseHandler);
    btnEdit.addMouseListener(mouseHandler);
    btnExport.addMouseListener(mouseHandler);
    btnDelete.addMouseListener(mouseHandler);
  }

  private void maybeShowPopup(MouseEvent e) {
    if (!e.isPopupTrigger()) {
      return;
    }
    AssetPanelItemPopupMenu menu = new AssetPanelItemPopupMenu(this);
    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), this);
    menu.show(this, p.x, p.y);
  }

  private void setupButtonActions() {
    btnAdd.addActionListener(e -> addEntity());
    btnEdit.addActionListener(e -> editAsset());
    btnDelete.addActionListener(e -> deleteAsset());
    btnExport.addActionListener(e -> exportAsset());
  }

  public String getName() {
    return assetName;
  }

  private void setAssetData(Icon icon, String text) {
    this.assetName = text;
    this.rawIcon = icon;
    updateScaledIcon();
    nameLabel.setText(wrapText(text, 16));
    detailsLabel.setText(getDetailsSummary());
    String tooltip = createTooltip(text);
    setToolTipText(tooltip);
    iconLabel.setToolTipText(tooltip);
    nameLabel.setToolTipText(tooltip);
  }

  private void updateScaledIcon() {
    if (rawIcon == null) {
      iconLabel.setIcon(null);
      return;
    }
    int iconArea = compact ? 32 : Math.max(8, cardSize - 2 * PADDING - 20);
    if (rawIcon instanceof ImageIcon imgIcon && imgIcon.getImage() instanceof java.awt.image.BufferedImage bi) {
      java.awt.image.BufferedImage scaled = de.gurkenlabs.litiengine.util.Imaging.scale(bi, iconArea, iconArea, true);
      if (scaled != null) {
        iconLabel.setIcon(new ImageIcon(scaled));
        return;
      }
    }
    iconLabel.setIcon(rawIcon);
  }

  private String createTooltip(String text) {
    StringBuilder tooltip = new StringBuilder("<html>");
    tooltip.append(String.format("<b>%s:</b> %s<br>", Resources.strings().get("assetpanel_assetname"), text));

    getDetails(origin).forEach((key, value) -> tooltip.append(String.format("<b>%s:</b> %s<br>", key, value)));

    tooltip.append("</html>");
    return tooltip.toString();
  }

  private String wrapText(String text, int maxLength) {
    if (text == null || text.length() <= maxLength) {
      return text;
    }

    StringBuilder wrapped = new StringBuilder("<html><center>");
    String[] words = text.split("\\s+");
    StringBuilder line = new StringBuilder();

    for (String word : words) {
      if (line.length() + word.length() + 1 > maxLength && !line.isEmpty()) {
          wrapped.append(line).append("<br>");
          line = new StringBuilder();
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

    RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, getWidth() - 1f, getHeight() - 1f, CORNER_RADIUS, CORNER_RADIUS);

    g2d.setColor(Style.COLOR_SURFACE);
    g2d.fill(roundRect);
    if (isSelected) {
      g2d.setColor(SELECTED_COLOR);
      g2d.fill(roundRect);
    } else if (isHovered) {
      g2d.setColor(HOVER_COLOR);
      g2d.fill(roundRect);
    }

    g2d.setColor(isSelected ? Style.COLOR_ACCENT_BLUE : Style.COLOR_BORDER);
    g2d.setStroke(isSelected ? FOCUS_STROKE : BORDER_STROKE);
    g2d.draw(roundRect);

    g2d.dispose();
    super.paintComponent(g);
  }

  public static Map<String, String> getDetails(Object origin) {
    Map<String, String> details = new java.util.LinkedHashMap<>();
    if (origin instanceof SpritesheetResource spritesheetResource) {
      details.put("Size", spritesheetResource.getWidth() + "x" + spritesheetResource.getHeight() + "px");
    } else if (origin instanceof Animation animation) {
      details.put("Frames", String.valueOf(animation.getKeyframes().size()));
      details.put("Duration", animation.getTotalDuration() + "ms");
      if (animation.getSpritesheet() != null) {
        details.put("Spritesheet", animation.getSpritesheet().getName());
      }
    }
    return details;
  }

  public Object getOrigin() {
    return origin;
  }

  public String getDetailsSummary() {
    Map<String, String> details = getDetails(origin);
    if (details.isEmpty()) {
      return "";
    }
    return String.join("  •  ", details.entrySet().stream()
        .map(entry -> entry.getKey() + ": " + entry.getValue())
        .toList());
  }

  public void setFocusCallback(Consumer<AssetPanelItem> focusCallback) {
    this.focusCallback = focusCallback;
  }

  public void setCompact(boolean compact) {
    if (this.compact == compact) {
      return;
    }
    this.compact = compact;
    removeAll();
    setupLayout();
    setPreferredSize(compact ? new Dimension(PREFERRED_SIZE.width, 48) : new Dimension(cardSize, cardSize));
    setMinimumSize(compact ? new Dimension(PREFERRED_SIZE.width, 48) : new Dimension(cardSize, cardSize));
    iconLabel.setPreferredSize(compact ? new Dimension(40, 40) : null);
    iconLabel.setHorizontalAlignment(compact ? SwingConstants.LEFT : SwingConstants.CENTER);
    nameLabel.setHorizontalAlignment(compact ? SwingConstants.LEFT : SwingConstants.CENTER);
    updateScaledIcon();
    revalidate();
    repaint();
  }

  public void setCardSize(int cardSize) {
    this.cardSize = cardSize;
    if (!compact) {
      setPreferredSize(new Dimension(cardSize, cardSize));
      setMinimumSize(new Dimension(cardSize, cardSize));
      int iconArea = Math.max(8, cardSize - 2 * PADDING - 20);
      iconPanel.setPreferredSize(new Dimension(iconArea + 8, iconArea + 2));
      updateScaledIcon();
      revalidate();
      repaint();
    }
  }

  public boolean isCompact() {
    return compact;
  }

  public void deleteAsset() {
    if (origin == null) {
      return;
    }

    String assetType = "";
    String deletedAssetName = "";
    boolean deleted = false;

    switch (origin) {
      case SpritesheetResource spritesheetResource -> {
        assetType = "spritesheet";
        deletedAssetName = spritesheetResource.getName();
        if (confirmDelete(assetType, deletedAssetName)) {
          Editor.instance().getGameFile().getSpriteSheets().remove(spritesheetResource);
          Resources.images().clear();
          Resources.spritesheets().remove(deletedAssetName);
          deleted = true;
        }
      }
      case EmitterAttributes emitterData -> {
        assetType = "emitter";
        deletedAssetName = emitterData.getName();
        if (confirmDelete(assetType, deletedAssetName)) {
          Editor.instance().getGameFile().getEmitters().remove(emitterData);
          deleted = true;
        }
      }
      case Blueprint blueprint -> {
        assetType = "blueprint";
        deletedAssetName = blueprint.getName();
        if (confirmDelete(assetType, deletedAssetName)) {
          Editor.instance().getGameFile().getBluePrints().remove(blueprint);
          Resources.blueprints().remove(deletedAssetName);
          deleted = true;
        }
      }
      case SoundResource soundResource -> {
        assetType = "sound";
        deletedAssetName = soundResource.getName();
        if (confirmDelete(assetType, deletedAssetName)) {
          Editor.instance().getGameFile().getSounds().remove(soundResource);
          Resources.sounds().remove(deletedAssetName);
          deleted = true;
        }
      }
      case Animation animation -> {
        assetType = "animation";
        deletedAssetName = animation.getName();
        if (confirmDelete(assetType, deletedAssetName)) {
          Resources.animations().remove(deletedAssetName);
          deleted = true;
        }
      }
      default -> {
      }
    }

    if (deleted && !deletedAssetName.isEmpty()) {
      Editor.instance().getMapComponent().reloadEnvironment();
      UI.getAssetController().refresh();
    }
  }

  private boolean confirmDelete(String assetType, String assetName) {
    return JOptionPane.OK_OPTION == getDeleteDialog(assetType, assetName);
  }

  public void addEntity() {
    if (Game.world().environment() == null || Game.world().camera() == null) {
      return;
    }

    if (origin instanceof SpritesheetResource spritesheetResource) {
      addSpriteEntity(spritesheetResource);
    } else if (origin instanceof EmitterAttributes) {
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
    MapObject newEmitter = (MapObject) EmitterMapObjectLoader.createMapObject((EmitterAttributes) origin);
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

  public void editAsset() {
    if (origin instanceof SpritesheetResource spritesheetResource) {
      editSpritesheet(spritesheetResource);
    } else if (origin instanceof Animation animation) {
      editAnimation(animation);
    }
  }

  private void editSpritesheet(SpritesheetResource spritesheetResource) {
    SpritesheetImportPanel spritePanel = new SpritesheetImportPanel(spritesheetResource);
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

  private void editAnimation(Animation animation) {
    AnimationEditPanel panel = new AnimationEditPanel(animation);
    try {
      int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), panel,
        Resources.strings().get("menu_assets_editAnimation"),
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (option == JOptionPane.OK_OPTION) {
        panel.applyChanges();
        UI.getAssetController().refresh();
      }
    } finally {
      panel.dispose();
    }
  }

  public void exportAsset() {
    if (origin instanceof Tileset tileset) {
      exportTileset(tileset);
    } else if (origin instanceof SpritesheetResource spritesheetResource) {
      exportSpritesheet(spritesheetResource);
    } else if (origin instanceof EmitterAttributes emitterData) {
      exportEmitter(emitterData);
    } else if (origin instanceof Blueprint blueprint) {
      exportBlueprint(blueprint);
    } else if (origin instanceof SoundResource soundResource) {
      exportSound(soundResource);
    } else if (origin instanceof Animation animation) {
      exportAnimation(animation);
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

  private void exportEmitter(EmitterAttributes emitter) {
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

  private void exportAnimation(Animation animation) {
    JFileChooser chooser = createFileChooser("Aseprite JSON", "json", animation.getName() + ".json");
    chooser.setDialogTitle("Export Aseprite Animation");

    if (chooser.showSaveDialog(Game.window().getRenderComponent()) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    java.nio.file.Path destination = chooser.getSelectedFile().toPath();
    if (Resources.animations().exportAseprite(animation, destination)) {
      log.log(Level.INFO, "exported animation {0} to {1}", new Object[] {animation.getName(), destination});
    } else {
      log.log(Level.WARNING, "failed to export animation {0}", animation.getName());
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

  public boolean canAdd() {
    if (origin instanceof SpritesheetResource spritesheetResource) {
      return PropPanel.getIdentifierBySpriteName(spritesheetResource.getName()) != null
        || CreaturePanel.getCreatureSpriteName(spritesheetResource.getName()) != null;
    }
    return origin instanceof MapObject || origin instanceof EmitterAttributes;
  }

  private static int getDeleteDialog(String assetType, String assetName) {
    return JOptionPane.showConfirmDialog(Game.window().getRenderComponent(),
      Resources.strings().get(String.format("assetpanel_confirmdelete_%s", assetType), assetName),
      Resources.strings().get(String.format("assetpanel_confirmdelete_%s_title", assetType)), JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE);
  }

  private void updateButtonVisibility(boolean visible) {
    if (origin instanceof SpritesheetResource || origin instanceof EmitterAttributes || origin instanceof MapObject) {
      btnAdd.setVisible(visible);
      btnDelete.setVisible(visible);
    } else if (origin instanceof Tileset || origin instanceof SoundResource) {
      btnAdd.setVisible(false);
      btnDelete.setVisible(origin instanceof SoundResource && visible);
    } else if (origin instanceof Animation) {
      btnAdd.setVisible(false);
      btnDelete.setVisible(visible);
    }
    btnEdit.setVisible(visible);
    btnExport.setVisible(visible);
  }
}
