package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
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
import de.gurkenlabs.utiliti.controller.tool.AssetFileExporter;
import de.gurkenlabs.utiliti.controller.tool.AssetTransferable;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.dialogs.XmlExportDialog;
import de.gurkenlabs.utiliti.view.menus.AssetPanelItemPopupMenu;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
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
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AssetPanelItem extends JPanel {
  private static final Logger log = Logger.getLogger(AssetPanelItem.class.getName());
  private static final int PADDING = Style.SPACE_SMALL;
  private static final int BUTTON_SIZE = Style.TOOLBAR_BUTTON_SIZE;
  private static final int COMPACT_HEIGHT = Style.CONTROL_HEIGHT + Style.SPACE_LARGE;
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
  private boolean isFocused;
  private boolean compact;
  private Icon rawIcon;
  private int cardSize = PREFERRED_SIZE.width;
  private Consumer<AssetPanelItem> focusCallback;
  private Consumer<MouseEvent> selectionPressedCallback;
  private Consumer<MouseEvent> selectionClickedCallback;
  private Supplier<java.util.List<Object>> transferAssetsSupplier;
  private MouseAdapter mouseHandler;
  private boolean dragStarted;
  private boolean individualActionsEnabled = true;

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
    label.setForeground(Style.text());
    return label;
  }

  private JLabel createDetailsLabel() {
    JLabel label = new JLabel();
    label.setHorizontalAlignment(SwingConstants.LEFT);
    label.setFont(label.getFont().deriveFont(Font.PLAIN, 10f));
    label.setForeground(Style.mutedText());
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
        return createAssetTransferable();
      }

      @Override public void exportToClipboard(JComponent component, Clipboard clipboard, int action) {
        if (action != COPY) {
          throw new IllegalArgumentException("Asset transfers only support copy");
        }
        Transferable data = createTransferable(component);
        if (data instanceof AssetTransferable assetTransferable) {
          assetTransferable.ownClipboard();
          try {
            clipboard.setContents(data, assetTransferable);
          } catch (IllegalStateException e) {
            assetTransferable.lostOwnership(clipboard, data);
            throw e;
          }
        }
      }

      @Override protected void exportDone(JComponent source, Transferable data, int action) {
        closeTransfer(data);
      }
    });
    btnAdd.setEnabled(canAdd());
  }

  private JButton createStyledButton(Icon icon, String tooltipKey) {
    JButton button = Style.iconButton(icon);
    buttonPanel.add(button);
    button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
    button.setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
    String tooltip = Resources.strings().get(tooltipKey);
    button.setToolTipText(tooltip);
    button.getAccessibleContext().setAccessibleName(tooltip);
    if ("assetpanel_delete".equals(tooltipKey)) {
      Style.styleButton(button, Style.ButtonVariant.DESTRUCTIVE);
    }
    button.setVisible(false);
    return button;
  }

  private void setupLayout() {
    if (compact) {
      setLayout(new BorderLayout(Style.SPACE_MEDIUM, 0));
      iconLabel.setHorizontalAlignment(SwingConstants.LEFT);
      iconLabel.setPreferredSize(new Dimension(COMPACT_HEIGHT, COMPACT_HEIGHT));
      iconLabel.setBorder(BorderFactory.createEmptyBorder(
        Style.SPACE_SMALL, Style.SPACE_SMALL, Style.SPACE_SMALL, Style.SPACE_SMALL));
      nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
      JPanel centerPanel = new JPanel(new BorderLayout());
      centerPanel.setOpaque(false);
      centerPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
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
    contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

    contentPanel.add(iconPanel, BorderLayout.NORTH);
    contentPanel.add(createTextPanel(), BorderLayout.CENTER);
    contentPanel.add(createBottomPanel(), BorderLayout.SOUTH);

    return contentPanel;
  }

  private JPanel createIconPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    int iconArea = getCardIconArea();
    panel.setPreferredSize(new Dimension(iconArea, iconArea));
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
    getInputMap(JComponent.WHEN_FOCUSED).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcutMask()), "copyAsset");
    getActionMap().put("copyAsset", TransferHandler.getCopyAction());

    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAsset");
    getActionMap().put("deleteAsset", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent ae) {
        if (individualActionsEnabled) {
          deleteAsset();
        }
      }
    });

    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "addAsset");
    getActionMap().put("addAsset", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent ae) {
        if (individualActionsEnabled && canAdd()) {
          addEntity();
        }
      }
    });

    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editAsset");
    getActionMap().put("editAsset", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent ae) {
        if (individualActionsEnabled && canEdit()) {
          editAsset();
        }
      }
    });

    getInputMap(JComponent.WHEN_FOCUSED).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_E, menuShortcutMask()), "exportAsset");
    getActionMap().put("exportAsset", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent ae) {
        if (individualActionsEnabled) {
          exportAsset();
        }
      }
    });
  }

  private void setupFocusHandling() {
    FocusAdapter focusHandler = new FocusAdapter() {
      @Override public void focusGained(FocusEvent e) {
        updateButtonVisibility(true);
        if (focusCallback != null) {
          focusCallback.accept(AssetPanelItem.this);
        }
        repaint();
      }

      @Override public void focusLost(FocusEvent e) {
        SwingUtilities.invokeLater(() -> {
          Component focusOwner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
          if (focusOwner == AssetPanelItem.this || focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, AssetPanelItem.this)) {
            return;
          }
          updateButtonVisibility(isHovered);
          repaint();
        });
      }
    };
    addFocusListener(focusHandler);
    btnAdd.addFocusListener(focusHandler);
    btnEdit.addFocusListener(focusHandler);
    btnExport.addFocusListener(focusHandler);
    btnDelete.addFocusListener(focusHandler);
  }

  private void setupMouseHandling() {
    this.mouseHandler = new MouseAdapter() {
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
        updateButtonVisibility(isSelected);
        repaint();
      }

      @Override public void mousePressed(MouseEvent e) {
        dragStarted = false;
        if (selectionPressedCallback != null) {
          selectionPressedCallback.accept(e);
        }
        requestFocus();
        maybeShowPopup(e);
      }

      @Override public void mouseDragged(MouseEvent e) {
        if (dragStarted || (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == 0) {
          return;
        }
        dragStarted = true;
        MouseEvent event = SwingUtilities.convertMouseEvent(e.getComponent(), e, AssetPanelItem.this);
        getTransferHandler().exportAsDrag(AssetPanelItem.this, event, TransferHandler.COPY);
      }

      @Override public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger() && selectionPressedCallback != null) {
          selectionPressedCallback.accept(e);
        }
        maybeShowPopup(e);
      }

      @Override public void mouseClicked(MouseEvent e) {
        if (selectionClickedCallback != null) {
          selectionClickedCallback.accept(e);
        }
        requestFocus();
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
          if (origin instanceof de.gurkenlabs.litiengine.scripting.ScriptDefinition scriptDef) {
            UI.openScript(scriptDef);
          } else {
            addEntity();
          }
        }
      }
    };
    installMouseHandler(this);
  }

  private void installMouseHandler(Component component) {
    boolean installed = java.util.Arrays.stream(component.getMouseListeners()).anyMatch(listener -> listener == this.mouseHandler);
    if (!installed) {
      component.addMouseListener(this.mouseHandler);
    }
    boolean motionInstalled = java.util.Arrays.stream(component.getMouseMotionListeners()).anyMatch(listener -> listener == this.mouseHandler);
    if (!motionInstalled) {
      component.addMouseMotionListener(this.mouseHandler);
    }
    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        installMouseHandler(child);
      }
    }
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
    updateNameLabel();
    detailsLabel.setText(getDetailsSummary());
    String tooltip = createTooltip(text);
    setToolTipText(tooltip);
    iconLabel.setToolTipText(tooltip);
    nameLabel.setToolTipText(tooltip);
    getAccessibleContext().setAccessibleName(text);
    getAccessibleContext().setAccessibleDescription(getDetailsSummary());
  }

  private void updateScaledIcon() {
    if (rawIcon == null) {
      iconLabel.setIcon(null);
      return;
    }
    int iconArea = compact ? 32 : getCardIconArea();
    if (rawIcon instanceof ImageIcon imgIcon && imgIcon.getImage() instanceof java.awt.image.BufferedImage bi) {
      java.awt.image.BufferedImage scaled = de.gurkenlabs.litiengine.util.Imaging.scale(bi, iconArea, iconArea, true);
      if (scaled != null) {
        iconLabel.setIcon(new ImageIcon(scaled));
        return;
      }
    }
    iconLabel.setIcon(rawIcon);
  }

  private int getCardIconArea() {
    return Math.max(32, cardSize - 2 * PADDING - BUTTON_SIZE - 24);
  }

  private void updateNameLabel() {
    nameLabel.setText(compact ? assetName : wrapText(assetName, Math.max(12, cardSize / 7)));
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
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    RoundRectangle2D roundRect = new RoundRectangle2D.Float(
      0, 0, getWidth() - 1f, getHeight() - 1f, Style.CORNER_RADIUS, Style.CORNER_RADIUS);

    g2d.setColor(Style.surface());
    g2d.fill(roundRect);
    if (isSelected) {
      g2d.setColor(Style.cardSelected());
      g2d.fill(roundRect);
    } else if (isHovered) {
      g2d.setColor(Style.cardHover());
      g2d.fill(roundRect);
    }

    g2d.dispose();
  }

  @Override
  protected void paintChildren(Graphics graphics) {
    super.paintChildren(graphics);
    Graphics2D g2d = (Graphics2D) graphics.create();
    try {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (this.isSelected) {
        g2d.setColor(Style.accent());
        g2d.fillRoundRect(3, 8, 3, Math.max(4, getHeight() - 16), 3, 3);
      }
      RoundRectangle2D roundRect = new RoundRectangle2D.Float(
          0, 0, getWidth() - 1f, getHeight() - 1f, Style.CORNER_RADIUS, Style.CORNER_RADIUS);
      g2d.setColor(this.isFocused ? Style.accent() : Style.border());
      g2d.setStroke(this.isFocused ? FOCUS_STROKE : BORDER_STROKE);
      g2d.draw(roundRect);
    } finally {
      g2d.dispose();
    }
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (this.nameLabel != null) {
      this.nameLabel.setForeground(Style.text());
    }
    if (this.detailsLabel != null) {
      this.detailsLabel.setForeground(Style.mutedText());
    }
  }

  public static Map<String, String> getDetails(Object origin) {
    Map<String, String> details = new java.util.LinkedHashMap<>();
    if (origin instanceof SpritesheetResource spritesheetResource) {
      details.put(Resources.strings().get("emitter_size"), Resources.strings().get(
        "assetpanel_pixel_dimensions", spritesheetResource.getWidth(), spritesheetResource.getHeight()));
    } else if (origin instanceof Animation animation) {
      details.put(Resources.strings().get("assetpanel_animation_frames"), Resources.strings().get(
        "assetpanel_metadata_count", animation.getKeyframes().size()));
      details.put(Resources.strings().get("assetpanel_animation_duration"), Resources.strings().get(
        "assetpanel_metadata_count", animation.getTotalDuration()));
      if (animation.getSpritesheet() != null) {
        details.put(Resources.strings().get("assetpanel_animation_spritesheet"), animation.getSpritesheet().getName());
      }
    } else if (origin instanceof Tileset tileset) {
      details.put(Resources.strings().get("assetpanel_metadata_tiles"), Resources.strings().get(
        "assetpanel_metadata_count", tileset.getTileCount()));
      details.put(Resources.strings().get("assetpanel_metadata_tile_size"), Resources.strings().get(
        "assetpanel_dimensions", tileset.getTileWidth(), tileset.getTileHeight()));
      if (tileset.getImage() != null) {
        details.put(Resources.strings().get("assetpanel_metadata_image"), tileset.getImage().getSource());
      }
    }
    return details;
  }

  public Object getOrigin() {
    return origin;
  }

  public boolean isSelected() {
    return this.isSelected;
  }

  public void setSelected(boolean selected) {
    if (this.isSelected == selected) {
      return;
    }
    this.isSelected = selected;
    repaint();
  }

  public boolean isFocused() {
    return this.isFocused;
  }

  public void setFocused(boolean focused) {
    if (this.isFocused == focused) {
      return;
    }
    this.isFocused = focused;
    repaint();
  }

  public boolean isIndividualActionsEnabled() {
    return this.individualActionsEnabled;
  }

  public void setIndividualActionsEnabled(boolean enabled) {
    this.individualActionsEnabled = enabled;
    this.btnAdd.setEnabled(enabled && canAdd());
    this.btnEdit.setEnabled(enabled && canEdit());
    this.btnDelete.setEnabled(enabled);
    this.btnExport.setEnabled(enabled);
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

  public void setSelectionCallbacks(
      Consumer<MouseEvent> pressedCallback, Consumer<MouseEvent> clickedCallback) {
    this.selectionPressedCallback = pressedCallback;
    this.selectionClickedCallback = clickedCallback;
  }

  public void setTransferAssetsSupplier(Supplier<java.util.List<Object>> supplier) {
    this.transferAssetsSupplier = supplier;
  }

  private Transferable createAssetTransferable() {
    java.util.List<Object> assets = transferAssetsSupplier != null
        ? transferAssetsSupplier.get()
        : java.util.List.of(origin);
    return assets.isEmpty() ? null : new AssetTransferable(assets);
  }

  Transferable createTransferableForTest() {
    return createAssetTransferable();
  }

  static void closeTransfer(Transferable data) {
    if (data instanceof AssetTransferable assetTransferable) {
      assetTransferable.close();
    }
  }

  public void setCompact(boolean compact) {
    if (this.compact == compact) {
      return;
    }
    this.compact = compact;
    removeAll();
    setupLayout();
    setPreferredSize(compact ? new Dimension(PREFERRED_SIZE.width, COMPACT_HEIGHT) : new Dimension(cardSize, cardSize));
    setMinimumSize(compact ? new Dimension(0, COMPACT_HEIGHT) : new Dimension(cardSize, cardSize));
    setMaximumSize(compact
      ? new Dimension(Integer.MAX_VALUE, COMPACT_HEIGHT)
      : new Dimension(cardSize, cardSize));
    setAlignmentX(LEFT_ALIGNMENT);
    iconLabel.setPreferredSize(compact ? new Dimension(COMPACT_HEIGHT, COMPACT_HEIGHT) : null);
    iconLabel.setHorizontalAlignment(compact ? SwingConstants.LEFT : SwingConstants.CENTER);
    nameLabel.setHorizontalAlignment(compact ? SwingConstants.LEFT : SwingConstants.CENTER);
    updateNameLabel();
    updateScaledIcon();
    if (this.mouseHandler != null) {
      installMouseHandler(this);
    }
    revalidate();
    repaint();
  }

  public void setCardSize(int cardSize) {
    this.cardSize = cardSize;
    if (!compact) {
      setPreferredSize(new Dimension(cardSize, cardSize));
      setMinimumSize(new Dimension(cardSize, cardSize));
      setMaximumSize(new Dimension(cardSize, cardSize));
      int iconArea = getCardIconArea();
      iconPanel.setPreferredSize(new Dimension(iconArea, iconArea));
      updateNameLabel();
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
    Editor.instance().getMapComponent().addMapObjectFromAsset(origin, Game.world().camera().getFocus());
  }

  public void editAsset() {
    if (origin instanceof SpritesheetResource spritesheetResource) {
      editSpritesheet(spritesheetResource);
    } else if (origin instanceof Animation animation) {
      editAnimation(animation);
    }
  }

  private void editSpritesheet(SpritesheetResource spritesheetResource) {
    UI.showSpriteInspector(spritesheetResource);
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
      JOptionPane.showOptionDialog(Game.window().getRenderComponent(),
        Resources.strings().get("assetpanel_export_format_prompt"),
        Resources.strings().get("contextmenu_resource_export_spritesheet"), JOptionPane.DEFAULT_OPTION,
        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

    if (answer == 0) {
      XmlExportDialog.export(spritesheetResource, Resources.strings().get("panel_spritesheet"),
          AssetFileExporter.safeFileName(spritesheetResource.getName()));
    } else if (answer == 1) {
      exportImage(sprite, format, AssetFileExporter.safeFileName(spritesheetResource.getName()));
    }
  }

  private void exportImage(Spritesheet sprite, ImageFormat format, String name) {
    JFileChooser chooser = createFileChooser(format.toString(), format.toFileExtension(), name + format.toFileExtension());
    chooser.setDialogTitle(Resources.strings().get("contextmenu_resource_export_spritesheet"));

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
    XmlExportDialog.export(tileset, Resources.strings().get("assetpanel_type_tileset"),
        AssetFileExporter.safeFileName(tileset.getName()), Tileset.FILE_EXTENSION);
  }

  private void exportEmitter(EmitterAttributes emitter) {
    XmlExportDialog.export(emitter, Resources.strings().get("panel_emitter"),
        AssetFileExporter.safeFileName(emitter.getName()));
  }

  private void exportBlueprint(Blueprint blueprint) {
    XmlExportDialog.export(blueprint, Resources.strings().get("assetpanel_type_blueprint"),
      AssetFileExporter.safeFileName(blueprint.getName()),
      Blueprint.BLUEPRINT_FILE_EXTENSION);
  }

  private void exportSound(SoundResource sound) {
    if (sound.getFormat() == SoundFormat.UNSUPPORTED) {
      return;
    }

    try {
      JFileChooser chooser =
        createFileChooser(sound.getFormat().toString(), sound.getFormat().toString(),
          AssetFileExporter.safeFileName(sound.getName()) + sound.getFormat().toFileExtension());
      chooser.setDialogTitle(Resources.strings().get("contextmenu_resource_export_sound"));

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
    JFileChooser chooser = createFileChooser(
      Resources.strings().get("assetpanel_aseprite_json"), "json",
      AssetFileExporter.safeFileName(animation.getName()) + ".json");
    chooser.setDialogTitle(Resources.strings().get("contextmenu_resource_export_animation"));

    if (chooser.showSaveDialog(Game.window().getRenderComponent()) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    java.nio.file.Path destination = chooser.getSelectedFile().toPath();
    try {
      if (AssetFileExporter.exportAnimation(animation, destination).size() == 2) {
        log.log(Level.INFO, "exported animation {0} to {1}",
          new Object[] {animation.getName(), destination});
      } else {
        log.log(Level.WARNING, "failed to export animation {0}", animation.getName());
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private JFileChooser createFileChooser(String description, String extension, String defaultFileName) {
    JFileChooser chooser = new JFileChooser(Editor.instance().getProjectPath().toFile());
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);

    FileFilter filter = new FileNameExtensionFilter(
      Resources.strings().get("assetpanel_file_filter", description), extension);
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

  public boolean canEdit() {
    return origin instanceof Animation;
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
    btnEdit.setVisible(visible && canEdit());
    btnExport.setVisible(visible);
  }

  private static int menuShortcutMask() {
    try {
      return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    } catch (HeadlessException ignored) {
      return InputEvent.CTRL_DOWN_MASK;
    }
  }
}
