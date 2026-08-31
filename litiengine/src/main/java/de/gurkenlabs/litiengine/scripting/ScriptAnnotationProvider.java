package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.entities.EntityHitEvent;
import de.gurkenlabs.litiengine.entities.EntityMessageEvent;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.physics.CollisionEvent;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Completion;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.CompletionKind;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Position;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Range;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.TextEdit;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/// Provides completions and snippet templates for script annotations (@ScriptProperty, @ScriptInfo) and @Override event lifecycle methods.
final class ScriptAnnotationProvider {
  private ScriptAnnotationProvider() {}

  record EventOverride(
      String methodName,
      String signature,
      String detail,
      String doc,
      String template,
      Class<?>[] requiredImports,
      ScriptHostType[] hosts
  ) {}

  private static final List<EventOverride> EVENT_OVERRIDES = List.of(
      // Environment Lifecycle
      new EventOverride(
          "onLoaded",
          "public void onLoaded()",
          "public void onLoaded() - Lifecycle Event Hook",
          "Called after the environment / entity is loaded and active in the world.",
          "@Override\npublic void onLoaded() {\n  ${0}\n}",
          new Class<?>[] {},
          new ScriptHostType[] {ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onUnloaded",
          "protected void onUnloaded()",
          "protected void onUnloaded() - Lifecycle Event Hook",
          "Called when the script or entity is detached during map unload.",
          "@Override\nprotected void onUnloaded() {\n  ${0}\n}",
          new Class<?>[] {},
          new ScriptHostType[] {ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onCleared",
          "protected void onCleared()",
          "protected void onCleared() - Environment Lifecycle Hook",
          "Called when the attached environment is cleared while remaining active.",
          "@Override\nprotected void onCleared() {\n  ${0}\n}",
          new Class<?>[] {},
          new ScriptHostType[] {ScriptHostType.ENVIRONMENT}
      ),
      new EventOverride(
          "onEntityAdded",
          "protected void onEntityAdded(IEntity entity)",
          "protected void onEntityAdded(IEntity entity) - Environment Event Hook",
          "Called when a new entity is added to the active environment.",
          "@Override\nprotected void onEntityAdded(IEntity entity) {\n  ${0}\n}",
          new Class<?>[] {IEntity.class},
          new ScriptHostType[] {ScriptHostType.ENVIRONMENT}
      ),
      new EventOverride(
          "onEntityRemoved",
          "protected void onEntityRemoved(IEntity entity)",
          "protected void onEntityRemoved(IEntity entity) - Environment Event Hook",
          "Called when an entity is removed or destroyed in the active environment.",
          "@Override\nprotected void onEntityRemoved(IEntity entity) {\n  ${0}\n}",
          new Class<?>[] {IEntity.class},
          new ScriptHostType[] {ScriptHostType.ENVIRONMENT}
      ),

      // Game Lifecycle
      new EventOverride(
          "onStarted",
          "public void onStarted()",
          "public void onStarted() - Game Lifecycle Entry Point",
          "Called after the game script enters the running game lifecycle.",
          "@Override\npublic void onStarted() {\n  ${0}\n}",
          new Class<?>[] {},
          new ScriptHostType[] {ScriptHostType.GAME}
      ),
      new EventOverride(
          "onStopped",
          "protected void onStopped()",
          "protected void onStopped() - Game Lifecycle Hook",
          "Called before the game script leaves the game lifecycle.",
          "@Override\nprotected void onStopped() {\n  ${0}\n}",
          new Class<?>[] {},
          new ScriptHostType[] {ScriptHostType.GAME}
      ),

      // Entity Combat & Physics
      new EventOverride(
          "onHit",
          "protected void onHit(EntityHitEvent event)",
          "protected void onHit(EntityHitEvent event) - Combat Event Hook",
          "Called when this combat entity receives damage from an attack or ability.",
          "@Override\nprotected void onHit(EntityHitEvent event) {\n  ${0}\n}",
          new Class<?>[] {EntityHitEvent.class},
          new ScriptHostType[] {ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onDeath",
          "protected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent)",
          "protected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent) - Combat Event Hook",
          "Called when this combat entity dies.",
          "@Override\nprotected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent) {\n  ${0}\n}",
          new Class<?>[] {ICombatEntity.class, EntityHitEvent.class},
          new ScriptHostType[] {ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onCollision",
          "protected void onCollision(CollisionEvent event)",
          "protected void onCollision(CollisionEvent event) - Physics Event Hook",
          "Called when this entity collides with another entity or static map obstacle.",
          "@Override\nprotected void onCollision(CollisionEvent event) {\n  ${0}\n}",
          new Class<?>[] {CollisionEvent.class},
          new ScriptHostType[] {ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onInteract",
          "protected void onInteract(IEntity source)",
          "protected void onInteract(IEntity source) - Interaction Event Hook",
          "Called when another entity interacts with this entity.",
          "@Override\nprotected void onInteract(IEntity source) {\n  ${0}\n}",
          new Class<?>[] {IEntity.class},
          new ScriptHostType[] {ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onAction",
          "protected void onAction(String action)",
          "protected void onAction(String action) - Custom Action Event Hook",
          "Called when a custom entity action is performed.",
          "@Override\nprotected void onAction(String action) {\n  ${0}\n}",
          new Class<?>[] {},
          new ScriptHostType[] {ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onMessage",
          "protected void onMessage(EntityMessageEvent event)",
          "protected void onMessage(EntityMessageEvent event) - Message Event Hook",
          "Called when a message is dispatched to this entity.",
          "@Override\nprotected void onMessage(EntityMessageEvent event) {\n  ${0}\n}",
          new Class<?>[] {EntityMessageEvent.class},
          new ScriptHostType[] {ScriptHostType.ENTITY}
      ),

      // Common Loop Hooks
      new EventOverride(
          "update",
          "public void update()",
          "public void update() - Tick Update Loop",
          "Called once every game tick (60 FPS default) for continuous script logic.",
          "@Override\npublic void update() {\n  ${0}\n}",
          new Class<?>[] {},
          new ScriptHostType[] {ScriptHostType.GAME, ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "render",
          "public void render(Graphics2D g)",
          "public void render(Graphics2D g) - World Render Pass Hook",
          "Called during the world render pass for custom drawing and visual effects.",
          "@Override\npublic void render(Graphics2D g) {\n  ${0}\n}",
          new Class<?>[] {Graphics2D.class},
          new ScriptHostType[] {ScriptHostType.GAME, ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),

      // Common Input Hooks
      new EventOverride(
          "onKeyPressed",
          "protected void onKeyPressed(KeyEvent event)",
          "protected void onKeyPressed(KeyEvent event) - Input Event Hook",
          "Called when a keyboard key is pressed.",
          "@Override\nprotected void onKeyPressed(KeyEvent event) {\n  ${0}\n}",
          new Class<?>[] {KeyEvent.class},
          new ScriptHostType[] {ScriptHostType.GAME, ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onKeyReleased",
          "protected void onKeyReleased(KeyEvent event)",
          "protected void onKeyReleased(KeyEvent event) - Input Event Hook",
          "Called when a keyboard key is released.",
          "@Override\nprotected void onKeyReleased(KeyEvent event) {\n  ${0}\n}",
          new Class<?>[] {KeyEvent.class},
          new ScriptHostType[] {ScriptHostType.GAME, ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onKeyTyped",
          "protected void onKeyTyped(KeyEvent event)",
          "protected void onKeyTyped(KeyEvent event) - Input Event Hook",
          "Called when a keyboard key is typed.",
          "@Override\nprotected void onKeyTyped(KeyEvent event) {\n  ${0}\n}",
          new Class<?>[] {KeyEvent.class},
          new ScriptHostType[] {ScriptHostType.GAME, ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onMouseClicked",
          "protected void onMouseClicked(MouseEvent event)",
          "protected void onMouseClicked(MouseEvent event) - Input Event Hook",
          "Called when a mouse button is clicked (pressed and released).",
          "@Override\nprotected void onMouseClicked(MouseEvent event) {\n  ${0}\n}",
          new Class<?>[] {MouseEvent.class},
          new ScriptHostType[] {ScriptHostType.GAME, ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onMousePressed",
          "protected void onMousePressed(MouseEvent event)",
          "protected void onMousePressed(MouseEvent event) - Input Event Hook",
          "Called when a mouse button is pressed.",
          "@Override\nprotected void onMousePressed(MouseEvent event) {\n  ${0}\n}",
          new Class<?>[] {MouseEvent.class},
          new ScriptHostType[] {ScriptHostType.GAME, ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onMouseReleased",
          "protected void onMouseReleased(MouseEvent event)",
          "protected void onMouseReleased(MouseEvent event) - Input Event Hook",
          "Called when a mouse button is released.",
          "@Override\nprotected void onMouseReleased(MouseEvent event) {\n  ${0}\n}",
          new Class<?>[] {MouseEvent.class},
          new ScriptHostType[] {ScriptHostType.GAME, ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onMouseMoved",
          "protected void onMouseMoved(MouseEvent event)",
          "protected void onMouseMoved(MouseEvent event) - Input Event Hook",
          "Called when the mouse cursor moves.",
          "@Override\nprotected void onMouseMoved(MouseEvent event) {\n  ${0}\n}",
          new Class<?>[] {MouseEvent.class},
          new ScriptHostType[] {ScriptHostType.GAME, ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      ),
      new EventOverride(
          "onMouseWheel",
          "protected void onMouseWheel(MouseWheelEvent event)",
          "protected void onMouseWheel(MouseWheelEvent event) - Input Event Hook",
          "Called when the mouse wheel is rotated.",
          "@Override\nprotected void onMouseWheel(MouseWheelEvent event) {\n  ${0}\n}",
          new Class<?>[] {MouseWheelEvent.class},
          new ScriptHostType[] {ScriptHostType.GAME, ScriptHostType.ENVIRONMENT, ScriptHostType.ENTITY}
      )
  );

  static void addAnnotationCompletions(
      List<Completion> result,
      String source,
      Set<String> importedFqns,
      int importInsertLine) {
    addAnnotationCompletions(result, source, importedFqns, importInsertLine, null);
  }

  static void addAnnotationCompletions(
      List<Completion> result,
      String source,
      Set<String> importedFqns,
      int importInsertLine,
      ScriptDefinition definition) {

    boolean hasWildcard = source.contains("import de.gurkenlabs.litiengine.scripting.*;");

    List<TextEdit> propEdits = (importedFqns.contains(ScriptProperty.class.getName()) || hasWildcard) ? List.of()
      : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
        "import " + ScriptProperty.class.getName() + ";\n"));

    result.add(new Completion("ScriptProperty", CompletionKind.PROPERTY, ScriptProperty.class.getName(),
      "Exports this field to the utiLITI inspector for live configuration and map persistence.",
      "ScriptProperty", ScriptProperty.class.getName(), List.of(), propEdits));

    result.add(new Completion("@ScriptProperty", CompletionKind.PROPERTY, ScriptProperty.class.getName(),
      "Exports this field to the utiLITI inspector for live configuration and map persistence.",
      "@ScriptProperty", ScriptProperty.class.getName(), List.of(), propEdits));

    result.add(new Completion("ScriptProperty(...)", CompletionKind.SNIPPET, "@ScriptProperty(name = \"...\", description = \"...\")",
      "Snippet for @ScriptProperty with configurable metadata attributes.",
      "ScriptProperty(name = \"${1:name}\", description = \"${2:description}\")", ScriptProperty.class.getName(), List.of(), propEdits));

    result.add(new Completion("@ScriptProperty(...)", CompletionKind.SNIPPET, "@ScriptProperty(name = \"...\", description = \"...\")",
      "Snippet for @ScriptProperty with configurable metadata attributes.",
      "@ScriptProperty(name = \"${1:name}\", description = \"${2:description}\")", ScriptProperty.class.getName(), List.of(), propEdits));

    result.add(new Completion("scriptproperty", CompletionKind.SNIPPET, "Property field template",
      "Generates an annotated @ScriptProperty field.",
      "@ScriptProperty\nprivate ${1:int} ${2:propertyName};\n", ScriptProperty.class.getName(), List.of(), propEdits));

    result.add(new Completion("prop", CompletionKind.SNIPPET, "Property field template",
      "Generates an annotated @ScriptProperty field.",
      "@ScriptProperty\nprivate ${1:int} ${2:propertyName};\n", ScriptProperty.class.getName(), List.of(), propEdits));

    List<TextEdit> infoEdits = (importedFqns.contains(ScriptInfo.class.getName()) || hasWildcard) ? List.of()
      : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
        "import " + ScriptInfo.class.getName() + ";\n"));

    result.add(new Completion("ScriptInfo", CompletionKind.CLASS, ScriptInfo.class.getName(),
      "Declares the script identifier, host type, and target entity class.",
      "ScriptInfo", ScriptInfo.class.getName(), List.of(), infoEdits));

    result.add(new Completion("@ScriptInfo", CompletionKind.CLASS, ScriptInfo.class.getName(),
      "Declares the script identifier, host type, and target entity class.",
      "@ScriptInfo", ScriptInfo.class.getName(), List.of(), infoEdits));

    result.add(new Completion("ScriptInfo(...)", CompletionKind.SNIPPET, "@ScriptInfo(id = \"...\", host = ...)",
      "Snippet for @ScriptInfo declaration.",
      "ScriptInfo(id = \"${1:id}\", host = ScriptHostType.${2|GAME,ENVIRONMENT,ENTITY|})", ScriptInfo.class.getName(), List.of(), infoEdits));

    result.add(new Completion("@ScriptInfo(...)", CompletionKind.SNIPPET, "@ScriptInfo(id = \"...\", host = ...)",
      "Snippet for @ScriptInfo declaration.",
      "@ScriptInfo(id = \"${1:id}\", host = ScriptHostType.${2|GAME,ENVIRONMENT,ENTITY|})", ScriptInfo.class.getName(), List.of(), infoEdits));

    result.add(new Completion("Override", CompletionKind.CLASS, "java.lang.Override",
      "Indicates that a method declaration is intended to override a method declaration in a supertype.",
      "Override", "java.lang.Override", List.of(), List.of()));

    result.add(new Completion("@Override", CompletionKind.CLASS, "java.lang.Override",
      "Indicates that a method declaration is intended to override a method declaration in a supertype.",
      "@Override", "java.lang.Override", List.of(), List.of()));

    result.add(new Completion("Deprecated", CompletionKind.CLASS, "java.lang.Deprecated",
      "Marks the annotated element as deprecated.",
      "Deprecated", "java.lang.Deprecated", List.of(), List.of()));

    result.add(new Completion("SuppressWarnings", CompletionKind.CLASS, "java.lang.SuppressWarnings",
      "Suppresses compiler warnings in the annotated element.",
      "SuppressWarnings(\"${1:all}\")", "java.lang.SuppressWarnings", List.of(), List.of()));

    // Add all event hook override templates
    ScriptHostType detectedHost = detectHostType(source, definition);
    for (EventOverride hook : EVENT_OVERRIDES) {
      if (isHostApplicable(hook, detectedHost)) {
        List<TextEdit> edits = createImportEdits(source, importedFqns, importInsertLine, hook.requiredImports());
        result.add(new Completion(
            "@Override " + hook.signature(),
            CompletionKind.SNIPPET,
            hook.detail(),
            hook.doc(),
            hook.template(),
            "void",
            List.of(),
            edits
        ));
      }
    }
  }

  static void addMethodOverrideCompletions(
      List<Completion> result,
      String source,
      Set<String> importedFqns,
      int importInsertLine,
      ScriptDefinition definition,
      String word) {
    addMethodOverrideCompletions(result, source, importedFqns, importInsertLine, definition, word, true);
  }

  static void addMethodOverrideCompletions(
      List<Completion> result,
      String source,
      Set<String> importedFqns,
      int importInsertLine,
      ScriptDefinition definition,
      String word,
      boolean includeOverrideAnnotation) {

    ScriptHostType detectedHost = detectHostType(source, definition);
    for (EventOverride hook : EVENT_OVERRIDES) {
      if (isHostApplicable(hook, detectedHost)) {
        String query = word.toLowerCase(java.util.Locale.ROOT);
        if (query.isEmpty() || hook.methodName().toLowerCase(java.util.Locale.ROOT).startsWith(query) || hook.signature().toLowerCase(java.util.Locale.ROOT).contains(query)) {
          List<TextEdit> edits = createImportEdits(source, importedFqns, importInsertLine, hook.requiredImports());
          String template = includeOverrideAnnotation ? hook.template() : (hook.signature() + " {\n  ${0}\n}");
          result.add(new Completion(
              hook.signature() + (includeOverrideAnnotation ? " [Override]" : ""),
              CompletionKind.SNIPPET,
              hook.detail(),
              hook.doc(),
              template,
              "void",
              List.of(),
              edits
          ));
        }
      }
    }
  }

  private static ScriptHostType detectHostType(String source, ScriptDefinition definition) {
    if (definition != null && definition.getHost() != null) {
      return definition.getHost();
    }
    if (source.contains("extends GameScript") || source.contains("ScriptHostType.GAME")) {
      return ScriptHostType.GAME;
    }
    if (source.contains("extends EnvironmentScript") || source.contains("ScriptHostType.ENVIRONMENT")) {
      return ScriptHostType.ENVIRONMENT;
    }
    if (source.contains("extends CreatureScript") || source.contains("extends EntityScript") || source.contains("ScriptHostType.ENTITY")) {
      return ScriptHostType.ENTITY;
    }
    return null;
  }

  private static boolean isHostApplicable(EventOverride hook, ScriptHostType host) {
    if (host == null) return true;
    for (ScriptHostType h : hook.hosts()) {
      if (h == host) return true;
    }
    return false;
  }

  private static List<TextEdit> createImportEdits(String source, Set<String> importedFqns, int importInsertLine, Class<?>... classes) {
    List<TextEdit> edits = new ArrayList<>();
    for (Class<?> cls : classes) {
      if (cls == null) continue;
      String name = cls.getName();
      String pkg = cls.getPackageName();
      boolean wildcardImported = source.contains("import " + pkg + ".*;");
      if (!importedFqns.contains(name) && !wildcardImported && !name.startsWith("java.lang.")) {
        edits.add(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
            "import " + name + ";\n"));
      }
    }
    return edits;
  }

  static void addAnnotationAttributeCompletions(
      List<Completion> result,
      String annotName,
      String paramsContent) {

    if ("ScriptProperty".equals(annotName)) {
      addAttributeIfNotPresent(result, paramsContent, "name", "name = \"${1:Name}\"", "Display label in inspector");
      addAttributeIfNotPresent(result, paramsContent, "description", "description = \"${1:Description}\"", "Tooltip description in inspector");
      addAttributeIfNotPresent(result, paramsContent, "category", "category = \"${1:General}\"", "Grouping category in inspector");
      addAttributeIfNotPresent(result, paramsContent, "min", "min = ${1:0.0}", "Minimum numeric value");
      addAttributeIfNotPresent(result, paramsContent, "max", "max = ${1:100.0}", "Maximum numeric value");
      addAttributeIfNotPresent(result, paramsContent, "unit", "unit = \"${1:px}\"", "Unit label (e.g. px, s, %)");
      addAttributeIfNotPresent(result, paramsContent, "required", "required = ${1|true,false|}", "Whether this property is required");
      addAttributeIfNotPresent(result, paramsContent, "defaultValue", "defaultValue = \"${1:value}\"", "Default fallback value");
    } else if ("ScriptInfo".equals(annotName)) {
      addAttributeIfNotPresent(result, paramsContent, "id", "id = \"${1:script-id}\"", "Unique script identifier");
      addAttributeIfNotPresent(result, paramsContent, "name", "name = \"${1:Script Name}\"", "Human-readable script name");
      addAttributeIfNotPresent(result, paramsContent, "host", "host = ScriptHostType.${1|GAME,ENVIRONMENT,ENTITY|}", "Target script host type");
      addAttributeIfNotPresent(result, paramsContent, "target", "target = ${1:Creature}.class", "Target entity class for entity scripts");
    }
  }

  private static void addAttributeIfNotPresent(List<Completion> result, String paramsContent, String attrName, String snippet, String doc) {
    if (!paramsContent.contains(attrName + " =") && !paramsContent.contains(attrName + "=")) {
      String attrDoc = ScriptDocumentation.getAttributeDoc(attrName);
      String fullDoc = attrDoc.isBlank() ? doc : doc + "\n\n" + attrDoc;
      result.add(new Completion(attrName, CompletionKind.PROPERTY, attrName + " = ...", fullDoc, snippet, "attribute", List.of(), List.of()));
    }
  }
}
