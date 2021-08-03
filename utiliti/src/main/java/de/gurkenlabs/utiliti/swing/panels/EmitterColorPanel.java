package de.gurkenlabs.utiliti.swing.panels;

import com.github.weisj.darklaf.components.border.DarkBorders;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.SwingHelpers;
import de.gurkenlabs.utiliti.swing.ControlBehavior;
import de.gurkenlabs.utiliti.swing.Icons;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class EmitterColorPanel extends PropertyPanel {
  private final DefaultTableModel model;
  private final JTable table;
  private final Box ctrlButtonBox;
  private final JButton btnAdd;
  private final JButton btnRemove;
  private final JButton btnEdit;
  private final JScrollPane scrollPanel;
  private final JPanel colorControls;
  private JSpinner colorVariance;
  private JSpinner alphaVariance;

  public EmitterColorPanel() {
    super();
    setMinimumSize(new Dimension(PANEL_WIDTH, CONTROL_HEIGHT * 6));
    setPreferredSize(new Dimension(PANEL_WIDTH, CONTROL_HEIGHT * 6));
    model = new DefaultTableModel(0, 1);
    table = new JTable(model);
    table.getColumnModel().getColumn(0).setCellRenderer(new ColorListCellRenderer());
    table.setTableHeader(null);
    scrollPanel = new JScrollPane(table);
    scrollPanel.setBorder(DarkBorders.createLineBorder(1, 1, 1, 1));

    ctrlButtonBox = Box.createVerticalBox();
    btnAdd = new JButton(Icons.ADD);
    btnAdd.setPreferredSize(BUTTON_SIZE);
    btnAdd.setMinimumSize(BUTTON_SIZE);
    btnAdd.setMaximumSize(BUTTON_SIZE);
    btnRemove = new JButton(Icons.DELETE);
    btnRemove.setPreferredSize(BUTTON_SIZE);
    btnRemove.setMinimumSize(BUTTON_SIZE);
    btnRemove.setMaximumSize(BUTTON_SIZE);
    btnEdit = new JButton(Icons.COLOR);
    btnEdit.setPreferredSize(BUTTON_SIZE);
    btnEdit.setMinimumSize(BUTTON_SIZE);
    btnEdit.setMaximumSize(BUTTON_SIZE);

    ctrlButtonBox.add(btnAdd);
    ctrlButtonBox.add(btnRemove);
    ctrlButtonBox.add(btnEdit);

    colorControls = new JPanel();
    GroupLayout grplayout = new GroupLayout(colorControls);
    grplayout.setHorizontalGroup(
        grplayout.createSequentialGroup().addComponent(ctrlButtonBox).addComponent(scrollPanel));
    grplayout.setVerticalGroup(
        grplayout.createParallelGroup().addComponent(ctrlButtonBox).addComponent(scrollPanel));
    colorControls.setLayout(grplayout);

    colorVariance =
        new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_COLOR_VARIANCE, 0d, 1d, STEP_FINE));
    alphaVariance =
        new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_ALPHA_VARIANCE, 0d, 1d, STEP_FINE));

    setLayout(createLayout());
    setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    model.setRowCount(0);
    table.clearSelection();
    colorVariance.setValue(EmitterData.DEFAULT_COLOR_VARIANCE);
    alphaVariance.setValue(EmitterData.DEFAULT_ALPHA_VARIANCE);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    setColors(mapObject.getStringValue(MapObjectProperty.Emitter.COLORS));
    colorVariance.setValue(
        mapObject.getFloatValue(
            MapObjectProperty.Emitter.COLORVARIANCE, EmitterData.DEFAULT_COLOR_VARIANCE));
    alphaVariance.setValue(
        mapObject.getFloatValue(
            MapObjectProperty.Emitter.ALPHAVARIANCE, EmitterData.DEFAULT_ALPHA_VARIANCE));
  }

  private void setColors(String commaSeparatedHexstrings) {
    if (commaSeparatedHexstrings == null) {
      return;
    }
    model.setRowCount(0);
    for (String colorStr : commaSeparatedHexstrings.split(",")) {
      model.addRow(new Object[] {colorStr});
    }
  }

  protected LayoutManager createLayout() {
    LayoutItem[] layoutItems =
        new LayoutItem[] {
          new LayoutItem(colorControls, CONTROL_HEIGHT * 3),
          new LayoutItem("emitter_colorVariance", colorVariance, CONTROL_HEIGHT),
          new LayoutItem("emitter_alphaVariance", alphaVariance, CONTROL_HEIGHT)
        };
    return this.createLayout(layoutItems);
  }

  private void setupChangedListeners() {
    btnAdd.addActionListener(
        a -> model.addRow(new Object[] {ColorHelper.encode(EmitterData.DEFAULT_COLOR.brighter())}));
    btnRemove.addActionListener(a -> model.removeRow(table.getSelectedRow()));
    btnEdit.addActionListener(
        a -> {
          Color previousColor =
              ColorHelper.decode(
                  table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString());
          final Color result =
              JColorChooser.showDialog(
                  null, Resources.strings().get("particle_editcolor"), previousColor);
          table.setValueAt(
              ColorHelper.encode(result), table.getSelectedRow(), table.getSelectedColumn());
        });
    setup(table, MapObjectProperty.Emitter.COLORS);
    setup(colorVariance, MapObjectProperty.Emitter.COLORVARIANCE);
    setup(alphaVariance, MapObjectProperty.Emitter.ALPHAVARIANCE);
  }

  private class ColorListCellRenderer extends JTextField implements TableCellRenderer {
    private transient Border focusBorder = DarkBorders.createLineBorder(1, 1, 1, 1);

    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      this.setColumns(9);
      this.setHorizontalAlignment(SwingConstants.CENTER);
      SwingHelpers.updateColorTextField(this, ColorHelper.decode((String) value));
      this.setBorder(hasFocus ? focusBorder : null);
      if (hasFocus) {
        ControlBehavior.apply(this);
      }
      return this;
    }
  }
}
