package de.gurkenlabs.utiliti.swing;

import de.gurkenlabs.litiengine.util.ArrayUtilities;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;

public class TextList extends JPanel {
  private final transient List<ActionListener> listeners;

  private final DefaultTableModel model;
  private final JTable table;

  public TextList(String columnName) {
    this.listeners = new ArrayList<>();
    this.table = new JTable();
    this.table.getTableHeader().setReorderingAllowed(false);
    this.table.setModel(
        new DefaultTableModel(new Object[][] {}, new String[] {columnName}) {
          final Class<?>[] columnTypes = new Class<?>[] {Integer.class};

          @Override
          public Class<?> getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
          }
        });
    this.table.getColumnModel().getColumn(0).setResizable(false);
    this.model = (DefaultTableModel) this.table.getModel();
    this.model.addTableModelListener(
        t -> {
          for (ActionListener listener : this.listeners) {
            listener.actionPerformed(null);
          }
        });

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportView(table);

    JButton buttonMinus = new JButton("-");
    buttonMinus.addActionListener(
        a -> {
          int[] rows = table.getSelectedRows();
          for (int i = 0; i < rows.length; i++) {
            model.removeRow(rows[i] - i);
          }
        });

    JButton buttonPlus = new JButton("+");
    buttonPlus.addActionListener(a -> model.addRow(new Object[] {0}));

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout
            .createParallelGroup(Alignment.TRAILING)
            .addGroup(
                groupLayout
                    .createSequentialGroup()
                    .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.TRAILING)
                            .addComponent(
                                buttonPlus,
                                GroupLayout.PREFERRED_SIZE,
                                41,
                                GroupLayout.PREFERRED_SIZE)
                            .addComponent(
                                buttonMinus,
                                GroupLayout.PREFERRED_SIZE,
                                41,
                                GroupLayout.PREFERRED_SIZE))
                    .addGap(0)));
    groupLayout.setVerticalGroup(
        groupLayout
            .createParallelGroup(Alignment.TRAILING)
            .addGroup(
                groupLayout
                    .createParallelGroup(Alignment.TRAILING)
                    .addComponent(
                        scrollPane,
                        Alignment.LEADING,
                        GroupLayout.DEFAULT_SIZE,
                        90,
                        Short.MAX_VALUE)
                    .addGroup(
                        Alignment.LEADING,
                        groupLayout
                            .createSequentialGroup()
                            .addComponent(
                                buttonPlus,
                                GroupLayout.PREFERRED_SIZE,
                                34,
                                GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(
                                buttonMinus,
                                GroupLayout.PREFERRED_SIZE,
                                37,
                                GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())));
    setLayout(groupLayout);
  }

  public void addActionListener(ActionListener listener) {
    this.listeners.add(listener);
  }

  public void removeActionListener(ActionListener listener) {
    this.listeners.remove(listener);
  }

  public void clear() {
    this.model.setRowCount(0);
  }

  public void add(Object row) {
    this.model.addRow(new Object[] {row});
  }

  public String getJoinedString() {
    List<String> rows = new ArrayList<>();
    for (int row = 0; row < model.getRowCount(); row++) {
      Object activator = model.getValueAt(row, 0);
      if (activator != null) {
        rows.add(activator.toString());
      }
    }

    return String.join(",", rows);
  }

  public void setJoinedString(String rows) {
    this.model.setRowCount(0);
    for (int target : ArrayUtilities.splitInt(rows)) {
      this.model.addRow(new Object[] {target});
    }
  }
}
