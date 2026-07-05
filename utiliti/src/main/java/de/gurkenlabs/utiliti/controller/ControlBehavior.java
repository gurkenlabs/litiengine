package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public final class ControlBehavior {
  static final Color INPUT_BG = new Color(36, 37, 42);

  private ControlBehavior() {}

  public static <T extends Component> T apply(T component) {
    if (component instanceof JTextField jTextField) {
      applyRoundedControlShape(jTextField);
      jTextField.setBackground(INPUT_BG);
      jTextField.setCaretColor(Style.COLOR_ACCENT_BLUE);
      jTextField.addFocusListener(
          new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
              jTextField.selectAll();
            }
          });
      return component;
    }

    if (component instanceof JComponent jComponent) {
      applySquareControlShape(jComponent);
    }

    if (component instanceof JSpinner spinner) {
      JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
      JTextField textField = editor.getTextField();
      applySquareControlShape(textField);
      textField.setBackground(INPUT_BG);
      textField.setCaretColor(Style.COLOR_ACCENT_BLUE);
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

  public static void applyRoundedBorder(JComponent component) {
    component.putClientProperty("JComponent.arc", 8);
    component.putClientProperty("JComponent.roundRect", true);
  }

  private static void applyRoundedControlShape(JComponent component) {
    component.putClientProperty("JComponent.arc", 8);
    component.putClientProperty("JComponent.roundRect", true);
    component.putClientProperty("JTextField.arc", 8);
  }

  private static void applySquareControlShape(JComponent component) {
    component.putClientProperty("JComponent.arc", 0);
    component.putClientProperty("JComponent.roundRect", false);
    component.putClientProperty("JTextField.arc", 0);
    component.putClientProperty("JComboBox.arc", 0);
    component.putClientProperty("JSpinner.arc", 0);
  }
}
