package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class ToastTest {

  @Test
  void replacesActiveToastAndKeepsInteractionInsideToastBounds() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      JRootPane rootPane = new JRootPane();
      rootPane.setSize(600, 400);
      rootPane.getLayeredPane().setSize(600, 400);

      Toast.show(rootPane, "First");
      Toast.show(rootPane, "Second", () -> { });

      Toast toast = findToast(rootPane);
      assertEquals(1, countToasts(rootPane));
      assertTrue(toast.getWidth() < rootPane.getWidth());
      assertTrue(toast.getY() > rootPane.getHeight() / 2);
      assertEquals("Second", findChild(toast, JLabel.class).getText());
      findChild(toast, JButton.class).doClick();
    });
  }

  @Test
  void actionRunsAndDismissesToast() throws Exception {
    AtomicBoolean invoked = new AtomicBoolean();
    SwingUtilities.invokeAndWait(() -> {
      JRootPane rootPane = new JRootPane();
      rootPane.setSize(600, 400);
      rootPane.getLayeredPane().setSize(600, 400);

      Toast.show(rootPane, "Deleted", () -> invoked.set(true));
      findChild(findToast(rootPane), JButton.class).doClick();

      assertTrue(invoked.get());
      assertEquals(0, countToasts(rootPane));
    });
  }

  private static Toast findToast(JRootPane rootPane) {
    return Arrays.stream(rootPane.getLayeredPane().getComponents())
      .filter(Toast.class::isInstance)
      .map(Toast.class::cast)
      .findFirst()
      .orElseThrow();
  }

  private static long countToasts(JRootPane rootPane) {
    return Arrays.stream(rootPane.getLayeredPane().getComponents())
      .filter(Toast.class::isInstance)
      .count();
  }

  private static <T extends Component> T findChild(Toast toast, Class<T> type) {
    return Arrays.stream(toast.getComponents())
      .filter(type::isInstance)
      .map(type::cast)
      .findFirst()
      .orElseThrow();
  }
}
