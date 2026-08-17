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
    if (component instanceof javax.swing.AbstractButton button && (button instanceof javax.swing.JCheckBox || button instanceof javax.swing.JRadioButton)) {
      button.setMargin(new java.awt.Insets(0, 0, 0, 0));
      button.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
      button.setOpaque(false);
      return component;
    }

    if (component instanceof javax.swing.text.JTextComponent jText) {
      applyRoundedControlShape(jText);
      jText.setCaretColor(Style.accent());
      if (jText instanceof JTextField jTextField) {
        jTextField.addFocusListener(
            new FocusAdapter() {
              @Override
              public void focusGained(final FocusEvent e) {
                jTextField.selectAll();
              }
            });
      }
      return component;
    }

    if (component instanceof JComponent jComponent) {
      applyRoundedControlShape(jComponent);
    }

    if (component instanceof JSpinner spinner) {
      JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
      JTextField textField = editor.getTextField();
      applyRoundedControlShape(textField);
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
    component.putClientProperty("JTextField.arc", Style.CORNER_RADIUS);
  }

  private static void applyRoundedControlShape(JComponent component) {
    component.putClientProperty("JComponent.arc", Style.CORNER_RADIUS);
    component.putClientProperty("JTextField.arc", Style.CORNER_RADIUS);
  }
}
