package de.gurkenlabs.utiliti.controller.debug;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/** JDI debugger for Java-compatible runtime scripts executed in the external project JVM. */
public final class JdiScriptDebuggerBackend implements ScriptDebuggerBackend {
  private static final int ATTACH_TIMEOUT_MILLIS = 120_000;
  private static final int MAX_FRAMES = 40;
  private static final int MAX_FIELDS = 50;
  private static final int MAX_CHILDREN = 100;
  private static final int MAX_VALUE_LENGTH = 512;
  private static final java.util.Set<String> BOXED_TYPES = java.util.Set.of(
      "java.lang.Boolean", "java.lang.Byte", "java.lang.Character", "java.lang.Double",
      "java.lang.Float", "java.lang.Integer", "java.lang.Long", "java.lang.Short");
  private final Listener listener;
  private final List<ScriptBreakpoint> breakpoints = new CopyOnWriteArrayList<>();
  private final List<ScriptDefinition> definitions = new CopyOnWriteArrayList<>();
  private final AtomicReference<State> state = new AtomicReference<>(State.DISCONNECTED);
  private final Map<String, ObjectReference> objectReferences = new ConcurrentHashMap<>();
  private volatile VirtualMachine vm;
  private volatile EventSet pausedEvents;
  private volatile ThreadReference pausedThread;
  private volatile boolean closed;

  public JdiScriptDebuggerBackend(Listener listener) {
    this.listener = listener == null ? new Listener() {} : listener;
  }

  @Override
  public void attach(String host, int port, Collection<ScriptDefinition> definitions) throws IOException {
    if (this.vm != null) throw new IOException("A debugger is already attached.");
    this.closed = false;
    this.definitions.clear();
    if (definitions != null) definitions.stream().filter(Objects::nonNull)
        .map(ScriptDefinition::new).forEach(this.definitions::add);
    this.setState(State.ATTACHING, "Attaching debugger...");

    IOException lastError = null;
    long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(ATTACH_TIMEOUT_MILLIS);
    do {
      try {
        VirtualMachine attached = attachSocket(host, port);
        if (this.closed) {
          attached.dispose();
          throw new IOException("Debugger attachment was cancelled.");
        }
        this.vm = attached;
        this.configureRequests();
        this.setState(State.RUNNING, "Debugger attached on port " + port);
        Thread.ofPlatform().daemon().name("utiliti-script-debug-events").start(this::eventLoop);
        this.vm.resume();
        return;
      } catch (IOException error) {
        lastError = error;
        try {
          Thread.sleep(100);
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          throw new IOException("Debugger attachment was interrupted.", interrupted);
        }
      }
    } while (!this.closed && System.nanoTime() < deadline);

    if (this.closed) throw new IOException("Debugger attachment was cancelled.", lastError);
    this.setState(State.FAILED, "Could not attach debugger: " + rootMessage(lastError));
    throw new IOException("Could not attach to the project debugger on port " + port + ".", lastError);
  }

  @Override
  public synchronized void setBreakpoints(Collection<ScriptBreakpoint> breakpoints) {
    this.breakpoints.clear();
    if (breakpoints != null) breakpoints.stream().filter(Objects::nonNull).forEach(this.breakpoints::add);
    VirtualMachine current = this.vm;
    if (current == null) return;
    try {
      EventRequestManager requests = current.eventRequestManager();
      requests.deleteEventRequests(new ArrayList<>(requests.breakpointRequests()));
      for (ReferenceType type : current.allClasses()) this.installBreakpoints(type);
    } catch (VMDisconnectedException ignored) {
      this.disconnected();
    }
  }

  @Override
  public synchronized void resume() {
    if (this.vm == null) return;
    this.deleteStepRequests();
    EventSet suspended = this.pausedEvents;
    this.pausedEvents = null;
    this.pausedThread = null;
    this.objectReferences.clear();
    try {
      if (suspended != null) suspended.resume();
      else this.vm.resume();
      this.setState(State.RUNNING, "Project is running");
    } catch (VMDisconnectedException ignored) {
      this.disconnected();
    }
  }

