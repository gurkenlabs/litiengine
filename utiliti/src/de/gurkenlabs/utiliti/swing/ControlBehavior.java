package de.gurkenlabs.utiliti.swing;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public final class ControlBehavior {
  private ControlBehavior() {
  }

  public static <T extends Component> T apply(T component) {
    if (component instanceof JTextField) {
      component.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(final FocusEvent e) {
          ((JTextField) component).selectAll();
        }
      });
    }

    if (component instanceof JSpinner) {
      JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) ((JSpinner) component).getEditor();
      JTextField textField = editor.getTextField();
      textField.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(final FocusEvent e) {
          SwingUtilities.invokeLater(() -> {
            JTextField tf = (JTextField) e.getSource();
            tf.selectAll();
          });
        }
      });
    }
    
    return component;
  }
}
