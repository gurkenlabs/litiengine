package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.controller.Editor;
import java.util.List;
import javax.swing.JComboBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class ScriptInspectorPanelTest {

  @BeforeEach
  void setUp() {
    if (Editor.instance().getGameFile() == null) {
      Editor.instance().load(null, false);
    }
    if (Editor.instance().getGameFile() != null) {
      Editor.instance().getGameFile().getScripts().clear();
    }
  }

  @Test
  void testEnvironmentScriptInspectorRefreshesAvailableScripts() {
    EnvironmentScriptInspectorPanel panel = new EnvironmentScriptInspectorPanel();
    TmxMap map = new TmxMap();
    panel.bind(map);

    JComboBox<ScriptDefinition> combo = findComboBox(panel);
    assertNotNull(combo);
    assertEquals(0, combo.getItemCount());

    ScriptDefinition envScript = new ScriptDefinition("TestEnvScript", "java", "TestEnvScript.java", "TestEnvScript", ScriptHostType.ENVIRONMENT);
    Editor.instance().getGameFile().getScripts().add(envScript);

    panel.refreshAvailableScripts();

    assertEquals(1, combo.getItemCount());
    assertEquals("TestEnvScript", combo.getItemAt(0).getId());
  }

  @Test
  void testEntityScriptInspectorRefreshesAvailableScripts() {
    ScriptBindingsInspectorPanel panel = new ScriptBindingsInspectorPanel();
    MapObject creatureObject = new MapObject();
    creatureObject.setType(MapObjectType.CREATURE.name());
    panel.bind(creatureObject);

    JComboBox<ScriptDefinition> combo = findComboBox(panel);
    assertNotNull(combo);
    assertEquals(0, combo.getItemCount());

    ScriptDefinition entityScript = new ScriptDefinition("TestCreatureScript", "java", "TestCreatureScript.java", "TestCreatureScript", ScriptHostType.ENTITY);
    entityScript.setTargetType("de.gurkenlabs.litiengine.entities.Creature");
    Editor.instance().getGameFile().getScripts().add(entityScript);

    panel.refreshAvailableScripts();

    assertEquals(1, combo.getItemCount());
    assertEquals("TestCreatureScript", combo.getItemAt(0).getId());
  }

  @SuppressWarnings("unchecked")
  private static JComboBox<ScriptDefinition> findComboBox(java.awt.Container container) {
    for (java.awt.Component comp : container.getComponents()) {
      if (comp instanceof JComboBox<?> cb) {
        return (JComboBox<ScriptDefinition>) cb;
      }
      if (comp instanceof java.awt.Container child) {
        JComboBox<ScriptDefinition> found = findComboBox(child);
        if (found != null) return found;
      }
    }
    return null;
  }
}
