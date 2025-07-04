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
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
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

    model = new DefaultTableModel(0, 1);
    table = new JTable(model);
    table.setTableHeader(null);
    scrollPanel = new JScrollPane(table);
    scrollPanel.setBorder(DarkBorders.createLineBorder(1, 1, 1, 1));
    scrollPanel.setPreferredSize(new Dimension(CONTROL_WIDTH * 2, CONTROL_HEIGHT * 3));

    ctrlButtonBox = Box.createVerticalBox();
    btnAdd = new JButton(Icons.ADD_16);
    btnRemove = new JButton(Icons.DELETE_16);
    btnAdd.setMaximumSize(BUTTON_SIZE);
    btnRemove.setMaximumSize(BUTTON_SIZE);

    ctrlButtonBox.add(btnAdd);
    ctrlButtonBox.add(btnRemove);
    ctrlButtonBox.add(Box.createVerticalGlue());

    textControls = new JPanel();
    GroupLayout grplayout = new GroupLayout(textControls);
    grplayout.setHorizontalGroup(
      grplayout.createSequentialGroup().addComponent(ctrlButtonBox).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(scrollPanel));
    grplayout.setVerticalGroup(grplayout.createParallelGroup(Alignment.LEADING).addComponent(ctrlButtonBox).addComponent(scrollPanel));
    textControls.setLayout(grplayout);
    textControls.setBorder(new EmptyBorder(CONTROL_MARGIN, 0, CONTROL_MARGIN, 0));

    setLayout(createLayout());
    setupChangedListeners();
  }

  @Override protected void clearControls() {
    model.setRowCount(0);
    table.clearSelection();
  }

  @Override protected void setControlValues(IMapObject mapObject) {
    setTexts(mapObject.getStringValue(MapObjectProperty.Particle.TEXTS, null));
  }

  protected LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem[] {new LayoutItem(textControls, CONTROL_HEIGHT * 3)};
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
