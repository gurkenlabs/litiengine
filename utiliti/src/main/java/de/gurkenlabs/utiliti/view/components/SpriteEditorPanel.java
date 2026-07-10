package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.litiengine.util.io.Codec;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

public class SpriteEditorPanel extends JPanel {
  private final JLabel titleLabel;
  private final JLabel animationPreviewLabel;
  private final JLabel previewLabel;
  private final JLabel imageSizeLabel;
  private final JLabel frameSizeLabel;
  private final JTextField nameField;
  private final JSpinner columnsSpinner;
  private final JSpinner rowsSpinner;
  private final DefaultTableModel keyframeModel;
  private final JTable keyframeTable;
  private SpritesheetResource spritesheetResource;
  private BufferedImage image;
  private boolean binding;
  private final Timer animationPreviewTimer;

  public SpriteEditorPanel() {
    super(new BorderLayout(0, 8));
    setOpaque(true);
    setBackground(Style.COLOR_BG);
    setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));

    this.titleLabel = new JLabel("No sprite selected");
    this.titleLabel.setForeground(Style.COLOR_TEXT);
    this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(java.awt.Font.BOLD));
    add(this.titleLabel, BorderLayout.NORTH);

    this.animationPreviewLabel = new JLabel("", SwingConstants.CENTER);
    this.animationPreviewLabel.setOpaque(true);
    this.animationPreviewLabel.setBackground(Style.COLOR_SURFACE);
    this.animationPreviewLabel.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));
    this.animationPreviewLabel.setPreferredSize(new Dimension(0, 112));

    this.previewLabel = new JLabel("", SwingConstants.CENTER);
    this.previewLabel.setOpaque(true);
    this.previewLabel.setBackground(Style.COLOR_SURFACE);
    this.previewLabel.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));
    this.previewLabel.setPreferredSize(new Dimension(0, 152));

    this.imageSizeLabel = new JLabel("-");
    this.frameSizeLabel = new JLabel("-");
    this.nameField = new JTextField();
    this.nameField.addActionListener(_ -> applyName());
    this.nameField.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        applyName();
      }
    });

    this.columnsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    this.rowsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    ChangeListener gridChanged = _ -> applyGrid();
    this.columnsSpinner.addChangeListener(gridChanged);
    this.rowsSpinner.addChangeListener(gridChanged);

    this.keyframeModel = new DefaultTableModel(new Object[][] {}, new String[] {"Frame", "Duration (ms)"}) {
      @Override public Class<?> getColumnClass(int columnIndex) {
        return Integer.class;
      }

      @Override public boolean isCellEditable(int row, int column) {
        return column == 1;
      }
    };
    this.keyframeModel.addTableModelListener(_ -> applyKeyframes());
    this.keyframeTable = new JTable(this.keyframeModel);
    this.keyframeTable.setFillsViewportHeight(true);
    this.keyframeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.keyframeTable.getTableHeader().setReorderingAllowed(false);

    JPanel details = new JPanel(new GridLayout(4, 1, 0, 6));
    details.setOpaque(false);
    details.add(row("Image Size", this.imageSizeLabel));
    details.add(row("Frame Size", this.frameSizeLabel));
    details.add(row("Name", this.nameField));
    JPanel grid = new JPanel(new GridLayout(1, 2, 8, 0));
    grid.setOpaque(false);
    grid.add(row("Columns", this.columnsSpinner));
    grid.add(row("Rows", this.rowsSpinner));
    details.add(grid);

    JPanel content = new JPanel(new BorderLayout());
    content.setOpaque(false);
    JPanel form = new JPanel();
    form.setOpaque(false);
    form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
    this.animationPreviewLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
    this.previewLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 152));
    details.setMaximumSize(new Dimension(Integer.MAX_VALUE, details.getPreferredSize().height));
    form.add(labeledPreview("Animation Preview", this.animationPreviewLabel));
    form.add(Box.createVerticalStrut(8));
    form.add(labeledPreview("Sprite Grid", this.previewLabel));
    form.add(Box.createVerticalStrut(8));
    form.add(details);
    form.add(Box.createVerticalStrut(8));
    JPanel frames = new JPanel(new BorderLayout(0, 4));
    frames.setOpaque(false);
    JLabel framesLabel = new JLabel("Keyframes");
    framesLabel.setForeground(Style.COLOR_TEXT);
    frames.add(framesLabel, BorderLayout.NORTH);
    JScrollPane frameScroll = new JScrollPane(this.keyframeTable);
    frameScroll.setPreferredSize(new Dimension(0, 180));
    frameScroll.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));
    frames.add(frameScroll, BorderLayout.CENTER);
    frames.setMaximumSize(new Dimension(Integer.MAX_VALUE, frames.getPreferredSize().height));
    form.add(frames);
    content.add(form, BorderLayout.NORTH);
    add(content, BorderLayout.CENTER);

    this.animationPreviewTimer = new Timer(80, _ -> updateAnimationPreview());
    this.animationPreviewTimer.start();
  }

  public void bind(SpritesheetResource spritesheetResource) {
    this.spritesheetResource = spritesheetResource;
    this.image = spritesheetResource != null ? Codec.decodeImage(spritesheetResource.getImage()) : null;
    refreshControls();
  }

  private void refreshControls() {
    this.binding = true;
    try {
      this.keyframeModel.setRowCount(0);
      if (this.spritesheetResource == null || this.image == null) {
        this.titleLabel.setText("No sprite selected");
        this.animationPreviewLabel.setIcon(null);
        this.previewLabel.setIcon(null);
        this.imageSizeLabel.setText("-");
        this.frameSizeLabel.setText("-");
        this.nameField.setText("");
        return;
      }
      this.titleLabel.setText("Sprite: " + this.spritesheetResource.getName());
      this.nameField.setText(this.spritesheetResource.getName());
      int columns = Math.max(1, this.image.getWidth() / this.spritesheetResource.getWidth());
      int rows = Math.max(1, this.image.getHeight() / this.spritesheetResource.getHeight());
      this.columnsSpinner.setModel(new SpinnerNumberModel(columns, 1, this.image.getWidth(), 1));
      this.rowsSpinner.setModel(new SpinnerNumberModel(rows, 1, this.image.getHeight(), 1));
      this.imageSizeLabel.setText(this.image.getWidth() + " x " + this.image.getHeight() + " px");
      this.frameSizeLabel.setText(this.spritesheetResource.getWidth() + " x " + this.spritesheetResource.getHeight() + " px");
      for (int i = 0; i < columns * rows; i++) {
        int[] durations = this.spritesheetResource.getKeyframes();
        int duration = i < durations.length ? durations[i] : Animation.DEFAULT_FRAME_DURATION;
        this.keyframeModel.addRow(new Object[] {i + 1, duration});
      }
      this.previewLabel.setIcon(new ImageIcon(renderPreview(columns, rows)));
      updateAnimationPreview();
    } finally {
      this.binding = false;
    }
  }

  private JPanel row(String labelText, java.awt.Component component) {
    JPanel row = new JPanel(new BorderLayout(8, 0));
    row.setOpaque(false);
    JLabel label = new JLabel(labelText);
    label.setForeground(Style.COLOR_TEXT);
    label.setPreferredSize(new Dimension(84, 24));
    row.add(label, BorderLayout.WEST);
    row.add(component, BorderLayout.CENTER);
    return row;
  }

  private static JPanel labeledPreview(String labelText, JLabel preview) {
    JPanel panel = new JPanel(new BorderLayout(0, 4));
    panel.setOpaque(false);
    JLabel label = new JLabel(labelText);
    label.setForeground(Style.COLOR_TEXT);
    panel.add(label, BorderLayout.NORTH);
    panel.add(preview, BorderLayout.CENTER);
    return panel;
  }

  private void updateAnimationPreview() {
    if (!this.isShowing() || this.spritesheetResource == null || this.image == null) {
      return;
    }
    int frameWidth = this.spritesheetResource.getWidth();
    int frameHeight = this.spritesheetResource.getHeight();
    int columns = Math.max(1, this.image.getWidth() / frameWidth);
    int frameCount = Math.max(1, columns * Math.max(1, this.image.getHeight() / frameHeight));
    int[] durations = resizedDurations(this.spritesheetResource.getKeyframes(), frameCount);
    int totalDuration = 0;
    for (int duration : durations) {
      totalDuration += Math.max(1, duration);
    }
    int elapsed = (int) (System.currentTimeMillis() % totalDuration);
    int frame = 0;
    for (; frame < durations.length - 1; frame++) {
      elapsed -= Math.max(1, durations[frame]);
      if (elapsed < 0) {
        break;
      }
    }
    int x = frame % columns * frameWidth;
    int y = frame / columns * frameHeight;
    BufferedImage sprite = this.image.getSubimage(x, y, frameWidth, frameHeight);
    BufferedImage scaled = Imaging.scale(sprite, 96, 96, true);
    this.animationPreviewLabel.setIcon(scaled != null ? new ImageIcon(scaled) : null);
  }

  private BufferedImage renderPreview(int columns, int rows) {
    BufferedImage preview = Imaging.scale(this.image, 340, 140, true);
    if (preview == null) {
      return this.image;
    }
    Graphics2D g = preview.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(Style.COLOR_COLLISION_BORDER);
    for (int column = 1; column < columns; column++) {
      int x = preview.getWidth() * column / columns;
      g.drawLine(x, 0, x, preview.getHeight() - 1);
    }
    for (int row = 1; row < rows; row++) {
      int y = preview.getHeight() * row / rows;
      g.drawLine(0, y, preview.getWidth() - 1, y);
    }
    g.drawRect(0, 0, preview.getWidth() - 1, preview.getHeight() - 1);
    g.dispose();
    return preview;
  }

  private void applyName() {
    if (this.binding || this.spritesheetResource == null) {
      return;
    }
    changeSprite(resource -> resource.setName(this.nameField.getText().trim()));
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
      Object value = this.keyframeModel.getValueAt(row, 1);
      durations[row] = value instanceof Number number ? Math.max(1, number.intValue()) : Animation.DEFAULT_FRAME_DURATION;
    }
    changeSprite(resource -> resource.setKeyframes(durations));
  }

  private static int[] resizedDurations(int[] current, int length) {
    int[] durations = new int[length];
    for (int i = 0; i < length; i++) {
      durations[i] = i < current.length ? current[i] : Animation.DEFAULT_FRAME_DURATION;
    }
    return durations;
  }

  private void changeSprite(java.util.function.Consumer<SpritesheetResource> change) {
    SpritesheetResource before = new SpritesheetResource(this.spritesheetResource);
    change.accept(this.spritesheetResource);
    SpritesheetResource after = new SpritesheetResource(this.spritesheetResource);
    if (Game.world().environment() != null) {
      UndoManager.instance().resourceChanged(() -> applySnapshot(before), () -> applySnapshot(after));
    }
    applySnapshot(after, before.getName());
  }

  private void applySnapshot(SpritesheetResource snapshot) {
    applySnapshot(snapshot, this.spritesheetResource.getName());
  }

  private void applySnapshot(SpritesheetResource snapshot, String previousName) {
    this.spritesheetResource.copyFrom(snapshot);
    Resources.spritesheets().remove(previousName);
    Resources.spritesheets().remove(this.spritesheetResource.getName());
    Editor.instance().loadSpriteSheets(Editor.instance().getGameFile().getSpriteSheets(), true);
    refreshControls();
    UI.showSpriteInspector(this.spritesheetResource);
  }

  void setNameForTest(String name) {
    this.nameField.setText(name);
    applyName();
  }

  void setColumnsForTest(int columns) {
    this.columnsSpinner.setValue(columns);
  }
}
