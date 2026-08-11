package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.EntityListener;
import de.gurkenlabs.litiengine.entities.EntityMessageListener;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.EnvironmentListener;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ReflectionUtilities;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Coordinates script providers, definitions, bindings, lifecycles, and explicit reloads. */
public final class ScriptManager implements IUpdateable {
  public static final String BINDINGS_PROPERTY = "scriptBindings";
  private static final Logger log = Logger.getLogger(ScriptManager.class.getName());

  private final Map<String, ScriptProvider> providers = new ConcurrentHashMap<>();
  private final Map<String, ScriptDefinition> definitions = new ConcurrentHashMap<>();
  private final Map<String, CompiledScript> compiled = new ConcurrentHashMap<>();
  private final List<Attachment> attachments = new CopyOnWriteArrayList<>();
  private final List<ScriptBinding> gameBindings = new CopyOnWriteArrayList<>();
  private final List<EntityScriptBinding> entityBindings = new CopyOnWriteArrayList<>();
  private final List<ScriptDiagnostic> diagnostics = new CopyOnWriteArrayList<>();
  private final ScriptGlobals globals = new ScriptGlobals();
  private final Object gameHost = new Object();
  private Path projectRoot;
  private ClassLoader projectClassLoader;
  private List<Path> projectClasspath = List.of();
  private int projectJavaVersion = Runtime.version().feature();
  private boolean attachedToLoop;

  public ScriptManager() {
    this.registerProvider(new ClasspathScriptProvider());
    this.registerProvider(new JavaScriptProvider());
    ServiceLoader.load(ScriptProvider.class).forEach(this::registerProvider);
    Game.addGameListener(new GameListener() {
      @Override public void started() { attachAll(gameHost, gameBindings); }
      @Override public boolean terminating() { detachAll(); return true; }
    });
    Game.world().onLoaded(this::environmentLoaded);
    Game.world().onUnloaded(this::detach);
  }

  public ScriptGlobals globals() {
    return this.globals;
  }

  public void registerProvider(ScriptProvider provider) {
    Objects.requireNonNull(provider);
    this.providers.put(provider.language().toLowerCase(), provider);
  }

  public Collection<ScriptProvider> getProviders() {
    return List.copyOf(this.providers.values());
  }

  /** Creates non-executing semantic tooling backed by the registered provider for a language. */
  public Optional<ScriptLanguageService> createLanguageService(String language) {
    if (language == null || language.isBlank()) return Optional.empty();
    ScriptProvider provider = this.providers.get(language.toLowerCase());
    if (provider == null) return Optional.empty();
    ClassLoader loader = this.projectClassLoader != null
      ? this.projectClassLoader : Thread.currentThread().getContextClassLoader();
    if (loader == null) loader = ScriptManager.class.getClassLoader();
    return provider.createLanguageService(new ScriptLanguageService.Workspace(this.projectRoot, loader, Map.of()));
  }

  public void setDefinitions(Collection<ScriptDefinition> definitions) {
    Map<String, ScriptDefinition> replacements = new LinkedHashMap<>();
    if (definitions != null) for (ScriptDefinition definition : definitions) {
      if (definition == null || !definition.validate().isEmpty()) {
        this.report(definition == null ? null : definition.getId(), definition == null ? null : definition.getSource(),
          "Invalid script definition: " + (definition == null ? "null" : String.join(" ", definition.validate())), null);
        continue;
      }
      ScriptDefinition previous = replacements.putIfAbsent(definition.getId(), new ScriptDefinition(definition));
      if (previous != null) this.report(definition.getId(), definition.getSource(), "Duplicate script id.", null);
    }

    this.definitions.forEach((id, previous) -> {
      if (!previous.hasSameConfiguration(replacements.get(id))) close(this.compiled.remove(id));
    });
    this.definitions.clear();
    this.definitions.putAll(replacements);
  }

  public Collection<ScriptDefinition> getDefinitions() {
    return this.definitions.values().stream().sorted(Comparator.comparing(ScriptDefinition::getId)).toList();
  }

  public ScriptDefinition getDefinition(String id) {
    return this.definitions.get(id);
  }

