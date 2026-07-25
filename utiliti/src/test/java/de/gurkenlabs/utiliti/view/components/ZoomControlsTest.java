package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class ZoomControlsTest {

  @Test
  void displaysZoomAndInvokesActions() {
    AtomicInteger action = new AtomicInteger();
    ZoomControls controls = new ZoomControls(
        () -> action.set(-1), () -> action.set(1), () -> action.set(2), "Fit preview");

    controls.setZoom(1.945);

    assertEquals("195%", controls.getZoomText());
    JPanel zoomGroup = assertInstanceOf(JPanel.class, controls.getComponent(0));
    JButton zoomOut = assertInstanceOf(JButton.class, zoomGroup.getComponent(0));
    JButton zoomIn = assertInstanceOf(JButton.class, zoomGroup.getComponent(2));
    JPanel fitGroup = assertInstanceOf(JPanel.class, controls.getComponent(2));
    JButton fit = assertInstanceOf(JButton.class, fitGroup.getComponent(0));
    assertTrue(Boolean.TRUE.equals(
        zoomOut.getClientProperty("Editor.groupedToolbarButton")));
    assertNotNull(zoomGroup.getBorder());

    zoomOut.doClick();
    assertEquals(-1, action.get());
    zoomIn.doClick();
    assertEquals(1, action.get());
    fit.doClick();
    assertEquals(2, action.get());
  }
}
