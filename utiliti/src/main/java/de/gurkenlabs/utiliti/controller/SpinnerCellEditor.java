package de.gurkenlabs.utiliti.controller;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SpinnerCellEditor extends DefaultCellEditor {
  @Serial
  private static final long serialVersionUID = 9136956833481466003L;

  private final JSpinner spinner;
  private final JSpinner.DefaultEditor editor;
  private final JTextField textField;
  private boolean valueSet;

  // Initializes the spinner.
  public SpinnerCellEditor() {
    super(ControlBehavior.apply(new JTextField()));
    spinner = new JSpinner();
    editor = ((JSpinner.DefaultEditor) spinner.getEditor());
    textField = editor.getTextField();
    textField.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusGained(FocusEvent fe) {
            SwingUtilities.invokeLater(
                () -> {
                  if (valueSet) {
                    textField.setCaretPosition(1);
                  }
                });
          }
        });
    textField.addActionListener(ae -> this.stopCellEditing());
  }

  // Prepares the spinner component and returns it.
  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {
    if (!valueSet) {
      spinner.setValue(value);
    }

    SwingUtilities.invokeLater(textField::requestFocus);
    return spinner;
  }

  @Override
  public boolean isCellEditable(EventObject eo) {
    if (eo instanceof KeyEvent keyEvent) {
      textField.setText(String.valueOf(keyEvent.getKeyChar()));
      valueSet = true;
    } else {
      valueSet = false;
    }
    return true;
  }

  // Returns the spinners current value.
  @Override
  public Object getCellEditorValue() {
    return spinner.getValue();
  }

  @Override
  public boolean stopCellEditing() {
    try {
      editor.commitEdit();
      spinner.commitEdit();
    } catch (java.text.ParseException e) {
      JOptionPane.showMessageDialog(null, "Invalid value, discarding.");
    }
    return super.stopCellEditing();
  }
}
