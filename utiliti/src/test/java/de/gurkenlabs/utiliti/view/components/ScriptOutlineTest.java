package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScriptOutlineTest {
  @Test
  void buildsClassScopedFieldsMethodsAndDependencies() {
    String source = """
      package game.scripts

      import de.gurkenlabs.litiengine.scripting.*
      import game.ai.ZombieAIComponent

      @ScriptInfo(id = "zombie", host = ScriptHostType.ENTITY)
      class ZombieBehavior extends CreatureScript {
        @ScriptProperty
        String displayName = "Zombie"
        ZombieAIComponent component

        void onLoaded() {
          def localValue = host()
        }

        void update() {
          component = new ZombieAIComponent()
        }
      }
      """;

    ScriptOutline.Symbol outline = ScriptOutline.parse(source);

    assertNotNull(outline);
    assertEquals("ZombieBehavior", outline.name());
    assertEquals("CreatureScript", outline.detail());
    assertEquals(List.of("Fields", "Methods", "Dependencies"),
      outline.children().stream().map(ScriptOutline.Symbol::name).toList());
    assertEquals(List.of("displayName", "component"), names(group(outline, "Fields")));
    assertEquals(List.of("onLoaded", "update"), names(group(outline, "Methods")));
    assertEquals(List.of("ZombieAIComponent"), names(group(outline, "Dependencies")));
    assertFalse(names(group(outline, "Fields")).contains("de"));
  }

  @Test
  void capturesReadableSignaturesAndSourceLines() {
    String source = """
      class TestScript extends CreatureScript {
        private int speed
        boolean canMove(String direction, double distance) {
          return true
        }
      }
      """;

    ScriptOutline.Symbol outline = ScriptOutline.parse(source);
    ScriptOutline.Symbol field = group(outline, "Fields").children().getFirst();
    ScriptOutline.Symbol method = group(outline, "Methods").children().getFirst();

    assertEquals("int", field.detail());
    assertEquals(1, field.line());
    assertEquals("(String direction, double distance) : boolean", method.detail());
    assertEquals(2, method.line());
  }

  private static ScriptOutline.Symbol group(ScriptOutline.Symbol outline, String name) {
    return outline.children().stream().filter(symbol -> symbol.name().equals(name)).findFirst().orElseThrow();
  }

  private static List<String> names(ScriptOutline.Symbol group) {
    return group.children().stream().map(ScriptOutline.Symbol::name).toList();
  }
}