  /** Returns configurable fields from the currently compiled generation without compiling or executing new code. */
  public List<ScriptPropertyMetadata> getPropertyMetadata(String scriptId) {
    CompiledScript compiledScript = this.compiled.get(scriptId);
    if (compiledScript == null) return List.of();
    List<ScriptPropertyMetadata> properties = new ArrayList<>();
    for (Field field : ReflectionUtilities.getAllFields(new ArrayList<>(), compiledScript.implementationType())) {
      ScriptProperty property = field.getAnnotation(ScriptProperty.class);
      if (property == null) continue;
      properties.add(new ScriptPropertyMetadata(field.getName(), property.name().isBlank() ? field.getName() : property.name(),
        property.description(), property.category(), property.type().isBlank() ? field.getType().getName() : property.type(),
        property.defaultValue(), property.min(), property.max(), property.unit(), property.required()));
    }
    return properties.stream().sorted(Comparator.comparing(ScriptPropertyMetadata::category)
      .thenComparing(ScriptPropertyMetadata::displayName)).toList();
  }

  public void setGameBindings(Collection<ScriptBinding> bindings) {
    this.gameBindings.clear();
    if (bindings != null) this.gameBindings.addAll(bindings);
    if (Game.hasStarted()) {
      this.detach(this.gameHost);
      this.attachAll(this.gameHost, this.gameBindings);
    }
  }

  /** Replaces reusable bindings that are automatically applied when matching entities are loaded. */
  public void setEntityBindings(Collection<EntityScriptBinding> bindings) {
    this.entityBindings.clear();
    if (bindings != null) bindings.stream().filter(Objects::nonNull)
      .map(EntityScriptBinding::new).forEach(this.entityBindings::add);
  }

  public List<EntityScriptBinding> getEntityBindings() {
    return this.entityBindings.stream().map(EntityScriptBinding::new).toList();
  }

  /** Adds or refreshes the script controller that owns the default bindings for an entity type. */
  public void configure(IEntity entity) {
    Objects.requireNonNull(entity);
    List<ScriptBinding> defaults = this.resolveEntityBindings(entity);
    EntityScriptController<?> controller = entity.scripts();
    if (controller == null) {
      if (defaults.isEmpty()) return;
      EntityScriptController<IEntity> created = new EntityScriptController<>(entity, List.of());
      created.setDefaultBindings(defaults);
      entity.addController(created);
      return;
    }
    controller.setDefaultBindings(defaults);
  }

  /** Sets the directory against which relative development-time source paths are resolved. */
  public void setProjectRoot(Path projectRoot) {
    this.projectRoot = projectRoot == null ? null : projectRoot.toAbsolutePath().normalize();
  }

  public Path getProjectRoot() {
    return this.projectRoot;
  }

  /** Sets the compiled project class loader used as the parent of runtime-compiled scripts. */
  public void setProjectClassLoader(ClassLoader projectClassLoader) {
    this.projectClassLoader = projectClassLoader;
  }

  /** Sets build-resolved locations used by development-time source compilation. */
  public void setProjectClasspath(Collection<Path> projectClasspath) {
    this.projectClasspath = projectClasspath == null
      ? List.of()
      : projectClasspath.stream().filter(Objects::nonNull).map(Path::toAbsolutePath).map(Path::normalize).distinct().toList();
  }

  /** Sets the Java language level used for development-time source compilation. */
  public void setProjectJavaVersion(int projectJavaVersion) {
    if (projectJavaVersion <= 0) throw new IllegalArgumentException("Project Java version must be positive.");
    this.projectJavaVersion = projectJavaVersion;
  }

  public List<ScriptDiagnostic> getDiagnostics() {
    return List.copyOf(this.diagnostics);
  }

  public void clearDiagnostics() {
    this.diagnostics.clear();
  }

  public List<ScriptInstance> attachAll(Object host, Collection<ScriptBinding> bindings) {
    return this.attachAll(host, bindings, false);
  }

  List<ScriptInstance> attachAll(Object host, Collection<ScriptBinding> bindings, boolean controllerManaged) {
    if (bindings == null) return List.of();
    List<ScriptInstance> instances = new ArrayList<>();
    bindings.stream().filter(ScriptBinding::isEnabled).sorted(Comparator.comparingInt(ScriptBinding::getOrder)).forEach(binding -> {
      ScriptInstance instance = this.attach(host, binding, controllerManaged);
      if (instance != null) instances.add(instance);
    });
    return List.copyOf(instances);
  }

  public ScriptInstance attach(Object host, ScriptBinding binding) {
    return this.attach(host, binding, false);
  }

