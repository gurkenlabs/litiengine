package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Swing panel for editing the editable properties of an {@link Animation}: its looping flag, the
 * per-frame durations, and a "set all" convenience spinner. A live preview cycles through the
 * animation's sprites at the configured speed so the user can validate their changes before
 * applying them.
 *
 * <p>
 * The animation itself is not mutated until {@link #applyChanges()} is called, allowing callers to
 * discard the dialog without side effects.
 * </p>
 */
public class AnimationEditPanel extends JPanel {
  private static final int PREVIEW_SIZE = 128;
  private static final String DURATION_COLUMN = "assetpanel_animation_duration";
  private static final String FRAME_COLUMN = "assetpanel_animation_frame";

  private final Animation animation;
  private final int[] workingDurations;
  private boolean workingLoop;

  private final JLabel previewLabel;
  private final JCheckBox loopCheckBox;
  private final JSpinner setAllSpinner;
  private final DurationTableModel tableModel;

  private Timer previewTimer;
  private int previewIndex;

  /**
   * Creates a new editor for the given animation. Modifications are kept in a working copy and only
   * applied when {@link #applyChanges()} is called.
   *
   * @param animation The animation to edit.
   */
  public AnimationEditPanel(Animation animation) {
    super(new BorderLayout(10, 10));
    this.animation = animation;
    int[] durations = animation.getKeyFrameDurations();
    this.workingDurations = durations.length == 0 ? new int[0] : durations.clone();
    this.workingLoop = animation.isLooping();

    this.previewLabel = createPreviewLabel();
    this.loopCheckBox = new JCheckBox(Resources.strings().get("assetpanel_animation_loop"), workingLoop);
    this.tableModel = new DurationTableModel();
    this.setAllSpinner = createSetAllSpinner();

    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(buildLeftPanel(), BorderLayout.WEST);
    add(buildRightPanel(), BorderLayout.CENTER);

    startPreview();
  }

  private JLabel createPreviewLabel() {
    JLabel label = new JLabel();
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setVerticalAlignment(SwingConstants.CENTER);
    label.setPreferredSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));
    label.setBorder(BorderFactory.createEtchedBorder());
    return label;
  }

  private JSpinner createSetAllSpinner() {
    int initial = workingDurations.length > 0 ? workingDurations[0] : Animation.DEFAULT_FRAME_DURATION;
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, 1, 60_000, 10));
    spinner.setPreferredSize(new Dimension(90, spinner.getPreferredSize().height));
    return spinner;
  }

  private JPanel buildLeftPanel() {
    JPanel left = new JPanel(new BorderLayout(5, 5));
    left.add(previewLabel, BorderLayout.NORTH);

    Spritesheet sheet = animation.getSpritesheet();
    String sheetName = sheet != null && sheet.getName() != null ? sheet.getName() : "-";
    JPanel info = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 0;
    info.add(new JLabel(Resources.strings().get("assetpanel_animation_name") + ":"), c);
    c.gridx = 1;
    info.add(new JLabel(animation.getName()), c);
    c.gridx = 0;
    c.gridy = 1;
    info.add(new JLabel(Resources.strings().get("assetpanel_animation_spritesheet") + ":"), c);
    c.gridx = 1;
    info.add(new JLabel(sheetName), c);
    c.gridx = 0;
    c.gridy = 2;
    info.add(new JLabel(Resources.strings().get("assetpanel_animation_frames") + ":"), c);
    c.gridx = 1;
    info.add(new JLabel(String.valueOf(workingDurations.length)), c);

    left.add(info, BorderLayout.CENTER);
    return left;
  }

  private JPanel buildRightPanel() {
    JPanel right = new JPanel(new BorderLayout(5, 5));

    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    controls.add(loopCheckBox);
    loopCheckBox.addActionListener(e -> workingLoop = loopCheckBox.isSelected());

    JPanel setAll = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    setAll.add(new JLabel(Resources.strings().get("assetpanel_animation_setAll") + ":"));
    setAll.add(setAllSpinner);
    JButton apply = new JButton(Resources.strings().get("assetpanel_animation_apply"));
    apply.addActionListener(e -> applySetAll());
    setAll.add(apply);

    JPanel header = new JPanel(new BorderLayout());
    header.add(controls, BorderLayout.NORTH);
    header.add(setAll, BorderLayout.SOUTH);
    right.add(header, BorderLayout.NORTH);

    JTable table = new JTable(tableModel);
    table.setRowHeight(22);
    table.setFillsViewportHeight(true);
    TableColumn frameCol = table.getColumnModel().getColumn(0);
    frameCol.setMaxWidth(80);
    frameCol.setPreferredWidth(60);
    frameCol.setCellRenderer(centerRenderer());

    JScrollPane scroll = new JScrollPane(table,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setPreferredSize(new Dimension(260, 220));
    right.add(scroll, BorderLayout.CENTER);

    return right;
  }

  private static DefaultTableCellRenderer centerRenderer() {
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(SwingConstants.CENTER);
    return renderer;
  }

  private void applySetAll() {
    int value = (Integer) setAllSpinner.getValue();
    for (int i = 0; i < workingDurations.length; i++) {
      workingDurations[i] = value;
    }
    tableModel.fireTableDataChanged();
  }

  private void startPreview() {
    Spritesheet sheet = animation.getSpritesheet();
    if (sheet == null || workingDurations.length == 0) {
      previewLabel.setText(Resources.strings().get("assetpanel_animation_noPreview"));
      return;
    }

    previewIndex = 0;
    renderPreviewFrame();
    previewTimer = new Timer(Math.max(workingDurations[0], 1), e -> {
      previewIndex = (previewIndex + 1) % workingDurations.length;
      renderPreviewFrame();
      ((Timer) e.getSource()).setDelay(Math.max(workingDurations[previewIndex], 1));
    });
    previewTimer.start();
  }

  private void renderPreviewFrame() {
    Spritesheet sheet = animation.getSpritesheet();
    if (sheet == null) {
      return;
    }
    BufferedImage img = sheet.getSprite(previewIndex);
    if (img == null) {
      return;
    }
    BufferedImage scaled = Imaging.scale(img, PREVIEW_SIZE, PREVIEW_SIZE, true);
    previewLabel.setIcon(new ImageIcon(scaled != null ? scaled : img));
  }

  /**
   * Stops any background timers used by this panel. Should be called once the dialog hosting this
   * panel is closed.
   */
  public void dispose() {
    if (previewTimer != null) {
      previewTimer.stop();
      previewTimer = null;
    }
  }

  /**
   * Applies the working copy of the edited values to the underlying {@link Animation}.
   */
  public void applyChanges() {
    animation.setLooping(workingLoop);
    if (workingDurations.length > 0) {
      animation.setKeyFrameDurations(workingDurations);
    }
  }

  /** @return The animation being edited. */
  public Animation getAnimation() {
    return animation;
  }

  /**
   * Table model that lets the user edit the per-frame durations of the animation.
   */
  private final class DurationTableModel extends AbstractTableModel {
    private final String[] columns = {
      Resources.strings().get(FRAME_COLUMN),
      Resources.strings().get(DURATION_COLUMN)
    };

    @Override public int getRowCount() {
      return workingDurations.length;
    }

    @Override public int getColumnCount() {
      return columns.length;
    }

    @Override public String getColumnName(int column) {
      return columns[column];
    }

    @Override public Class<?> getColumnClass(int columnIndex) {
      return Integer.class;
    }

    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex == 1;
    }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
      return columnIndex == 0 ? rowIndex : workingDurations[rowIndex];
    }

    @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (columnIndex != 1 || aValue == null) {
        return;
      }
      try {
        int value = Integer.parseInt(aValue.toString());
        if (value < 1) {
          value = 1;
        }
        workingDurations[rowIndex] = value;
        fireTableCellUpdated(rowIndex, columnIndex);
      } catch (NumberFormatException ignored) {
        // ignore invalid input - the cell editor commits on focus loss
      }
    }
  }
}

