package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.litiengine.util.io.Codec;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.SpinnerCellEditor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class SpriteEditorPanel extends JPanel {
  private static final int PREVIEW_HEIGHT = 142;
  private static final int TABLE_ROW_HEIGHT = 38;
  private static final float[] PREVIEW_ZOOM_LEVELS = {1f, 2f, 4f, 8f};

  private final JLabel titleLabel;
  private final JLabel metadataLabel;
  private final JLabel thumbnailLabel;
  private final JLabel frameIndicator;
  private final JLabel frameSizeLabel;
  private final JLabel frameCountLabel;
  private final JLabel imageSizeLabel;
  private final JLabel validationLabel;
  private final JLabel durationSummaryLabel;
  private final JLabel totalDurationLabel;
  private final JTextField nameField;
  private final JPanel namePanel;
  private final PreviewCanvas animationPreview;
  private final SpriteGridCanvas spriteGrid;
  private final JSpinner columnsSpinner;
  private final JSpinner rowsSpinner;
  private final JSpinner defaultDurationSpinner;
  private final JComboBox<String> durationScopeCombo;
  private final ZoomControls zoomControls;
  private final JToggleButton playButton;
  private final JToggleButton loopButton;
  private final DefaultTableModel keyframeModel;
  private final JTable keyframeTable;
  private final Timer animationPreviewTimer;
  private final JComboBox<VariantItem> variantCombo;

  private SpritesheetResource spritesheetResource;
  private BufferedImage image;
  private boolean binding;
  private int currentFrame;
  private long frameStartedAt;
  private float previewZoom = 1f;
  private boolean previewFit = true;

  public SpriteEditorPanel() {
    super(new BorderLayout());
    setOpaque(true);
    setBackground(Style.background());

    this.variantCombo = new JComboBox<>();
    this.variantCombo.setFont(this.variantCombo.getFont().deriveFont(11f));
    this.variantCombo.setFocusable(false);
    this.variantCombo.addActionListener(e -> {
      if (this.binding || this.spritesheetResource == null) {
        return;
      }
      Object selected = this.variantCombo.getSelectedItem();
      if (selected instanceof VariantItem item) {
        if (!item.name().equalsIgnoreCase(this.spritesheetResource.getName())) {
          SpritesheetResource target = findSpriteResource(item.name());
          if (target != null) {
            bind(target);
          } else if (item.isMirrored() && item.sourceName() != null) {
            SpritesheetResource source = findSpriteResource(item.sourceName());
            if (source != null) {
              SpritesheetResource virtual = createMirroredResource(source, item.name());
              bind(virtual);
            }
          }
        }
      }
    });

    this.titleLabel = new JLabel(Resources.strings().get("spriteEditor_noSpriteSelected"));
    this.titleLabel.setForeground(Style.text());
    this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(Font.BOLD, 15f));
    this.metadataLabel = mutedLabel("-");
    this.thumbnailLabel = new JLabel();
    this.thumbnailLabel.setHorizontalAlignment(SwingConstants.CENTER);
    this.thumbnailLabel.setPreferredSize(new Dimension(48, 48));
    this.thumbnailLabel.setOpaque(true);
    this.thumbnailLabel.setBackground(Style.raisedSurface());
    this.thumbnailLabel.setBorder(BorderFactory.createLineBorder(Style.border()));

    this.nameField = ControlBehavior.apply(new JTextField());
    this.nameField.addActionListener(_ -> finishRename());
    this.nameField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent event) {
        finishRename();
      }
    });
    this.namePanel = new JPanel(new CardLayout());
    this.namePanel.setOpaque(false);
    this.namePanel.add(this.titleLabel, "label");
    this.namePanel.add(this.nameField, "editor");

    this.animationPreview = new PreviewCanvas();
    this.animationPreview.setPreferredSize(new Dimension(0, PREVIEW_HEIGHT));
    this.frameIndicator = mutedLabel("Frame 0 / 0");
    this.frameSizeLabel = mutedLabel("-");
    this.frameCountLabel = mutedLabel("-");
    this.imageSizeLabel = mutedLabel("-");
    this.validationLabel = new JLabel();
    this.validationLabel.setForeground(Style.COLOR_ORANGE);
    this.validationLabel.setVisible(false);
    this.durationSummaryLabel = mutedLabel("-");
    this.totalDurationLabel = new JLabel();
    this.totalDurationLabel.setForeground(Style.accent());

    this.spriteGrid = new SpriteGridCanvas();
    this.spriteGrid.setPreferredSize(new Dimension(0, 138));

    this.columnsSpinner = createGridSpinner();
    this.rowsSpinner = createGridSpinner();
    ChangeListener gridChanged = _ -> applyGrid();
    this.columnsSpinner.addChangeListener(gridChanged);
    this.rowsSpinner.addChangeListener(gridChanged);

    this.defaultDurationSpinner = new JSpinner(
        new SpinnerNumberModel(Animation.DEFAULT_FRAME_DURATION, 1, 60_000, 10));
    ControlBehavior.apply(this.defaultDurationSpinner);
    this.defaultDurationSpinner.setPreferredSize(new Dimension(86, Style.CONTROL_HEIGHT));
    this.defaultDurationSpinner.addChangeListener(_ -> updateDurationSummary());
    this.durationScopeCombo = new JComboBox<>(new String[] {
        Resources.strings().get("spriteEditor_selectedFrames"),
        Resources.strings().get("spriteEditor_allFrames")});
    this.durationScopeCombo.setPreferredSize(new Dimension(126, Style.CONTROL_HEIGHT));

    this.keyframeModel = createKeyframeModel();
    this.keyframeTable = createKeyframeTable();
    this.keyframeModel.addTableModelListener(_ -> applyKeyframes());
    this.keyframeTable.getSelectionModel().addListSelectionListener(_ -> {
      if (!this.binding && this.keyframeTable.getSelectedRow() >= 0) {
        selectFrame(this.keyframeTable.getSelectedRow(), false);
      }
      updateDurationSummary();
    });

    this.zoomControls = new ZoomControls(
        () -> stepPreviewZoom(-1),
        () -> stepPreviewZoom(1),
        this::fitPreview,
        Resources.strings().get("toolbar_fit"));

    this.playButton = Style.iconToggleButton(Icons.PAUSE_16, true);
    configureButton(this.playButton, "spriteEditor_playPause");
    this.playButton.addActionListener(_ -> {
      this.playButton.setIcon(this.playButton.isSelected() ? Icons.PAUSE_16 : Icons.PLAY_16);
      this.frameStartedAt = System.currentTimeMillis();
    });
    this.loopButton = Style.iconToggleButton(Icons.LOOP_16, true);
    configureButton(this.loopButton, "spriteEditor_loop");

    JPanel cards = new ViewportWidthPanel();
    cards.setOpaque(false);
    cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
    cards.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    cards.add(createPreviewCard());
    cards.add(Box.createVerticalStrut(10));
    cards.add(createSpriteSheetCard());
    cards.add(Box.createVerticalStrut(10));
    cards.add(createAnimationCard());
    cards.add(Box.createVerticalStrut(10));
    cards.add(createAdvancedCard());
    cards.add(Box.createVerticalGlue());

    JScrollPane scroll = new JScrollPane(
        cards,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(24);
    scroll.getViewport().setBackground(Style.background());
    JPanel header = createHeader();
    header.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    add(header, BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);

    this.animationPreviewTimer = new Timer(32, _ -> updateAnimationPreview());
    this.animationPreviewTimer.start();
  }

  public void bind(SpritesheetResource spritesheetResource) {
    this.spritesheetResource = spritesheetResource;
    this.image = spritesheetResource != null ? Codec.decodeImage(spritesheetResource.getImage()) : null;
    this.currentFrame = 0;
    this.frameStartedAt = System.currentTimeMillis();
    refreshControls();
  }

  @Override
  public void addNotify() {
    super.addNotify();
    if (this.animationPreviewTimer != null) {
      this.animationPreviewTimer.start();
    }
  }

  @Override
  public void removeNotify() {
    if (this.animationPreviewTimer != null) {
      this.animationPreviewTimer.stop();
    }
    super.removeNotify();
  }

  private JPanel createHeader() {
    JPanel group = new JPanel(new BorderLayout(10, 0));
    group.setOpaque(false);
    group.add(this.thumbnailLabel, BorderLayout.WEST);

    JPanel text = new JPanel();
    text.setOpaque(false);
    text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
    JPanel titleRow = new JPanel(new BorderLayout(4, 0));
    titleRow.setOpaque(false);
    titleRow.add(this.namePanel, BorderLayout.CENTER);
    JButton rename = Style.iconButton(Icons.PENCIL_16);
    configureButton(rename, "spriteEditor_rename");
    rename.addActionListener(_ -> beginRename());

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
    actions.setOpaque(false);
    actions.add(this.variantCombo);
    actions.add(rename);
    titleRow.add(actions, BorderLayout.EAST);
    text.add(titleRow);
    text.add(Box.createVerticalStrut(2));
    this.metadataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    text.add(this.metadataLabel);
    group.add(text, BorderLayout.CENTER);
    group.setMaximumSize(group.getPreferredSize());

    JPanel header = new JPanel();
    header.setOpaque(false);
    header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
    header.setAlignmentX(Component.LEFT_ALIGNMENT);
    header.add(group);
    header.add(Box.createHorizontalGlue());
    header.setMaximumSize(new Dimension(Integer.MAX_VALUE, group.getPreferredSize().height));
    return header;
  }

  private ExpandableCard createPreviewCard() {
    JPanel content = new JPanel(new BorderLayout(0, 6));
    content.setOpaque(false);
    content.add(this.animationPreview, BorderLayout.CENTER);

    JPanel toolbar = new JPanel(new BorderLayout());
    toolbar.setOpaque(false);
    JPanel playback = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    playback.setOpaque(false);
    JButton previous = Style.iconButton(Icons.STEP_BACK_16);
    configureButton(previous, "spriteEditor_previousFrame");
    previous.addActionListener(_ -> stepFrame(-1));
    JButton next = Style.iconButton(Icons.STEP_FORWARD_16);
    configureButton(next, "spriteEditor_nextFrame");
    next.addActionListener(_ -> stepFrame(1));
    playback.add(previous);
    playback.add(this.playButton);
    playback.add(next);
    playback.add(this.loopButton);
    toolbar.add(playback, BorderLayout.WEST);

    JPanel view = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
    view.setOpaque(false);
    JCheckBox checkerboard = new JCheckBox(Resources.strings().get("spriteEditor_checkerboard"), true);
    checkerboard.addActionListener(_ -> {
      this.animationPreview.setCheckerboard(checkerboard.isSelected());
      this.spriteGrid.setCheckerboard(checkerboard.isSelected());
    });
    view.add(checkerboard);
    view.add(this.zoomControls);
    toolbar.add(view, BorderLayout.EAST);
    content.add(toolbar, BorderLayout.SOUTH);

    ExpandableCard card = new ExpandableCard(Resources.strings().get("spriteEditor_preview"), content);
    card.setHeaderTrailing(this.frameIndicator);
    return card;
  }

  private ExpandableCard createSpriteSheetCard() {
    JPanel content = new JPanel();
    content.setOpaque(false);
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    this.spriteGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
    content.add(this.spriteGrid);
    content.add(Box.createVerticalStrut(8));

    JPanel fields = new JPanel(new GridLayout(1, 4, 8, 0));
    fields.setOpaque(false);
    fields.add(field(Resources.strings().get("spriteEditor_columns"), this.columnsSpinner));
    fields.add(field(Resources.strings().get("spriteEditor_rows"), this.rowsSpinner));
    fields.add(field(Resources.strings().get("spriteEditor_frameSize"), this.frameSizeLabel));
    fields.add(field(Resources.strings().get("spriteEditor_totalFrames"), this.frameCountLabel));
    fields.setMaximumSize(new Dimension(Integer.MAX_VALUE, fields.getPreferredSize().height));
    fields.setAlignmentX(Component.LEFT_ALIGNMENT);
    content.add(fields);
    content.add(Box.createVerticalStrut(4));
    this.validationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    content.add(this.validationLabel);
    return new ExpandableCard(Resources.strings().get("spriteEditor_spriteSheet"), content);
  }

  private ExpandableCard createAnimationCard() {
    JPanel content = new JPanel();
    content.setOpaque(false);
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    toolbar.setOpaque(false);
    JButton duplicate = actionButton(Icons.COPY_16, "spriteEditor_duplicateTiming", this::duplicateTiming);
    JButton delete = actionButton(Icons.DELETE_16, "spriteEditor_resetTiming", this::resetTiming);
    Style.styleButton(delete, Style.ButtonVariant.DESTRUCTIVE);
    JButton moveUp = actionButton(Icons.LIFT_16, "spriteEditor_moveEarlier", () -> moveTiming(-1));
    JButton moveDown = actionButton(Icons.LOWER_16, "spriteEditor_moveLater", () -> moveTiming(1));
    toolbar.add(duplicate);
    toolbar.add(delete);
    toolbar.add(Box.createHorizontalStrut(8));
    toolbar.add(moveUp);
    toolbar.add(moveDown);
    toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
    content.add(toolbar);
    content.add(Box.createVerticalStrut(6));

    JScrollPane tableScroll = new JScrollPane(
        this.keyframeTable,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    tableScroll.setBorder(BorderFactory.createLineBorder(Style.border()));
    tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
    content.add(tableScroll);
    content.add(Box.createVerticalStrut(8));

    JPanel durationTools = new JPanel(new BorderLayout(8, 0));
    durationTools.setOpaque(false);
    JPanel apply = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    apply.setOpaque(false);
    apply.add(new JLabel(Resources.strings().get("spriteEditor_setDurationFor")));
    apply.add(this.durationScopeCombo);
    apply.add(new JLabel(Resources.strings().get("spriteEditor_durationTo")));
    apply.add(this.defaultDurationSpinner);
    apply.add(new JLabel("ms"));
    JButton applyButton = new JButton(Resources.strings().get("assetpanel_animation_apply"));
    applyButton.setPreferredSize(new Dimension(
        Math.max(64, applyButton.getFontMetrics(applyButton.getFont()).stringWidth(applyButton.getText()) + 24),
        Style.CONTROL_HEIGHT));
    applyButton.setMinimumSize(applyButton.getPreferredSize());
    applyButton.setMaximumSize(applyButton.getPreferredSize());
    applyButton.addActionListener(_ -> applyDurationToSelection());
    apply.add(applyButton);
    durationTools.add(apply, BorderLayout.WEST);
    durationTools.add(this.durationSummaryLabel, BorderLayout.EAST);
    durationTools.setMaximumSize(new Dimension(Integer.MAX_VALUE, durationTools.getPreferredSize().height));
    durationTools.setAlignmentX(Component.LEFT_ALIGNMENT);
    content.add(durationTools);

    ExpandableCard card = new ExpandableCard(Resources.strings().get("spriteEditor_animation"), content);
    card.setHeaderTrailing(this.totalDurationLabel);
    return card;
  }

  private ExpandableCard createAdvancedCard() {
    JPanel content = new JPanel(new GridLayout(3, 1, 0, 6));
    content.setOpaque(false);
    content.add(field(Resources.strings().get("spriteEditor_imageSize"), this.imageSizeLabel));
    content.add(field(Resources.strings().get("spriteEditor_format"), mutedLabel("PNG")));
    content.add(field(Resources.strings().get("spriteEditor_resourceType"), mutedLabel("Sprite")));
    return new ExpandableCard(Resources.strings().get("spriteEditor_advanced"), content, false);
  }

  private void refreshControls() {
    this.binding = true;
    try {
      this.keyframeModel.setRowCount(0);
      if (this.spritesheetResource == null || this.image == null) {
        this.titleLabel.setText(Resources.strings().get("spriteEditor_noSpriteSelected"));
        this.metadataLabel.setText("-");
        this.imageSizeLabel.setText("-");
        this.thumbnailLabel.setIcon(null);
        this.animationPreview.repaint();
        this.spriteGrid.repaint();
        return;
      }

      this.titleLabel.setText(this.spritesheetResource.getName());
      this.nameField.setText(this.spritesheetResource.getName());
      int columns = getColumns();
      int rows = getRows();
      int frameCount = columns * rows;
      this.currentFrame = Math.min(this.currentFrame, frameCount - 1);
      this.columnsSpinner.setModel(new SpinnerNumberModel(columns, 1, this.image.getWidth(), 1));
      this.rowsSpinner.setModel(new SpinnerNumberModel(rows, 1, this.image.getHeight(), 1));
      this.frameSizeLabel.setText(dimensions(
          this.spritesheetResource.getWidth(), this.spritesheetResource.getHeight()) + " px");
      this.frameCountLabel.setText(String.valueOf(frameCount));
      this.imageSizeLabel.setText(dimensions(this.image.getWidth(), this.image.getHeight()) + " px");
      boolean virtual = isVirtualMirrored(this.spritesheetResource.getName());
      String opposite = virtual ? CreaturePanel.oppositeHorizontalDirection(this.spritesheetResource.getName()) : null;
      String dimensionsText = dimensions(this.image.getWidth(), this.image.getHeight());
      String frameCountText = String.valueOf(frameCount);
      if (virtual && opposite != null) {
        this.metadataLabel.setText(Resources.strings().get("spriteEditor_mirroredFrom", opposite)
            + "  ·  " + dimensionsText + " px  ·  " + frameCountText + " frames");
      } else {
        this.metadataLabel.setText(Resources.strings().get(
            "spriteEditor_metadata",
            dimensionsText,
            frameCountText));
      }

      BufferedImage thumbnail = scaleNearest(frameImage(0), 42, 42, true);
      this.thumbnailLabel.setIcon(thumbnail != null ? new ImageIcon(thumbnail) : null);
      int[] durations = resizedDurations(this.spritesheetResource.getKeyframes(), frameCount);
      for (int i = 0; i < frameCount; i++) {
        this.keyframeModel.addRow(new Object[] {i + 1, i, durations[i]});
      }
      this.keyframeTable.setRowSelectionInterval(this.currentFrame, this.currentFrame);
      updateTableHeight();
      updateValidation(columns, rows);
      updateFrameState();
      updateTotalDuration();
      updateDurationSummary();
      updateVariantCombo();
    } finally {
      this.binding = false;
      updatePreviewZoom();
    }
  }

  private void updateVariantCombo() {
    if (this.spritesheetResource == null || Editor.instance().getGameFile() == null) {
      this.variantCombo.setVisible(false);
      return;
    }

    String currentName = this.spritesheetResource.getName();
    java.util.List<VariantItem> variants = getFamilyVariants(currentName);
    if (variants.size() <= 1) {
      this.variantCombo.setVisible(false);
      return;
    }

    javax.swing.DefaultComboBoxModel<VariantItem> model = new javax.swing.DefaultComboBoxModel<>();
    VariantItem selected = null;
    for (VariantItem variant : variants) {
      model.addElement(variant);
      if (variant.name().equalsIgnoreCase(currentName)) {
        selected = variant;
      }
    }
    this.variantCombo.setModel(model);
    if (selected != null) {
      this.variantCombo.setSelectedItem(selected);
    }
    this.variantCombo.setVisible(true);
  }

  public static java.util.List<VariantItem> getFamilyVariants(String currentName) {
    java.util.List<VariantItem> variants = new java.util.ArrayList<>();
    if (currentName == null || Editor.instance().getGameFile() == null) {
      return variants;
    }

    java.util.Set<String> explicitNames = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    for (SpritesheetResource resource : Editor.instance().getGameFile().getSpriteSheets()) {
      if (resource != null && resource.getName() != null) {
        explicitNames.add(resource.getName());
      }
    }

    String creatureBase = CreaturePanel.getCreatureSpriteName(currentName);
    if (creatureBase != null) {
      java.util.Set<String> familyNames = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      for (String name : explicitNames) {
        if (creatureBase.equalsIgnoreCase(CreaturePanel.getCreatureSpriteName(name))) {
          familyNames.add(name);
        }
      }
      if (creatureBase.equalsIgnoreCase(CreaturePanel.getCreatureSpriteName(currentName))) {
        familyNames.add(currentName);
      }

      java.util.Set<String> mirroredNames = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      for (String name : familyNames) {
        String opposite = CreaturePanel.oppositeHorizontalDirection(name);
        if (opposite != null && !explicitNames.contains(opposite) && !familyNames.contains(opposite)) {
          mirroredNames.add(opposite);
        }
      }

      for (String name : familyNames) {
        boolean virtual = !explicitNames.contains(name);
        String source = virtual ? CreaturePanel.oppositeHorizontalDirection(name) : null;
        variants.add(new VariantItem(name, virtual, source));
      }
      for (String name : mirroredNames) {
        String source = CreaturePanel.oppositeHorizontalDirection(name);
        variants.add(new VariantItem(name, true, source));
      }

      variants.sort(java.util.Comparator.comparing(VariantItem::name, String.CASE_INSENSITIVE_ORDER));
      return variants;
    }

    String propId = PropPanel.getIdentifierBySpriteName(currentName);
    if (propId != null) {
      for (String name : explicitNames) {
        if (propId.equalsIgnoreCase(PropPanel.getIdentifierBySpriteName(name))) {
          variants.add(new VariantItem(name, false, null));
        }
      }
      if (!explicitNames.contains(currentName)) {
        variants.add(new VariantItem(currentName, false, null));
      }
      variants.sort(java.util.Comparator.comparing(VariantItem::name, String.CASE_INSENSITIVE_ORDER));
      return variants;
    }

    int lastDash = currentName.lastIndexOf('-');
    if (lastDash > 0) {
      String prefix = currentName.substring(0, lastDash);
      for (String name : explicitNames) {
        if (name.toLowerCase(java.util.Locale.ROOT).startsWith(prefix.toLowerCase(java.util.Locale.ROOT) + "-")) {
          variants.add(new VariantItem(name, false, null));
        }
      }
      if (!explicitNames.contains(currentName)) {
        variants.add(new VariantItem(currentName, false, null));
      }
      variants.sort(java.util.Comparator.comparing(VariantItem::name, String.CASE_INSENSITIVE_ORDER));
    }

    return variants;
  }

  public static boolean isVirtualMirrored(String name) {
    if (name == null || Editor.instance().getGameFile() == null) {
      return false;
    }
    return findSpriteResource(name) == null
        && CreaturePanel.oppositeHorizontalDirection(name) != null
        && findSpriteResource(CreaturePanel.oppositeHorizontalDirection(name)) != null;
  }

  public static SpritesheetResource createMirroredResource(SpritesheetResource source, String mirroredName) {
    if (source == null) {
      return null;
    }
    BufferedImage sourceImage = Codec.decodeImage(source.getImage());
    if (sourceImage == null) {
      return null;
    }
    int spriteWidth = Math.max(1, source.getWidth());
    int spriteHeight = Math.max(1, source.getHeight());
    int columns = Math.max(1, sourceImage.getWidth() / spriteWidth);
    int rows = Math.max(1, sourceImage.getHeight() / spriteHeight);
    BufferedImage flipped = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = flipped.createGraphics();
    for (int col = 0; col < columns; col++) {
      for (int row = 0; row < rows; row++) {
        int x = col * spriteWidth;
        int y = row * spriteHeight;
        BufferedImage sub = sourceImage.getSubimage(x, y, spriteWidth, spriteHeight);
        g.drawImage(sub, x + spriteWidth, y, -spriteWidth, spriteHeight, null);
      }
    }
    g.dispose();
    SpritesheetResource mirrored = new SpritesheetResource(flipped, mirroredName, source.getWidth(), source.getHeight());
    mirrored.setImageFormat(source.getImageFormat() != null ? source.getImageFormat() : de.gurkenlabs.litiengine.resources.ImageFormat.PNG);
    mirrored.setKeyframes(source.getKeyframes());
    return mirrored;
  }

  public static SpritesheetResource findSpriteResource(String name) {
    if (name == null || Editor.instance().getGameFile() == null) {
      return null;
    }
    for (SpritesheetResource resource : Editor.instance().getGameFile().getSpriteSheets()) {
      if (resource != null && name.equalsIgnoreCase(resource.getName())) {
        return resource;
      }
    }
    return null;
  }

  public record VariantItem(String name, boolean isMirrored, String sourceName) {
    @Override
    public String toString() {
      if (isMirrored) {
        return name + " (" + Resources.strings().get("spriteEditor_mirrored") + ")";
      }
      return name;
    }
  }

  private DefaultTableModel createKeyframeModel() {
    return new DefaultTableModel(new Object[][] {}, new String[] {
        "#",
        Resources.strings().get("spriteEditor_preview"),
        Resources.strings().get("assetpanel_animation_duration")}) {
      @Override
      public Class<?> getColumnClass(int columnIndex) {
        return Integer.class;
      }

      @Override
      public boolean isCellEditable(int row, int column) {
        return column == 2;
      }
    };
  }

  private JTable createKeyframeTable() {
    JTable table = new JTable(this.keyframeModel) {
      @Override
      protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setColor(Style.accent());
        g.setStroke(new BasicStroke(1.5f));
        for (int row : getSelectedRows()) {
          java.awt.Rectangle bounds = getCellRect(row, 0, true);
          bounds.width = getWidth() - 1;
          g.drawRect(0, bounds.y, bounds.width, bounds.height - 1);
        }
        g.dispose();
      }
    };
    table.setFillsViewportHeight(false);
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setSelectionBackground(blend(Style.raisedSurface(), Style.accent(), 0.32f));
    table.setSelectionForeground(Style.text());
    table.getTableHeader().setReorderingAllowed(false);
    table.setRowHeight(TABLE_ROW_HEIGHT);
    table.getColumnModel().getColumn(0).setMaxWidth(54);
    table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer());
    table.getColumnModel().getColumn(1).setCellRenderer(new FrameRenderer());
    table.getColumnModel().getColumn(2).setCellRenderer(new DurationRenderer());
    table.getColumnModel().getColumn(2).setCellEditor(new SpinnerCellEditor());
    return table;
  }

  private void updateAnimationPreview() {
    if (!this.isShowing() || this.image == null || !this.playButton.isSelected()) {
      return;
    }
    int duration = durationAt(this.currentFrame);
    long now = System.currentTimeMillis();
    if (now - this.frameStartedAt < duration) {
      return;
    }
    if (this.currentFrame >= frameCount() - 1 && !this.loopButton.isSelected()) {
      this.playButton.setSelected(false);
      this.playButton.setIcon(Icons.PLAY_16);
      return;
    }
    this.currentFrame = (this.currentFrame + 1) % frameCount();
    this.frameStartedAt = now;
    updateFrameState();
  }

  private void stepFrame(int direction) {
    if (frameCount() == 0) {
      return;
    }
    this.playButton.setSelected(false);
    this.playButton.setIcon(Icons.PLAY_16);
    this.currentFrame = Math.floorMod(this.currentFrame + direction, frameCount());
    this.frameStartedAt = System.currentTimeMillis();
    selectFrame(this.currentFrame, true);
  }

  private void selectFrame(int frame, boolean selectTableRow) {
    if (frame < 0 || frame >= frameCount()) {
      return;
    }
    this.currentFrame = frame;
    this.frameStartedAt = System.currentTimeMillis();
    if (selectTableRow && frame < this.keyframeTable.getRowCount()) {
      this.keyframeTable.setRowSelectionInterval(frame, frame);
      this.keyframeTable.scrollRectToVisible(this.keyframeTable.getCellRect(frame, 0, true));
    }
    updateFrameState();
  }

  private void updateFrameState() {
    int count = frameCount();
    this.frameIndicator.setText(Resources.strings().get(
        "spriteEditor_frameIndicator",
        String.valueOf(count == 0 ? 0 : this.currentFrame + 1),
        String.valueOf(count)));
    updatePreviewZoom();
    this.spriteGrid.repaint();
    this.keyframeTable.repaint();
  }

  private void stepPreviewZoom(int direction) {
    float current = currentPreviewZoom();
    float target = current;
    if (direction > 0) {
      for (float level : PREVIEW_ZOOM_LEVELS) {
        if (level > current + 0.001f) {
          target = level;
          break;
        }
      }
    } else {
      for (int index = PREVIEW_ZOOM_LEVELS.length - 1; index >= 0; index--) {
        if (PREVIEW_ZOOM_LEVELS[index] < current - 0.001f) {
          target = PREVIEW_ZOOM_LEVELS[index];
          break;
        }
      }
    }
    this.previewFit = false;
    this.previewZoom = target;
    updatePreviewZoom();
  }

  private void fitPreview() {
    this.previewFit = true;
    updatePreviewZoom();
  }

  private float currentPreviewZoom() {
    BufferedImage frame = frameImage(this.currentFrame);
    if (!this.previewFit || frame == null) {
      return this.previewZoom;
    }
    float availableWidth = Math.max(1, this.animationPreview.getWidth() - 24);
    float availableHeight = Math.max(1, this.animationPreview.getHeight() - 24);
    return Math.max(0.01f, Math.min(
        availableWidth / frame.getWidth(), availableHeight / frame.getHeight()));
  }

  private void updatePreviewZoom() {
    this.zoomControls.setZoom(currentPreviewZoom());
    this.animationPreview.repaint();
  }

  private void applyDurationToSelection() {
    int duration = (int) this.defaultDurationSpinner.getValue();
    int[] durations = resizedDurations(this.spritesheetResource.getKeyframes(), frameCount());
    if (this.durationScopeCombo.getSelectedIndex() == 1) {
      Arrays.fill(durations, duration);
    } else {
      int[] selected = this.keyframeTable.getSelectedRows();
      if (selected.length == 0 && this.currentFrame < durations.length) {
        selected = new int[] {this.currentFrame};
      }
      for (int row : selected) {
        durations[row] = duration;
      }
    }
    changeSprite(resource -> resource.setKeyframes(durations));
  }

  private void duplicateTiming() {
    int row = this.keyframeTable.getSelectedRow();
    if (row < 0 || row + 1 >= this.keyframeModel.getRowCount()) {
      return;
    }
    this.keyframeModel.setValueAt(this.keyframeModel.getValueAt(row, 2), row + 1, 2);
    this.keyframeTable.setRowSelectionInterval(row + 1, row + 1);
  }

  private void resetTiming() {
    int[] selected = this.keyframeTable.getSelectedRows();
    for (int row : selected) {
      this.keyframeModel.setValueAt(Animation.DEFAULT_FRAME_DURATION, row, 2);
    }
  }

  private void moveTiming(int direction) {
    int row = this.keyframeTable.getSelectedRow();
    int target = row + direction;
    if (row < 0 || target < 0 || target >= this.keyframeModel.getRowCount()) {
      return;
    }
    Object duration = this.keyframeModel.getValueAt(row, 2);
    this.keyframeModel.setValueAt(this.keyframeModel.getValueAt(target, 2), row, 2);
    this.keyframeModel.setValueAt(duration, target, 2);
    this.keyframeTable.setRowSelectionInterval(target, target);
  }

  private void updateDurationSummary() {
    if (this.durationSummaryLabel == null || this.defaultDurationSpinner == null) {
      return;
    }
    int duration = (int) this.defaultDurationSpinner.getValue();
    this.durationSummaryLabel.setText(Resources.strings().get(
        "spriteEditor_fpsEquivalent", String.format("%.2f", 1000.0 / duration)));
  }

  private void updateTotalDuration() {
    int total = 0;
    for (int row = 0; row < this.keyframeModel.getRowCount(); row++) {
      Object value = this.keyframeModel.getValueAt(row, 2);
      total += value instanceof Number number ? Math.max(1, number.intValue()) : 0;
    }
    this.totalDurationLabel.setText(Resources.strings().get(
        "spriteEditor_totalDuration", String.format("%.2f", total / 1000.0)));
  }

  private void updateTableHeight() {
    int height = this.keyframeTable.getTableHeader().getPreferredSize().height
        + this.keyframeTable.getRowCount() * TABLE_ROW_HEIGHT + 2;
    JComponent parent = (JComponent) this.keyframeTable.getParent().getParent();
    parent.setPreferredSize(new Dimension(0, height));
    parent.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
  }

  private void updateValidation(int columns, int rows) {
    boolean invalid = this.image.getWidth() % columns != 0 || this.image.getHeight() % rows != 0;
    this.validationLabel.setText(Resources.strings().get("spriteEditor_gridWarning"));
    this.validationLabel.setVisible(invalid);
  }

  private void beginRename() {
    if (this.spritesheetResource == null) {
      return;
    }
    ((CardLayout) this.namePanel.getLayout()).show(this.namePanel, "editor");
    this.nameField.requestFocusInWindow();
    this.nameField.selectAll();
  }

  private void finishRename() {
    applyName();
    ((CardLayout) this.namePanel.getLayout()).show(this.namePanel, "label");
  }

  private void applyName() {
    if (this.binding || this.spritesheetResource == null) {
      return;
    }
    String name = this.nameField.getText().trim();
    if (!name.isEmpty() && !name.equals(this.spritesheetResource.getName())) {
      changeSprite(resource -> resource.setName(name));
    }
  }

  private void applyGrid() {
    if (this.binding || this.spritesheetResource == null || this.image == null) {
      return;
    }
    int columns = (int) this.columnsSpinner.getValue();
    int rows = (int) this.rowsSpinner.getValue();
    changeSprite(resource -> {
      resource.setWidth(Math.max(1, this.image.getWidth() / columns));
      resource.setHeight(Math.max(1, this.image.getHeight() / rows));
      resource.setKeyframes(resizedDurations(resource.getKeyframes(), columns * rows));
    });
  }

  private void applyKeyframes() {
    if (this.binding || this.spritesheetResource == null) {
      return;
    }
    int[] durations = new int[this.keyframeModel.getRowCount()];
    for (int row = 0; row < durations.length; row++) {
      Object value = this.keyframeModel.getValueAt(row, 2);
      durations[row] = value instanceof Number number
          ? Math.max(1, number.intValue())
          : Animation.DEFAULT_FRAME_DURATION;
    }
    if (!Arrays.equals(durations, this.spritesheetResource.getKeyframes())) {
      changeSprite(resource -> resource.setKeyframes(durations));
    }
    updateTotalDuration();
  }

  private void changeSprite(java.util.function.Consumer<SpritesheetResource> change) {
    SpritesheetResource before = new SpritesheetResource(this.spritesheetResource);
    boolean wasVirtual = isVirtualMirrored(this.spritesheetResource.getName());
    String previousName = this.spritesheetResource.getName();
    change.accept(this.spritesheetResource);
    SpritesheetResource after = new SpritesheetResource(this.spritesheetResource);
    if (wasVirtual) {
      if (Editor.instance().getGameFile() != null
          && !Editor.instance().getGameFile().getSpriteSheets().contains(this.spritesheetResource)) {
        Editor.instance().getGameFile().getSpriteSheets().add(this.spritesheetResource);
      }
      if (Game.world().environment() != null) {
        UndoManager.instance().resourceChanged(
          () -> {
            if (Editor.instance().getGameFile() != null) {
              Editor.instance().getGameFile().getSpriteSheets().removeIf(x -> x.getName().equalsIgnoreCase(after.getName()));
            }
            Resources.spritesheets().remove(after.getName());
            if (Editor.instance().getGameFile() != null) {
              Editor.instance().loadSpriteSheets(Editor.instance().getGameFile().getSpriteSheets(), true);
            }
            String opp = CreaturePanel.oppositeHorizontalDirection(after.getName());
            SpritesheetResource oppRes = findSpriteResource(opp);
            if (oppRes != null) {
              bind(oppRes);
            }
          },
          () -> {
            if (Editor.instance().getGameFile() != null && !Editor.instance().getGameFile().getSpriteSheets().contains(after)) {
              Editor.instance().getGameFile().getSpriteSheets().add(after);
            }
            applySnapshot(after, after.getName());
          }
        );
      }
    } else {
      if (Game.world().environment() != null) {
        UndoManager.instance().resourceChanged(() -> applySnapshot(before), () -> applySnapshot(after));
      }
    }
    applySnapshot(after, previousName);
  }

  private void applySnapshot(SpritesheetResource snapshot) {
    applySnapshot(snapshot, this.spritesheetResource.getName());
  }

  private void applySnapshot(SpritesheetResource snapshot, String previousName) {
    this.spritesheetResource.copyFrom(snapshot);
    Resources.spritesheets().remove(previousName);
    Resources.spritesheets().remove(this.spritesheetResource.getName());
    if (Editor.instance().getGameFile() != null) {
      Editor.instance().loadSpriteSheets(Editor.instance().getGameFile().getSpriteSheets(), true);
    }
    refreshControls();
    UI.showSpriteInspector(this.spritesheetResource);
  }

  private BufferedImage frameImage(int frame) {
    if (this.image == null || frame < 0 || frame >= frameCount()) {
      return null;
    }
    int frameWidth = this.spritesheetResource.getWidth();
    int frameHeight = this.spritesheetResource.getHeight();
    int x = frame % getColumns() * frameWidth;
    int y = frame / getColumns() * frameHeight;
    return this.image.getSubimage(x, y, frameWidth, frameHeight);
  }

  private int getColumns() {
    return this.image == null || this.spritesheetResource == null
        ? 1
        : Math.max(1, this.image.getWidth() / this.spritesheetResource.getWidth());
  }

  private int getRows() {
    return this.image == null || this.spritesheetResource == null
        ? 1
        : Math.max(1, this.image.getHeight() / this.spritesheetResource.getHeight());
  }

  private int frameCount() {
    return this.image == null || this.spritesheetResource == null ? 0 : getColumns() * getRows();
  }

  private int durationAt(int frame) {
    if (frame < 0 || frame >= this.keyframeModel.getRowCount()) {
      return Animation.DEFAULT_FRAME_DURATION;
    }
    Object value = this.keyframeModel.getValueAt(frame, 2);
    return value instanceof Number number ? Math.max(1, number.intValue()) : Animation.DEFAULT_FRAME_DURATION;
  }

  private static JSpinner createGridSpinner() {
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    ControlBehavior.apply(spinner);
    return spinner;
  }

  private static JPanel field(String labelText, Component component) {
    JPanel field = new JPanel(new BorderLayout(0, 3));
    field.setOpaque(false);
    JLabel label = mutedLabel(labelText);
    field.add(label, BorderLayout.NORTH);
    field.add(component, BorderLayout.CENTER);
    return field;
  }

  private static JLabel mutedLabel(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(Style.mutedText());
    return label;
  }

  private JButton actionButton(javax.swing.Icon icon, String key, Runnable action) {
    JButton button = Style.iconButton(icon);
    configureButton(button, key);
    button.addActionListener(_ -> action.run());
    return button;
  }

  private static void configureButton(javax.swing.AbstractButton button, String key) {
    String text = Resources.strings().get(key);
    button.setToolTipText(text);
    button.getAccessibleContext().setAccessibleName(text);
  }

  private static DefaultTableCellRenderer centerRenderer() {
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(SwingConstants.CENTER);
    return renderer;
  }

  private static String dimensions(int width, int height) {
    return Resources.strings().get(
        "spriteEditor_dimensions", String.valueOf(width), String.valueOf(height));
  }

  private static int[] resizedDurations(int[] current, int length) {
    int[] durations = new int[length];
    for (int i = 0; i < length; i++) {
      durations[i] = i < current.length ? current[i] : Animation.DEFAULT_FRAME_DURATION;
    }
    return durations;
  }

  private static BufferedImage scaleNearest(BufferedImage source, int width, int height, boolean preserveRatio) {
    if (source == null || width <= 0 || height <= 0) {
      return null;
    }
    int targetWidth = width;
    int targetHeight = height;
    if (preserveRatio) {
      double scale = Math.min((double) width / source.getWidth(), (double) height / source.getHeight());
      targetWidth = Math.max(1, (int) Math.floor(source.getWidth() * scale));
      targetHeight = Math.max(1, (int) Math.floor(source.getHeight() * scale));
    }
    BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = scaled.createGraphics();
    graphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
    graphics.dispose();
    return scaled;
  }

  void setNameForTest(String name) {
    this.nameField.setText(name);
    applyName();
  }

  void setColumnsForTest(int columns) {
    this.columnsSpinner.setValue(columns);
  }

  void setDurationForTest(int frame, int duration) {
    this.keyframeModel.setValueAt(duration, frame, 2);
  }

  int getSelectedFrameForTest() {
    return this.currentFrame;
  }

  private final class PreviewCanvas extends JPanel {
    private boolean checkerboard = true;

    private PreviewCanvas() {
      setOpaque(true);
      setBackground(Style.raisedSurface());
      setBorder(BorderFactory.createLineBorder(Style.border()));
      addComponentListener(new ComponentAdapter() {
        @Override public void componentResized(ComponentEvent event) {
          if (previewFit) {
            updatePreviewZoom();
          }
        }
      });
    }

    private void setCheckerboard(boolean checkerboard) {
      this.checkerboard = checkerboard;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);
      Graphics2D g = (Graphics2D) graphics.create();
      if (this.checkerboard) {
        paintCheckerboard(g, getWidth(), getHeight());
      }
      BufferedImage frame = frameImage(currentFrame);
      if (frame != null) {
        float scale = currentPreviewZoom();
        int width = Math.max(1, Math.round(frame.getWidth() * scale));
        int height = Math.max(1, Math.round(frame.getHeight() * scale));
        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(frame, (getWidth() - width) / 2, (getHeight() - height) / 2, width, height, null);
      }
      g.dispose();
    }
  }

  private final class SpriteGridCanvas extends JPanel {
    private boolean checkerboard = true;
    private int hoveredFrame = -1;
    private int imageX;
    private int imageY;
    private int imageWidth;
    private int imageHeight;

    private SpriteGridCanvas() {
      setOpaque(true);
      setBackground(Style.raisedSurface());
      setBorder(BorderFactory.createLineBorder(Style.border()));
      MouseAdapter mouse = new MouseAdapter() {
        @Override
        public void mouseMoved(MouseEvent event) {
          hoveredFrame = frameAt(event.getX(), event.getY());
          repaint();
        }

        @Override
        public void mouseExited(MouseEvent event) {
          hoveredFrame = -1;
          repaint();
        }

        @Override
        public void mouseClicked(MouseEvent event) {
          int frame = frameAt(event.getX(), event.getY());
          if (frame >= 0) {
            selectFrame(frame, true);
          }
        }
      };
      addMouseListener(mouse);
      addMouseMotionListener(mouse);
    }

    private void setCheckerboard(boolean checkerboard) {
      this.checkerboard = checkerboard;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);
      if (image == null) {
        return;
      }
      Graphics2D g = (Graphics2D) graphics.create();
      if (this.checkerboard) {
        paintCheckerboard(g, getWidth(), getHeight());
      }
      int scale = Math.max(1, Math.min((getWidth() - 24) / image.getWidth(), (getHeight() - 24) / image.getHeight()));
      this.imageWidth = image.getWidth() * scale;
      this.imageHeight = image.getHeight() * scale;
      this.imageX = (getWidth() - this.imageWidth) / 2;
      this.imageY = (getHeight() - this.imageHeight) / 2;
      g.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      g.drawImage(image, this.imageX, this.imageY, this.imageWidth, this.imageHeight, null);

      int cellWidth = this.imageWidth / getColumns();
      int cellHeight = this.imageHeight / getRows();
      g.setStroke(new BasicStroke(1f));
      for (int frame = 0; frame < frameCount(); frame++) {
        int x = this.imageX + frame % getColumns() * cellWidth;
        int y = this.imageY + frame / getColumns() * cellHeight;
        if (frame == this.hoveredFrame) {
          g.setColor(Style.cardSelected());
          g.fillRect(x, y, cellWidth, cellHeight);
        }
        g.setColor(Style.border());
        g.drawRect(x, y, cellWidth, cellHeight);
        g.setColor(new Color(Style.background().getRed(), Style.background().getGreen(), Style.background().getBlue(), 190));
        g.fillRoundRect(x + 4, y + 4, 20, 18, 5, 5);
        g.setColor(Style.text());
        g.drawString(String.valueOf(frame + 1), x + 10, y + 17);
      }

      int selected = keyframeTable.getSelectedRow();
      if (selected >= 0) {
        paintFrameOutline(g, selected, cellWidth, cellHeight, 2f);
      }
      if (currentFrame >= 0) {
        int x = this.imageX + currentFrame % getColumns() * cellWidth;
        int y = this.imageY + currentFrame / getColumns() * cellHeight;
        g.setColor(Style.accent());
        g.fillRect(x + 2, y + cellHeight - 3, cellWidth - 3, 3);
      }
      g.dispose();
    }

    private void paintFrameOutline(Graphics2D graphics, int frame, int cellWidth, int cellHeight, float width) {
      int x = this.imageX + frame % getColumns() * cellWidth;
      int y = this.imageY + frame / getColumns() * cellHeight;
      graphics.setStroke(new BasicStroke(width));
      graphics.setColor(Style.accent());
      graphics.drawRect(x + 1, y + 1, cellWidth - 2, cellHeight - 2);
    }

    private int frameAt(int x, int y) {
      if (x < this.imageX || y < this.imageY
          || x >= this.imageX + this.imageWidth || y >= this.imageY + this.imageHeight) {
        return -1;
      }
      int column = (x - this.imageX) * getColumns() / this.imageWidth;
      int row = (y - this.imageY) * getRows() / this.imageHeight;
      return row * getColumns() + column;
    }
  }

  private final class FrameRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      JLabel label = (JLabel) super.getTableCellRendererComponent(
          table, "", selected, focused, row, column);
      label.setHorizontalAlignment(SwingConstants.CENTER);
      BufferedImage frame = frameImage(row);
      BufferedImage scaled = scaleNearest(frame, 42, 30, true);
      label.setIcon(scaled != null ? new ImageIcon(scaled) : null);
      return label;
    }
  }

  private final class DurationRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      JLabel label = (JLabel) super.getTableCellRendererComponent(
          table, value + " ms", selected, focused, row, column);
      label.setHorizontalAlignment(SwingConstants.RIGHT);
      label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
      return label;
    }
  }

  private static void paintCheckerboard(Graphics2D graphics, int width, int height) {
    TransparencyGrid.paint(graphics, width, height);
  }

  private static Color blend(Color first, Color second, float amount) {
    float inverse = 1f - amount;
    return new Color(
        Math.round(first.getRed() * inverse + second.getRed() * amount),
        Math.round(first.getGreen() * inverse + second.getGreen() * amount),
        Math.round(first.getBlue() * inverse + second.getBlue() * amount));
  }

  private static final class ViewportWidthPanel extends JPanel implements Scrollable {
    @Override
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
      return 16;
    }

    @Override
    public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
      return Math.max(16, visibleRect.height - 16);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
      return false;
    }
  }
}
