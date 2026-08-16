package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import org.junit.jupiter.api.Test;

class ScriptTemplateFactoryTest {

  @Test
  void testGameScriptTemplate() {
    String source = ScriptTemplateFactory.generateTemplate(
        "MainGame", ScriptHostType.GAME, null, "com.example.game", "MainGame");
    assertTrue(source.contains("package com.example.game;"));
    assertTrue(source.contains("public class MainGame extends GameScript"));
    assertTrue(source.contains("@ScriptInfo(id = \"MainGame\", host = ScriptHostType.GAME)"));
    assertTrue(source.contains("void onStarted()"));
    assertTrue(source.contains("void update()"));
  }

  @Test
  void testEnvironmentScriptTemplate() {
    String source = ScriptTemplateFactory.generateTemplate(
        "Level1Script", ScriptHostType.ENVIRONMENT, null, "com.example.map", "Level1Script");
    assertTrue(source.contains("package com.example.map;"));
    assertTrue(source.contains("public class Level1Script extends EnvironmentScript"));
    assertTrue(source.contains("@ScriptInfo(id = \"Level1Script\", host = ScriptHostType.ENVIRONMENT)"));
    assertTrue(source.contains("void onLoaded()"));
  }

  @Test
  void testEntityCreatureScriptTemplate() {
    String source = ScriptTemplateFactory.generateTemplate(
        "EnemyAI", ScriptHostType.ENTITY, "de.gurkenlabs.litiengine.entities.Creature", "com.example.ai", "EnemyAI");
    assertTrue(source.contains("package com.example.ai;"));
    assertTrue(source.contains("public class EnemyAI extends CreatureScript"));
    assertTrue(source.contains("@ScriptInfo(id = \"EnemyAI\", host = ScriptHostType.ENTITY, target = Creature.class)"));
    assertTrue(source.contains("void onHit(EntityHitEvent event)"));
  }

  @Test
  void testEntityCustomPropScriptTemplate() {
    String source = ScriptTemplateFactory.generateTemplate(
        "ChestScript", ScriptHostType.ENTITY, "de.gurkenlabs.litiengine.entities.Prop", "com.example.prop", "ChestScript");
    assertTrue(source.contains("package com.example.prop;"));
    assertTrue(source.contains("public class ChestScript extends EntityScript<Prop>"));
    assertTrue(source.contains("@ScriptInfo(id = \"ChestScript\", host = ScriptHostType.ENTITY, target = Prop.class)"));
  }

  @Test
  void testSynchronizeDeclaration() {
    ScriptDefinition def = new ScriptDefinition("BossLogic", "java", "scripts/BossLogic.java",
        "BossLogic", ScriptHostType.ENTITY);
    def.setTargetType("de.gurkenlabs.litiengine.entities.Creature");

    String oldSource = """
        @ScriptInfo(id = "OldId", host = ScriptHostType.GAME)
        public class BossLogic extends GameScript {
          @Override
          public void onStarted() {}
        }
        """;

    String updated = ScriptTemplateFactory.synchronizeDeclaration(oldSource, def);
    assertTrue(updated.contains("@ScriptInfo(id = \"BossLogic\", host = ScriptHostType.ENTITY, target = de.gurkenlabs.litiengine.entities.Creature.class)"));
    assertTrue(updated.contains("public class BossLogic extends CreatureScript"));
    assertTrue(updated.contains("void onLoaded()"));
    assertFalse(updated.contains("void onStarted()"));
  }
}
