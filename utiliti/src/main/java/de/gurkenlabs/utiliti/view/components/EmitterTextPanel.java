package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.components.border.DarkBorders;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class EmitterTextPanel extends PropertyPanel {
  private final DefaultTableModel model;
  private final JTable table;
  private final Box ctrlButtonBox;
  private final JButton btnAdd;
  private final JButton btnRemove;
  private final JScrollPane scrollPanel;
  private final JPanel textControls;

  public EmitterTextPanel() {
    super();
    setMinimumSize(new Dimension(PANEL_WIDTH, CONTROL_HEIGHT * 6));
    setPreferredSize(new Dimension(PANEL_WIDTH, CONTROL_HEIGHT * 6));

    model = new DefaultTableModel(0, 1);
    table = new JTable(model);
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

    ctrlButtonBox.add(btnAdd);
    ctrlButtonBox.add(btnRemove);

    textControls = new JPanel();
    GroupLayout grplayout = new GroupLayout(textControls);
    grplayout.setHorizontalGroup(
        grplayout.createSequentialGroup().addComponent(ctrlButtonBox).addComponent(scrollPanel));
    grplayout.setVerticalGroup(
        grplayout.createParallelGroup().addComponent(ctrlButtonBox).addComponent(scrollPanel));
    textControls.setLayout(grplayout);

    setLayout(createLayout());
    setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    model.setRowCount(0);
    table.clearSelection();
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    setTexts(mapObject.getStringValue(MapObjectProperty.Particle.TEXTS, null));
  }

  protected LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem[] {new LayoutItem(textControls, CONTROL_HEIGHT * 2)};
    return this.createLayout(layoutItems);
  }

  private void setTexts(String commaSeparatedTexts) {
    if (commaSeparatedTexts == null) {
      return;
    }
    model.setRowCount(0);
    for (String colorStr : commaSeparatedTexts.split(",")) {
      model.addRow(new Object[] {colorStr});
    }
  }

  private void setupChangedListeners() {
    btnAdd.addActionListener(a -> model.addRow(new Object[] {EmitterData.DEFAULT_TEXT}));
    btnRemove.addActionListener(a -> model.removeRow(table.getSelectedRow()));
    setup(table, MapObjectProperty.Particle.TEXTS);
  }
}
