package de.gurkenlabs.litiengine.scripting;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/// Global thread-safe shared state store accessible across entity, game, and behavior scripts.
public final class ScriptGlobals {
  private final Map<String, Object> values = new ConcurrentHashMap<>();
  private final Map<String, List<BiConsumer<Object, Object>>> listeners = new ConcurrentHashMap<>();

  public Object get(String key) {
    return this.values.get(key);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key, Class<T> type) {
    Object val = this.values.get(key);
    return type != null && type.isInstance(val) ? (T) val : null;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key, T defaultValue) {
    Object val = this.values.get(key);
    if (val == null) return defaultValue;
    if (defaultValue != null && defaultValue.getClass().isInstance(val)) {
      return (T) val;
    }
    return (T) val;
  }

  public void put(String key, Object value) {
    Objects.requireNonNull(key, "Global key cannot be null");
    Object previous = value == null ? this.values.remove(key) : this.values.put(key, value);
    if (!Objects.equals(previous, value)) {
      List<BiConsumer<Object, Object>> keyListeners = this.listeners.get(key);
      if (keyListeners != null) {
        for (BiConsumer<Object, Object> listener : keyListeners) {
          try {
            listener.accept(previous, value);
          } catch (Exception ignored) {
          }
        }
      }
    }
  }

  public Subscription onChanged(String key, BiConsumer<Object, Object> listener) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(listener);
    List<BiConsumer<Object, Object>> list = this.listeners.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
    list.add(listener);
    return () -> list.remove(listener);
  }

  public boolean contains(String key) {
    return this.values.containsKey(key);
  }

  public void remove(String key) {
    this.put(key, null);
  }

  public void clear() {
    this.values.clear();
    this.listeners.clear();
  }

  public Map<String, Object> getEntries() {
    return Map.copyOf(this.values);
  }
}