  private ScriptInstance attach(Object host, ScriptBinding binding, boolean controllerManaged) {
    Objects.requireNonNull(host);
    Objects.requireNonNull(binding);
    ScriptDefinition definition = this.definitions.get(binding.getScript());
    if (definition == null) {
      this.report(binding.getScript(), null, "No script definition is registered for this binding.", null);
      return null;
    }
    if (!this.isCompatible(definition, host)) {
      this.report(definition.getId(), definition.getSource(), "Script is incompatible with host " + host.getClass().getName() + ".", null);
      return null;
    }
    ScriptContext<Object> context = null;
    try {
      CompiledScript compiledScript = this.compiled.computeIfAbsent(definition.getId(), id -> {
        try {
          return this.compile(definition);
        } catch (ScriptException e) {
          throw new CompilationFailure(e);
        }
      });
      ScriptInstance instance = compiledScript.create();
      this.applyParameters(instance, binding);
      context = new ScriptContext<>(definition, binding, host);
      instance.attach(context);
      Attachment attachment = new Attachment(host, definition, binding, instance, context, controllerManaged);
      this.attachments.add(attachment);
      if (!controllerManaged) this.ensureUpdateAttached();
      if (host instanceof IEntity entity) this.registerEntityLifecycle(attachment, entity);
      if (host instanceof Environment environment) this.registerEnvironmentLifecycle(attachment, environment);
      return instance;
    } catch (CompilationFailure e) {
      this.record(definition, e.scriptException);
    } catch (Exception | LinkageError e) {
      if (context != null) context.close();
      this.report(definition.getId(), definition.getSource(), "Could not attach script: " + e.getMessage(), e);
    }
    return null;
  }

  public boolean reload(String scriptId) {
    ScriptDefinition definition = this.definitions.get(scriptId);
    if (definition == null) return false;
    final CompiledScript replacement;
    try {
      replacement = this.compile(definition);
    } catch (ScriptException e) {
      this.record(definition, e);
      return false;
    }

    List<Attachment> affected = this.attachments.stream().filter(a -> a.definition.getId().equals(scriptId)).toList();
    List<HostBinding> bindings = affected.stream().map(a -> new HostBinding(a.host, a.binding, a.controllerManaged)).toList();
    affected.forEach(this::detachAttachment);
    CompiledScript previous = this.compiled.put(scriptId, replacement);
    close(previous);
    bindings.forEach(binding -> this.attach(binding.host, binding.binding, binding.controllerManaged));
    return true;
  }

  public void detach(Object host) {
    this.attachments.stream().filter(attachment -> attachment.host == host).toList().forEach(this::detachAttachment);
  }

  void detach(Object host, boolean controllerManaged) {
    this.attachments.stream()
      .filter(attachment -> attachment.host == host && attachment.controllerManaged == controllerManaged)
      .toList().forEach(this::detachAttachment);
  }

  public void detachAll() {
    List.copyOf(this.attachments).forEach(this::detachAttachment);
    this.compiled.values().forEach(ScriptManager::close);
    this.compiled.clear();
    this.detachUpdateLoop();
  }

  @Override
  public void update() {
    this.updateAttachments(null, false);
  }

  void update(Object host) {
    this.updateAttachments(host, true);
  }

  private void updateAttachments(Object host, boolean controllerManaged) {
    for (Attachment attachment : this.attachments) {
      if (attachment.controllerManaged != controllerManaged || host != null && attachment.host != host) continue;
      if (attachment.faulted) continue;
      try {
        attachment.instance.update();
      } catch (Exception | LinkageError e) {
        attachment.faulted = true;
        this.report(attachment.definition.getId(), attachment.definition.getSource(), "Script update failed: " + e.getMessage(), e);
        this.detachAttachment(attachment);
      }
    }
  }

  private void environmentLoaded(Environment environment) {
    if (environment.getMap() instanceof ICustomPropertyProvider properties) {
      String encoded = properties.getStringValue(BINDINGS_PROPERTY, null);
      try {
        this.attachAll(environment, ScriptBindingCodec.decode(encoded));
      } catch (IllegalArgumentException e) {
        this.report(null, null, "Could not decode environment script bindings.", e);
      }
    }
  }

  private CompiledScript compile(ScriptDefinition definition) throws ScriptException {
    ScriptProvider provider = this.providers.get(definition.getLanguage().toLowerCase());
    if (provider == null) throw new ScriptException("No provider is registered for language " + definition.getLanguage() + ".");
    URL source = this.resolveSource(definition.getSource());
    if (!"java".equalsIgnoreCase(definition.getLanguage()) && source == null) {
      throw new ScriptException("Could not resolve script source " + definition.getSource() + ".");
    }
    ClassLoader parent = this.projectClassLoader != null ? this.projectClassLoader : Thread.currentThread().getContextClassLoader();
    if (parent == null) parent = ScriptManager.class.getClassLoader();
    return provider.compile(definition, source,
      new ScriptCompilationContext(parent, this.projectClasspath, this.projectJavaVersion));
  }

