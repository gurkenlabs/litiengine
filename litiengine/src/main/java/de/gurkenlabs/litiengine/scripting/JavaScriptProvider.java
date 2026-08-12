package de.gurkenlabs.litiengine.scripting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import javax.tools.SimpleJavaFileObject;

import javax.tools.ToolProvider;

/** Compiles Java source files at runtime using the system Java compiler or loads precompiled script classes. */
public class JavaScriptProvider implements ScriptProvider {
  @Override
  public String language() {
    return "java";
  }

  @Override
  public Optional<ScriptLanguageService> createLanguageService(ScriptLanguageService.Workspace workspace) {
    return Optional.of(new JavaLanguageService(workspace));
  }

  @Override
  public CompiledScript compile(ScriptDefinition definition, URL source, ClassLoader parent) throws ScriptException {
    return this.compile(definition, source, new ScriptCompilationContext(parent, List.of(), 0));
  }

  @Override
  public CompiledScript compile(ScriptDefinition definition, URL source, ScriptCompilationContext context)
      throws ScriptException {
    Path sourcePath = resolveSourcePath(definition, source);
    if (sourcePath != null && Files.isRegularFile(sourcePath)) {
      try {
        String code = Files.readString(sourcePath, StandardCharsets.UTF_8);
        return compileSource(definition, sourcePath, code, context);
      } catch (IOException e) {
        throw new ScriptException("Could not read Java source file: " + e.getMessage(), e);
      }
    }

    try {
      Class<?> type = Class.forName(definition.getImplementation(), false, context.parent());
      Class<? extends ScriptInstance> scriptType = type.asSubclass(ScriptInstance.class);
      scriptType.getConstructor();
      return new CompiledScript() {
        @Override
        public ScriptInstance create() throws ScriptException {
          try {
            return scriptType.getConstructor().newInstance();
          } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ScriptException("Could not instantiate script " + definition.getId() + ".", e);
          }
        }

        @Override
        public Class<? extends ScriptInstance> implementationType() {
          return scriptType;
        }
      };
    } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException e) {
      throw new ScriptException("Could not resolve script implementation " + definition.getImplementation() + ".", e);
    }
  }

  private static Path resolveSourcePath(ScriptDefinition definition, URL source) {
    if (source != null) {
      try {
        return Path.of(source.toURI());
      } catch (Exception ignored) {
      }
    }
    if (definition != null && definition.getSource() != null) {
      try {
        Path p = Path.of(definition.getSource());
        if (Files.exists(p)) return p;
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  static CompiledScript compileSource(ScriptDefinition definition, Path path, String sourceCode, ClassLoader parent) throws ScriptException {
    return compileSource(definition, path, sourceCode, new ScriptCompilationContext(parent, List.of(), 0));
  }

  static CompiledScript compileSource(
      ScriptDefinition definition, Path path, String sourceCode, ScriptCompilationContext context)
      throws ScriptException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new ScriptException("No system Java compiler available. Ensure running on JDK.", List.of());
    }

    String className = definition.getImplementation();
    if (className == null || className.isBlank()) {
      className = extractClassName(sourceCode);
    }

    String filename = path == null ? className + ".java" : path.getFileName().toString();
    JavaSourceFileObject sourceFile = new JavaSourceFileObject(filename, sourceCode);

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    MemoryFileManager fileManager = new MemoryFileManager(compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8));

    List<String> options = new ArrayList<>();
    options.add("-g");
    List<String> classpathEntries = new ArrayList<>();
    String processClasspath = System.getProperty("java.class.path");
    if (processClasspath != null && !processClasspath.isBlank()) {
      classpathEntries.addAll(List.of(processClasspath.split(java.util.regex.Pattern.quote(File.pathSeparator))));
    }
    context.classpath().stream().map(Path::toString).forEach(classpathEntries::add);
    if (!classpathEntries.isEmpty()) {
      options.add("-classpath");
      options.add(String.join(File.pathSeparator, classpathEntries.stream().distinct().toList()));
    }
    if (context.javaVersion() > 0) {
      options.add("--release");
      options.add(Integer.toString(context.javaVersion()));
    }

    JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, List.of(sourceFile));
    boolean success = task.call();

    if (!success || !diagnostics.getDiagnostics().isEmpty()) {
      List<ScriptDiagnostic> errorList = new ArrayList<>();
      for (Diagnostic<? extends JavaFileObject> diag : diagnostics.getDiagnostics()) {
        if (diag.getKind() == Diagnostic.Kind.ERROR) {
          errorList.add(new ScriptDiagnostic(
            ScriptDiagnostic.Severity.ERROR,
            definition.getId(),
            path == null ? null : path.toUri().toString(),
            (int) diag.getLineNumber(),
            (int) diag.getColumnNumber(),
            diag.getMessage(null)
          ));
        }
      }
      if (!errorList.isEmpty()) {
        throw new ScriptException("Java compilation failed for " + definition.getId() + ".", errorList);
      }
    }

    MemoryClassLoader classLoader = new MemoryClassLoader(fileManager.getByteCodeMap(), context.parent());
    try {
      Class<?> loadedClass = classLoader.loadClass(className);
      Class<? extends ScriptInstance> scriptType = loadedClass.asSubclass(ScriptInstance.class);
      scriptType.getConstructor();

      return new CompiledScript() {
        @Override
        public ScriptInstance create() throws ScriptException {
          try {
            return scriptType.getConstructor().newInstance();
          } catch (Exception e) {
            throw new ScriptException("Could not instantiate Java script " + definition.getId() + ".", e);
          }
        }

        @Override
        public Class<? extends ScriptInstance> implementationType() {
          return scriptType;
        }
      };
    } catch (Exception e) {
      throw new ScriptException("Could not load compiled Java script class " + className + ".", e);
    }
  }

  private static String extractClassName(String source) {
    var matcher = java.util.regex.Pattern.compile("(?m)^\\s*(?:public\\s+)?class\\s+([A-Za-z_$][\\w$]*)").matcher(source);
    return matcher.find() ? matcher.group(1) : "Script";
  }

  private static class JavaSourceFileObject extends SimpleJavaFileObject {
    private final String code;

    JavaSourceFileObject(String name, String code) {
      super(URI.create("string:///" + name.replace('\\', '/')), Kind.SOURCE);
      this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return this.code;
    }
  }

  private static class MemoryByteCode extends SimpleJavaFileObject {
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    MemoryByteCode(String name) {
      super(URI.create("byte:///" + name.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() {
      return this.baos;
    }

    byte[] getBytes() {
      return this.baos.toByteArray();
    }
  }

  private static class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Map<String, MemoryByteCode> byteCodeMap = new HashMap<>();

    MemoryFileManager(JavaFileManager fileManager) {
      super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
      MemoryByteCode byteCode = new MemoryByteCode(className);
      this.byteCodeMap.put(className, byteCode);
      return byteCode;
    }

    Map<String, MemoryByteCode> getByteCodeMap() {
      return this.byteCodeMap;
    }
  }

  private static class MemoryClassLoader extends ClassLoader {
    private final Map<String, MemoryByteCode> byteCodeMap;

    MemoryClassLoader(Map<String, MemoryByteCode> byteCodeMap, ClassLoader parent) {
      super(parent);
      this.byteCodeMap = byteCodeMap;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      MemoryByteCode byteCode = this.byteCodeMap.get(name);
      if (byteCode != null) {
        byte[] bytes = byteCode.getBytes();
        return defineClass(name, bytes, 0, bytes.length);
      }
      return super.findClass(name);
    }
  }
}
