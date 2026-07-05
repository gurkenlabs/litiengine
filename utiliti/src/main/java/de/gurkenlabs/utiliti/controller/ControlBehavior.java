package de.gurkenlabs.utiliti.controller;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public final class ControlBehavior {
  private ControlBehavior() {}

  public static <T extends Component> T apply(T component) {
    if (component instanceof JComponent jComponent) {
      applySquareControlShape(jComponent);
    }

    if (component instanceof JTextField jTextField) {
      component.addFocusListener(
          new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
              jTextField.selectAll();
            }
          });
    }

    if (component instanceof JSpinner spinner) {
      JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
      JTextField textField = editor.getTextField();
      applySquareControlShape(textField);
      textField.addFocusListener(
          new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
              SwingUtilities.invokeLater(
                  () -> {
                    JTextField tf = (JTextField) e.getSource();
                    tf.selectAll();
                  });
            }
          });
      spinner.putClientProperty("JSpinner.arrowButton", false);
    }

    if (component instanceof JComboBox<?> comboBox && comboBox.getEditor() != null) {
      Component editor = comboBox.getEditor().getEditorComponent();
      if (editor instanceof JComponent jComponent) {
        applySquareControlShape(jComponent);
      }
    }

    return component;
  }

  private static void applySquareControlShape(JComponent component) {
    component.putClientProperty("JComponent.arc", 0);
    component.putClientProperty("JComponent.roundRect", false);
    component.putClientProperty("JTextField.arc", 0);
    component.putClientProperty("JComboBox.arc", 0);
    component.putClientProperty("JSpinner.arc", 0);
  }
}