  private URL resolveSource(String configuredSource) throws ScriptException {
    if (configuredSource == null || configuredSource.isBlank()) return null;
    if (this.projectRoot != null) {
      Path configuredPath = Path.of(configuredSource);
      Path candidate = (configuredPath.isAbsolute() ? configuredPath : this.projectRoot.resolve(configuredPath)).toAbsolutePath().normalize();
      if (!candidate.startsWith(this.projectRoot)) throw new ScriptException("Script source escapes the configured project directory.");
      if (Files.isRegularFile(candidate)) {
        try {
          return candidate.toUri().toURL();
        } catch (java.net.MalformedURLException e) {
          throw new ScriptException("Could not resolve script source " + configuredSource + ".", e);
        }
      }
    }
    return Resources.getLocation(configuredSource);
  }

  private boolean isCompatible(ScriptDefinition definition, Object host) {
    if (definition.getHost() == ScriptHostType.GAME && host != this.gameHost) return false;
    if (definition.getHost() == ScriptHostType.ENVIRONMENT && !(host instanceof Environment)) return false;
    if (definition.getHost() == ScriptHostType.ENTITY && !(host instanceof IEntity)) return false;
    if (definition.getTargetType() == null || definition.getTargetType().isBlank()) return true;
    try {
      return Class.forName(definition.getTargetType(), false, host.getClass().getClassLoader()).isInstance(host);
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private List<ScriptBinding> resolveEntityBindings(IEntity entity) {
    List<ResolvedEntityBinding> matching = new ArrayList<>();
    for (int index = 0; index < this.entityBindings.size(); index++) {
      EntityScriptBinding binding = this.entityBindings.get(index);
      if (binding.getTargetType() == null || binding.getTargetType().isBlank()) continue;
      try {
        Class<?> target = Class.forName(binding.getTargetType(), false, entity.getClass().getClassLoader());
        int distance = typeDistance(entity.getClass(), target);
        if (distance < 0 || !binding.isInherited() && distance != 0) continue;
        matching.add(new ResolvedEntityBinding(binding, distance, index));
      } catch (ClassNotFoundException | LinkageError e) {
        this.report(null, null, "Could not resolve entity script target " + binding.getTargetType() + ".", e);
      }
    }

    matching.sort(Comparator.comparingInt(ResolvedEntityBinding::distance).reversed()
      .thenComparingInt(ResolvedEntityBinding::index));
    Map<String, ScriptBinding> merged = new LinkedHashMap<>();
    for (ResolvedEntityBinding resolved : matching) {
      for (ScriptBinding binding : resolved.binding().getScripts()) {
        if (binding == null || binding.getScript() == null) continue;
        merged.remove(binding.getScript());
        merged.put(binding.getScript(), new ScriptBinding(binding));
      }
    }
    return merged.values().stream().sorted(Comparator.comparingInt(ScriptBinding::getOrder)).toList();
  }

  private static int typeDistance(Class<?> concrete, Class<?> target) {
    if (!target.isAssignableFrom(concrete)) return -1;
    List<Class<?>> current = List.of(concrete);
    Set<Class<?>> visited = new HashSet<>();
    for (int distance = 0; !current.isEmpty(); distance++) {
      if (current.contains(target)) return distance;
      List<Class<?>> next = new ArrayList<>();
      for (Class<?> type : current) {
        if (!visited.add(type)) continue;
        Class<?> parent = type.getSuperclass();
        if (parent != null) next.add(parent);
        next.addAll(List.of(type.getInterfaces()));
      }
      current = next;
    }
    return -1;
  }

  private void applyParameters(ScriptInstance instance, ScriptBinding binding) throws ScriptException {
    Map<String, String> parameters = binding.getParameters();
    for (Field field : ReflectionUtilities.getAllFields(new ArrayList<>(), instance.getClass())) {
      ScriptProperty property = field.getAnnotation(ScriptProperty.class);
      if (property == null || Modifier.isFinal(field.getModifiers())) continue;
      String value = parameters.get(field.getName());
      if (value == null) value = property.defaultValue().isEmpty() ? null : property.defaultValue();
      if (value == null) {
        if (property.required()) throw new ScriptException("Required script parameter " + field.getName() + " is missing.");
        continue;
      }
      if (!ReflectionUtilities.setFieldValue(field.getDeclaringClass(), instance, field.getName(), value)) {
        throw new ScriptException("Could not set script parameter " + field.getName() + ".");
      }
    }
  }

  private void registerEntityLifecycle(Attachment attachment, IEntity entity) {
    EntityMessageListener messageListener = event -> {
      if (attachment.instance instanceof EntityScript<?> script) {
        try {
          script.dispatchMessage(event);
        } catch (Exception e) {
          this.report(attachment.definition.getId(), attachment.definition.getSource(), "Script message handler failed: " + e.getMessage(), e);
          this.detachAttachment(attachment);
        }
      }
    };
    EntityListener entityListener = new EntityListener() {
      @Override public void removed(IEntity removed, Environment environment) { detachAttachment(attachment); }
    };
    entity.onMessage(messageListener);
    entity.addListener(entityListener);
    attachment.context.manage(() -> entity.removeListener(messageListener));
    attachment.context.manage(() -> entity.removeListener(entityListener));
  }

  private void registerEnvironmentLifecycle(Attachment attachment, Environment environment) {
    if (!(attachment.instance instanceof EnvironmentScript script)) return;
    EnvironmentListener listener = new EnvironmentListener() {
      @Override public void cleared(Environment cleared) {
        try {
          script.dispatchCleared();
        } catch (Exception e) {
          report(attachment.definition.getId(), attachment.definition.getSource(),
            "Script environment-clear handler failed: " + e.getMessage(), e);
          detachAttachment(attachment);
        }
      }
    };
    environment.addListener(listener);
    attachment.context.manage(() -> environment.removeListener(listener));
  }

  private void detachAttachment(Attachment attachment) {
    if (!this.attachments.remove(attachment)) return;
    try {
      attachment.instance.detach();
    } catch (Exception e) {
      this.report(attachment.definition.getId(), attachment.definition.getSource(), "Could not detach script: " + e.getMessage(), e);
    } finally {
      attachment.context.close();
      if (this.attachments.stream().noneMatch(candidate -> !candidate.controllerManaged)) this.detachUpdateLoop();
    }
  }

  private void ensureUpdateAttached() {
    if (this.attachedToLoop || Game.loop() == null) return;
    Game.loop().attach(this);
    this.attachedToLoop = true;
  }

  private void detachUpdateLoop() {
    if (!this.attachedToLoop || Game.loop() == null) return;
    Game.loop().detach(this);
    this.attachedToLoop = false;
  }

  private void record(ScriptDefinition definition, ScriptException exception) {
    if (!exception.getDiagnostics().isEmpty()) this.diagnostics.addAll(exception.getDiagnostics());
    else this.report(definition.getId(), definition.getSource(), exception.getMessage(), exception);
  }

  private void report(String scriptId, String source, String message, Throwable cause) {
    this.diagnostics.add(new ScriptDiagnostic(ScriptDiagnostic.Severity.ERROR, scriptId, source, -1, -1, message));
    if (cause == null) log.warning(message);
    else log.log(Level.WARNING, message, cause);
  }

  private static void close(CompiledScript compiledScript) {
    if (compiledScript == null) return;
    try {
      compiledScript.close();
    } catch (Exception e) {
      log.log(Level.FINE, "Could not close compiled script generation.", e);
    }
  }

  private record HostBinding(Object host, ScriptBinding binding, boolean controllerManaged) {}

  private record ResolvedEntityBinding(EntityScriptBinding binding, int distance, int index) {}

  private static final class Attachment {
    private final Object host;
    private final ScriptDefinition definition;
    private final ScriptBinding binding;
    private final ScriptInstance instance;
    private final ScriptContext<?> context;
    private final boolean controllerManaged;
    private boolean faulted;

    private Attachment(Object host, ScriptDefinition definition, ScriptBinding binding, ScriptInstance instance, ScriptContext<?> context,
      boolean controllerManaged) {
      this.host = host;
      this.definition = definition;
      this.binding = binding;
      this.instance = instance;
      this.context = context;
      this.controllerManaged = controllerManaged;
    }
  }

  private static final class CompilationFailure extends RuntimeException {
    private final transient ScriptException scriptException;
    private CompilationFailure(ScriptException exception) { super(exception); this.scriptException = exception; }
  }
}
