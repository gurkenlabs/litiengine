package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Completion;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.CompletionKind;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Position;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Range;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.TextEdit;
import java.util.List;
import java.util.Set;

/** Provides completions and snippet templates for script annotations (@ScriptProperty, @ScriptInfo). */
final class ScriptAnnotationProvider {
  private ScriptAnnotationProvider() {}

  static void addAnnotationCompletions(
      List<Completion> result,
      String source,
      Set<String> importedFqns,
      int importInsertLine) {

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
