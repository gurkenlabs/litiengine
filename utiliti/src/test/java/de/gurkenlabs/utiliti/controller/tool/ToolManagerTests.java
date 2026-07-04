package de.gurkenlabs.utiliti.controller.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ToolManagerTests {
  private ToolManager manager;

  @BeforeEach
  void setUp() {
    ToolManager.reset();
    manager = ToolManager.instance();
  }

  @AfterEach
  void tearDown() {
    ToolManager.reset();
  }

  @Test
  void testSingleton() {
    assertEquals(manager, ToolManager.instance());
  }

  @Test
  void testRegisterTool() {
    DummyTool tool = new DummyTool();
    manager.register(tool);
    assertEquals(1, manager.getTools().size());
    assertTrue(manager.getTools().contains(tool));
  }

  @Test
  void testFirstRegisteredToolIsActive() {
    DummyTool tool = new DummyTool();
    manager.register(tool);
    assertEquals(tool, manager.getActiveTool());
  }

  @Test
  void testSetActiveTool() {
    DummyTool tool1 = new DummyTool();
    DummyTool tool2 = new DummyTool();
    manager.register(tool1);
    manager.register(tool2);
    manager.setActiveTool(tool2);
    assertEquals(tool2, manager.getActiveTool());
  }

  @Test
  void testActivateDeactivateCalled() {
    DummyTool tool1 = new DummyTool();
    DummyTool tool2 = new DummyTool();
    manager.register(tool1);
    manager.register(tool2);
    manager.setActiveTool(tool2);
    assertTrue(tool1.deactivatedCalled);
    assertTrue(tool2.activatedCalled);
  }

  @Test
  void testSetSameToolDoesNothing() {
    DummyTool tool = new DummyTool();
    manager.register(tool);
    tool.activatedCalled = false;
    manager.setActiveTool(tool);
    assertFalse(tool.activatedCalled);
  }

  @Test
  void testListenerNotified() {
    DummyTool tool = new DummyTool();
    manager.register(tool);
    List<Integer> calls = new ArrayList<>();
    manager.addListener(() -> calls.add(1));
    DummyTool tool2 = new DummyTool();
    manager.register(tool2);
    manager.setActiveTool(tool2);
    assertEquals(1, calls.size());
  }

  @Test
  void testRemoveListener() {
    DummyTool tool = new DummyTool();
    manager.register(tool);
    List<Integer> calls = new ArrayList<>();
    Runnable listener = () -> calls.add(1);
    manager.addListener(listener);
    manager.removeListener(listener);
    DummyTool tool2 = new DummyTool();
    manager.register(tool2);
    manager.setActiveTool(tool2);
    assertEquals(0, calls.size());
  }

  @Test
  void testGetToolsReturnsUnmodifiable() {
    DummyTool tool = new DummyTool();
    manager.register(tool);
    assertThrows(UnsupportedOperationException.class, () -> manager.getTools().add(new DummyTool()));
  }

  @Test
  void testReset() {
    DummyTool tool = new DummyTool();
    manager.register(tool);
    ToolManager.reset();
    ToolManager instance2 = ToolManager.instance();
    assertNull(instance2.getActiveTool());
    assertTrue(instance2.getTools().isEmpty());
  }

  private static class DummyTool implements Tool {
    boolean activatedCalled;
    boolean deactivatedCalled;

    @Override
    public String getName() {
      return "Dummy";
    }

    @Override
    public Icon getIcon() {
      return null;
    }

    @Override
    public void activated() {
      activatedCalled = true;
    }

    @Override
    public void deactivated() {
      deactivatedCalled = true;
    }
  }
}