  @Override
  public synchronized void pause() {
    if (this.vm == null || this.state.get() != State.RUNNING) return;
    try {
      this.vm.suspend();
      ThreadReference thread = this.selectPauseThread();
      this.pausedThread = thread;
      this.objectReferences.clear();
      this.setState(State.PAUSED, "Project paused");
      if (thread != null) this.listener.paused(this.snapshot(thread));
    } catch (VMDisconnectedException ignored) {
      this.disconnected();
    }
  }

  @Override public void stepInto() { this.step(StepRequest.STEP_INTO); }

  @Override public void stepOver() { this.step(StepRequest.STEP_OVER); }

  @Override public void stepOut() { this.step(StepRequest.STEP_OUT); }

  @Override
  public synchronized List<ScriptDebugSnapshot.Variable> expandVariable(String reference) {
    if (reference == null || this.state.get() != State.PAUSED) return List.of();
    ObjectReference object = this.objectReferences.get(reference);
    if (object == null) return List.of();
    try {
      return children(object);
    } catch (RuntimeException ignored) {
      return List.of();
    }
  }

  @Override public State state() { return this.state.get(); }

  @Override
  public synchronized void close() {
    this.closed = true;
    VirtualMachine current = this.vm;
    this.vm = null;
    this.pausedEvents = null;
    this.pausedThread = null;
    this.objectReferences.clear();
    if (current != null) {
      try {
        current.dispose();
      } catch (VMDisconnectedException ignored) {
      }
    }
    this.setState(State.DISCONNECTED, "Debugger disconnected");
  }

  private void configureRequests() {
    for (ScriptDefinition definition : this.definitions) {
      String implementation = definition.getImplementation();
      if (implementation == null || implementation.isBlank()) continue;
      ClassPrepareRequest prepare = this.vm.eventRequestManager().createClassPrepareRequest();
      prepare.addClassFilter(implementation + "*");
      prepare.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      prepare.enable();
    }
    for (ReferenceType type : this.vm.allClasses()) this.installBreakpoints(type);
  }

