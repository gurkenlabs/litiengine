package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** A list of map-entity references that persists map IDs while showing descriptive entity labels. */
public class EntityReferenceList extends JPanel {
  private final transient List<ActionListener> listeners = new ArrayList<>();
  private final transient Supplier<List<EntityReference>> candidatesSupplier;
  private final DefaultTableModel model;
  private final JTable table;
  private final JButton buttonPlus;

  public EntityReferenceList(String columnName, Supplier<List<EntityReference>> candidatesSupplier) {
    this.setOpaque(false);
    this.candidatesSupplier = candidatesSupplier;
    this.table = new JTable();
    this.table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    this.table.getTableHeader().setReorderingAllowed(false);
    this.table.setShowHorizontalLines(true);
    this.table.setShowVerticalLines(false);
    this.table.setGridColor(Style.COLOR_BORDER);
    this.table.setIntercellSpacing(new Dimension(0, 1));
    this.table.setModel(new DefaultTableModel(new Object[][] {}, new String[] {columnName}) {
      @Override
      public Class<?> getColumnClass(int column) {
        return EntityReference.class;
      }

      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    });
    this.table.setDefaultRenderer(EntityReference.class, new EntityReferenceRenderer());
    this.table.getColumnModel().getColumn(0).setResizable(false);
    this.model = (DefaultTableModel) this.table.getModel();
    this.model.addTableModelListener(event -> this.fireActionEvent());

    JScrollPane scrollPane = new JScrollPane(this.table);
    JButton buttonMinus = Style.textButton("−");
    buttonMinus.setToolTipText("Remove selected entity references");
    buttonMinus.addActionListener(event -> this.removeSelected());

    this.buttonPlus = Style.textButton("+");
    this.buttonPlus.setToolTipText("Add an entity reference");
    this.buttonPlus.addActionListener(event -> this.showPicker());

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
                            .addComponent(this.buttonPlus, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonMinus, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))));
    groupLayout.setVerticalGroup(
        groupLayout
            .createParallelGroup(Alignment.TRAILING)
            .addGroup(
                groupLayout
                    .createParallelGroup(Alignment.TRAILING)
                    .addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                    .addGroup(
                        Alignment.LEADING,
                        groupLayout
                            .createSequentialGroup()
                            .addComponent(this.buttonPlus, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(buttonMinus, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())));
    this.setLayout(groupLayout);
  }

  public void addActionListener(ActionListener listener) {
    this.listeners.add(listener);
  }

  public void clear() {
    this.model.setRowCount(0);
  }

  public String getJoinedString() {
    List<String> ids = new ArrayList<>();
    for (int row = 0; row < this.model.getRowCount(); row++) {
      ids.add(Integer.toString(((EntityReference) this.model.getValueAt(row, 0)).id()));
    }
    return String.join(",", ids);
  }

  public void setJoinedString(String value) {
    this.model.setRowCount(0);
    if (value == null || value.isBlank()) {
      return;
    }

    Map<Integer, EntityReference> candidates = new LinkedHashMap<>();
    for (EntityReference candidate : this.candidatesSupplier.get()) {
      candidates.putIfAbsent(candidate.id(), candidate);
    }
    for (String token : value.split(",")) {
      try {
        int id = Integer.parseInt(token.trim());
        this.model.addRow(new Object[] {candidates.getOrDefault(id, EntityReference.missing(id))});
      } catch (NumberFormatException _) {
        // The engine ignores non-numeric references; the inspector does the same without losing valid ones.
      }
    }
  }

  List<String> getDisplayValuesForTest() {
    List<String> values = new ArrayList<>();
    for (int row = 0; row < this.model.getRowCount(); row++) {
      values.add(this.model.getValueAt(row, 0).toString());
    }
    return values;
  }

  Icon getReferenceIconForTest(int row) {
    return ((JLabel) this.table.prepareRenderer(this.table.getCellRenderer(row, 0), row, 0)).getIcon();
  }

  void addReferenceForTest(EntityReference reference) {
    this.addReference(reference);
  }

  void removeReferencesForTest(int... rows) {
    for (int index = rows.length - 1; index >= 0; index--) {
      this.model.removeRow(rows[index]);
    }
  }

  private void addReference(EntityReference reference) {
    this.model.addRow(new Object[] {reference});
  }

  private void removeSelected() {
    int[] rows = this.table.getSelectedRows();
    for (int index = 0; index < rows.length; index++) {
      this.model.removeRow(rows[index] - index);
    }
  }

  private void showPicker() {
    List<EntityReference> candidates = new ArrayList<>(this.candidatesSupplier.get());
    candidates.sort(Comparator.comparing(EntityReference::toString, String.CASE_INSENSITIVE_ORDER));
    JPopupMenu popup = new JPopupMenu();
    JTextField filter = new JTextField();
    filter.setToolTipText("Filter entities by name, type, or ID");
    DefaultListModel<EntityReference> listModel = new DefaultListModel<>();
    JList<EntityReference> list = new JList<>(listModel);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setVisibleRowCount(8);
    list.setCellRenderer(new EntityReferenceListCellRenderer());

    Runnable updateResults = () -> {
      String query = filter.getText().trim().toLowerCase(java.util.Locale.ROOT);
      listModel.clear();
      for (EntityReference candidate : candidates) {
        if (candidate.searchText().contains(query)) {
          listModel.addElement(candidate);
        }
      }
      if (!listModel.isEmpty()) {
        list.setSelectedIndex(0);
      }
    };
    DocumentListener filterListener = new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent event) { updateResults.run(); }
      @Override public void removeUpdate(DocumentEvent event) { updateResults.run(); }
      @Override public void changedUpdate(DocumentEvent event) { updateResults.run(); }
    };
    filter.getDocument().addDocumentListener(filterListener);
    list.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override public void mouseClicked(java.awt.event.MouseEvent event) {
        if (event.getClickCount() == 2) {
          selectPickerValue(list, popup);
        }
      }
    });
    list.addKeyListener(new KeyAdapter() {
      @Override public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
          selectPickerValue(list, popup);
        }
      }
    });
    filter.addActionListener(event -> selectPickerValue(list, popup));

    JPanel contents = new JPanel(new BorderLayout(0, 4));
    contents.add(filter, BorderLayout.NORTH);
    contents.add(new JScrollPane(list), BorderLayout.CENTER);
    popup.add(contents);
    updateResults.run();
    popup.show(this.buttonPlus, 0, this.buttonPlus.getHeight());
    filter.requestFocusInWindow();
  }

  private void selectPickerValue(JList<EntityReference> list, JPopupMenu popup) {
    EntityReference reference = list.getSelectedValue();
    if (reference != null) {
      this.addReference(reference);
      popup.setVisible(false);
    }
  }

  private void fireActionEvent() {
    for (ActionListener listener : this.listeners) {
      listener.actionPerformed(null);
    }
  }

  private static final class EntityReferenceRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      setReferenceIcon(this, value);
      return this;
    }
  }

  private static final class EntityReferenceListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      setReferenceIcon(label, value);
      return label;
    }
  }

  private static void setReferenceIcon(JLabel label, Object value) {
    if (value instanceof EntityReference reference && !reference.missing()) {
      label.setIcon(Icons.forMapObjectType(MapObjectType.get(reference.type())));
    } else {
      label.setIcon(null);
    }
  }

  public record EntityReference(int id, String name, String type, boolean missing) {
    public static EntityReference missing(int id) {
      return new EntityReference(id, null, null, true);
    }

    String searchText() {
      return String.join(" ", this.name == null ? "" : this.name, this.type == null ? "" : this.type, Integer.toString(this.id))
          .toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public String toString() {
      if (this.missing) {
        return "Missing entity (#" + this.id + ")";
      }
      String displayName = this.name == null || this.name.isBlank() ? "#" + this.id : this.name;
      return displayName + " [#" + this.id + "]";
    }
  }
}
