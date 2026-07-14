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
  static final Color INPUT_BG = Style.COLOR_INPUT_BG;

  private ControlBehavior() {}

  public static <T extends Component> T apply(T component) {
    if (component instanceof JTextField jTextField) {
      applyRoundedControlShape(jTextField);
      jTextField.setBackground(inputBackground());
      jTextField.setCaretColor(Style.accent());
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
      applyRoundedControlShape(jComponent);
    }

    if (component instanceof JSpinner spinner) {
      JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
      JTextField textField = editor.getTextField();
      applyRoundedControlShape(textField);
      textField.setBackground(inputBackground());
      textField.setCaretColor(Style.accent());
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
        applyRoundedControlShape(jComponent);
      }
    }

    return component;
  }

  public static void applyRoundedBorder(JComponent component) {
    component.putClientProperty("JComponent.arc", Style.CORNER_RADIUS);
    component.putClientProperty("JComponent.roundRect", true);
  }

  private static void applyRoundedControlShape(JComponent component) {
    component.putClientProperty("JComponent.arc", Style.CORNER_RADIUS);
    component.putClientProperty("JComponent.roundRect", true);
    component.putClientProperty("JTextField.arc", Style.CORNER_RADIUS);
  }

  private static Color inputBackground() {
    Color color = javax.swing.UIManager.getColor("TextField.background");
    return color != null ? color : INPUT_BG;
  }
}