  private void eventLoop() {
    try {
      while (!this.closed && this.vm != null) {
        EventSet events = this.vm.eventQueue().remove();
        boolean paused = false;
        for (Event event : events) {
          if (event instanceof ClassPrepareEvent prepared) {
            this.installBreakpoints(prepared.referenceType());
          } else if (!paused && event instanceof BreakpointEvent breakpoint) {
            paused = true;
            this.pauseAt(events, breakpoint.thread(), "Paused at breakpoint");
          } else if (!paused && event instanceof StepEvent step) {
            paused = true;
            this.deleteStepRequests();
            this.pauseAt(events, step.thread(), "Step complete");
          } else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
            this.disconnected();
            return;
          }
        }
        if (!paused) events.resume();
      }
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    } catch (VMDisconnectedException ignored) {
      this.disconnected();
    } catch (RuntimeException error) {
      this.setState(State.FAILED, "Debugger failed: " + rootMessage(error));
    }
  }

  private synchronized void pauseAt(EventSet events, ThreadReference thread, String detail) {
    this.pausedEvents = events;
    this.pausedThread = thread;
    this.objectReferences.clear();
    this.setState(State.PAUSED, detail);
    this.listener.paused(this.snapshot(thread));
  }

  private void installBreakpoints(ReferenceType type) {
    ScriptDefinition definition = this.definitionFor(type.name());
    if (definition == null) return;
    for (ScriptBreakpoint breakpoint : this.breakpoints) {
      if (!breakpoint.enabled() || !definition.getId().equals(breakpoint.scriptId())) continue;
      try {
        for (Location location : type.locationsOfLine(breakpoint.line())) {
          BreakpointRequest request = this.vm.eventRequestManager().createBreakpointRequest(location);
          request.setSuspendPolicy(EventRequest.SUSPEND_ALL);
          request.enable();
        }
      } catch (AbsentInformationException ignored) {
      }
    }
  }

  private ScriptDefinition definitionFor(String className) {
    for (ScriptDefinition definition : this.definitions) {
      String implementation = definition.getImplementation();
      if (implementation != null && (implementation.equals(className) || className.startsWith(implementation + "$"))) {
        return definition;
      }
    }
    return null;
  }

  private ScriptDebugSnapshot snapshot(ThreadReference thread) {
    List<ScriptDebugSnapshot.Frame> result = new ArrayList<>();
    try {
      List<StackFrame> frames = thread.frames(0, Math.min(thread.frameCount(), MAX_FRAMES));
      boolean hasScriptFrame = frames.stream()
          .anyMatch(frame -> this.definitionFor(frame.location().declaringType().name()) != null);
      for (StackFrame frame : frames) {
        Location location = frame.location();
        boolean inspectVariables = this.definitionFor(location.declaringType().name()) != null
            || (!hasScriptFrame && result.isEmpty());
        List<ScriptDebugSnapshot.Variable> variables = inspectVariables ? variables(frame) : List.of();
        String source;
        try {
          source = location.sourceName();
        } catch (AbsentInformationException ignored) {
          source = "Unknown Source";
        }
        result.add(new ScriptDebugSnapshot.Frame(location.declaringType().name(), location.method().name(),
            source, location.lineNumber(), variables));
      }
    } catch (IncompatibleThreadStateException | RuntimeException ignored) {
    }
    return new ScriptDebugSnapshot(thread.name(), result);
  }

  private List<ScriptDebugSnapshot.Variable> variables(StackFrame frame) {
    List<ScriptDebugSnapshot.Variable> variables = new ArrayList<>();
    ObjectReference thisObject = frame.thisObject();
    if (thisObject != null) {
      variables.add(this.variable("this", thisObject.referenceType().name(), thisObject));
      thisObject.referenceType().allFields().stream()
          .filter(field -> !field.isStatic() && !field.isSynthetic()).limit(MAX_FIELDS)
          .forEach(field -> variables.add(this.variable(
              "this." + field.name(), field.typeName(), thisObject.getValue(field))));
    }
    try {
      List<com.sun.jdi.LocalVariable> visible = frame.visibleVariables();
      Map<com.sun.jdi.LocalVariable, Value> values = frame.getValues(visible);
      visible.forEach(local -> variables.add(this.variable(
          local.name(), local.typeName(), values.get(local))));
    } catch (AbsentInformationException ignored) {
    }
    ReferenceType declaringType = frame.location().declaringType();
    declaringType.allFields().stream()
        .filter(field -> field.isStatic() && !field.isSynthetic() && field.declaringType().equals(declaringType))
        .limit(MAX_FIELDS)
        .forEach(field -> variables.add(this.variable(
            field.name(), field.typeName(), declaringType.getValue(field))));
    return List.copyOf(variables);
  }

  private List<ScriptDebugSnapshot.Variable> children(ObjectReference object) {
    if (object instanceof ArrayReference array) {
      List<ScriptDebugSnapshot.Variable> result = new ArrayList<>();
      int count = Math.min(array.length(), MAX_CHILDREN);
      for (int index = 0; index < count; index++) {
        Value value = array.getValue(index);
        result.add(this.variable("[" + index + "]", typeName(value), value));
      }
      if (array.length() > count) {
        result.add(new ScriptDebugSnapshot.Variable("…", "", (array.length() - count) + " more items"));
      }
      return List.copyOf(result);
    }
    if ("de.gurkenlabs.litiengine.scripting.ScriptGlobals".equals(object.referenceType().name())) {
      Value values = fieldValue(object, "values");
      if (values instanceof ObjectReference map) return this.mapEntries(map);
    }
    if (isMap(object)) {
      List<ScriptDebugSnapshot.Variable> entries = this.mapEntries(object);
      if (!entries.isEmpty()) return entries;
    }
    List<ScriptDebugSnapshot.Variable> result = new ArrayList<>();
    object.referenceType().allFields().stream()
        .filter(field -> !field.isStatic() && !field.isSynthetic()).limit(MAX_CHILDREN)
        .forEach(field -> result.add(this.variable(field.name(), field.typeName(), object.getValue(field))));
    return List.copyOf(result);
  }

  private List<ScriptDebugSnapshot.Variable> mapEntries(ObjectReference map) {
    List<ScriptDebugSnapshot.Variable> result = new ArrayList<>();
    String typeName = map.referenceType().name();
    if (typeName.contains("ImmutableCollections$Map1")) {
      addMapEntry(result, fieldValue(map, "k0"), fieldValue(map, "v0"));
      return List.copyOf(result);
    }
    Value tableValue = fieldValue(map, "table");
    if (!(tableValue instanceof ArrayReference table)) return List.of();
    if (typeName.contains("ImmutableCollections$MapN")) {
      for (int index = 0; index + 1 < table.length() && result.size() < MAX_CHILDREN; index += 2) {
        Value key = table.getValue(index);
        if (key != null) addMapEntry(result, key, table.getValue(index + 1));
      }
      return List.copyOf(result);
    }
    java.util.Set<Long> visited = new java.util.HashSet<>();
    for (Value bucket : table.getValues()) {
      if (!(bucket instanceof ObjectReference node)) continue;
      Value first = fieldValue(node, "first");
      if (first instanceof ObjectReference firstNode) node = firstNode;
      while (node != null && result.size() < MAX_CHILDREN && visited.add(node.uniqueID())) {
        Value key = fieldValue(node, "key");
        Value value = fieldValue(node, "val");
        if (value == null) value = fieldValue(node, "value");
        if (key != null) addMapEntry(result, key, value);
        Value next = fieldValue(node, "next");
        node = next instanceof ObjectReference nextNode ? nextNode : null;
      }
      if (result.size() >= MAX_CHILDREN) break;
    }
    return List.copyOf(result);
  }

  private void addMapEntry(List<ScriptDebugSnapshot.Variable> result, Value key, Value value) {
    String label = key instanceof StringReference text ? text.value() : format(key);
    result.add(this.variable("[" + label + "]", typeName(value), value));
  }

  private ScriptDebugSnapshot.Variable variable(String name, String type, Value value) {
    String reference = null;
    if (value instanceof ObjectReference object && !(value instanceof StringReference)
        && boxedValue(object) == null) {
      reference = Long.toString(object.uniqueID());
      this.objectReferences.put(reference, object);
    }
    return new ScriptDebugSnapshot.Variable(name, type, format(value), reference);
  }

  private static Value fieldValue(ObjectReference object, String name) {
    Field field = object.referenceType().allFields().stream()
        .filter(candidate -> candidate.name().equals(name)).findFirst().orElse(null);
    return field == null ? null : object.getValue(field);
  }

  private static boolean isMap(ObjectReference object) {
    if (!(object.referenceType() instanceof com.sun.jdi.ClassType type)) return false;
    return type.allInterfaces().stream().anyMatch(candidate -> "java.util.Map".equals(candidate.name()));
  }

  private static String typeName(Value value) {
    return value == null ? "null" : value.type().name();
  }

  private ThreadReference selectPauseThread() {
    ThreadReference fallback = null;
    for (ThreadReference candidate : this.vm.allThreads()) {
      try {
        List<StackFrame> frames = candidate.frames(0, Math.min(candidate.frameCount(), MAX_FRAMES));
        if (frames.isEmpty()) continue;
        if (fallback == null || "main".equals(candidate.name())) fallback = candidate;
        if (frames.stream().anyMatch(frame -> this.definitionFor(frame.location().declaringType().name()) != null)) {
          return candidate;
        }
      } catch (IncompatibleThreadStateException | VMDisconnectedException ignored) {
      }
    }
    return fallback;
  }

  private synchronized void step(int depth) {
    if (this.vm == null || this.pausedThread == null || this.state.get() != State.PAUSED) return;
    this.deleteStepRequests();
    StepRequest request = this.vm.eventRequestManager().createStepRequest(this.pausedThread, StepRequest.STEP_LINE, depth);
    request.addCountFilter(1);
    request.addClassExclusionFilter("java.*");
    request.addClassExclusionFilter("jdk.*");
    request.addClassExclusionFilter("sun.*");
    request.setSuspendPolicy(EventRequest.SUSPEND_ALL);
    request.enable();
    this.resumeAfterStep();
  }

  private void resumeAfterStep() {
    EventSet suspended = this.pausedEvents;
    this.pausedEvents = null;
    this.pausedThread = null;
    this.objectReferences.clear();
    if (suspended != null) suspended.resume();
    else this.vm.resume();
    this.setState(State.RUNNING, "Project is running");
  }

  private void deleteStepRequests() {
    if (this.vm == null) return;
    try {
      this.vm.eventRequestManager().deleteEventRequests(new ArrayList<>(this.vm.eventRequestManager().stepRequests()));
    } catch (VMDisconnectedException ignored) {
    }
  }

  private void disconnected() {
    this.vm = null;
    this.pausedEvents = null;
    this.pausedThread = null;
    this.objectReferences.clear();
    this.setState(State.DISCONNECTED, "Debugger disconnected");
  }

  private void setState(State newState, String detail) {
    this.state.set(newState);
    this.listener.stateChanged(newState, detail);
  }

  private static VirtualMachine attachSocket(String host, int port) throws IOException {
    AttachingConnector connector = Bootstrap.virtualMachineManager().attachingConnectors().stream()
        .filter(candidate -> "com.sun.jdi.SocketAttach".equals(candidate.name()))
        .findFirst().orElseThrow(() -> new IOException("The JDI socket connector is unavailable."));
    Map<String, Connector.Argument> arguments = connector.defaultArguments();
    arguments.get("hostname").setValue(host);
    arguments.get("port").setValue(Integer.toString(port));
    Connector.Argument timeout = arguments.get("timeout");
    if (timeout != null) timeout.setValue("1000");
    try {
      return connector.attach(arguments);
    } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException error) {
      throw new IOException("Invalid debugger connection settings.", error);
    }
  }

  private static String format(Value value) {
    if (value == null) return "null";
    if (value instanceof StringReference text) return truncate('"' + text.value() + '"');
    if (value instanceof ArrayReference array) return array.referenceType().name() + "[" + array.length() + "]";
    if (value instanceof ObjectReference object) {
      Value boxed = boxedValue(object);
      if (boxed != null) return format(boxed);
      String type = object.referenceType().name();
      String simple = type.substring(type.lastIndexOf('.') + 1);
      return simple + " #" + object.uniqueID();
    }
    return truncate(value.toString());
  }

  private static Value boxedValue(ObjectReference object) {
    String type = object.referenceType().name();
    if (!BOXED_TYPES.contains(type)) return null;
    return fieldValue(object, "value");
  }

  private static String truncate(String value) {
    if (value == null || value.length() <= MAX_VALUE_LENGTH) return value;
    return value.substring(0, MAX_VALUE_LENGTH - 1) + "\u2026";
  }

  private static String rootMessage(Throwable error) {
    if (error == null) return "unknown error";
    Throwable current = error;
    while (current.getCause() != null) current = current.getCause();
    return Objects.requireNonNullElse(current.getMessage(), current.getClass().getSimpleName());
  }
}
