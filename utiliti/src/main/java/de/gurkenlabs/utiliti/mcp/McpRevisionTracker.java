package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks map revision counters for stateless optimistic concurrency control.
 * Revisions are stored as custom map properties and also cached in-memory
 * for fast access. Every mutation should call {@link #incrementRevision(IMap)}
 * after successfully modifying map state.
 */
public final class McpRevisionTracker {
  private static final Map<String, Long> REVISIONS = new ConcurrentHashMap<>();
  private static final String PROPERTY_REVISION = "mcp:revision";

  private McpRevisionTracker() {}

  public static long getRevision(IMap map) {
    if (map == null) {
      return 0L;
    }
    String name = map.getName() != null ? map.getName() : "";
    if (map.hasCustomProperty(PROPERTY_REVISION)) {
      try {
        long val = Long.parseLong(map.getStringValue(PROPERTY_REVISION, "1"));
        REVISIONS.put(name, val);
        return val;
      } catch (Exception ignored) {
        // Fall back to in-memory revision
      }
    }
    return REVISIONS.computeIfAbsent(name, k -> 1L);
  }

  public static long incrementRevision(IMap map) {
    if (map == null) {
      return 0L;
    }
    long current = getRevision(map);
    long next = current + 1;
    String name = map.getName() != null ? map.getName() : "";
    REVISIONS.put(name, next);
    try {
      map.setValue(PROPERTY_REVISION, String.valueOf(next));
    } catch (Exception ignored) {
      // Map property update is optional
    }
    return next;
  }

  public static boolean validateRevision(IMap map, Long expectedRevision) {
    if (expectedRevision == null || map == null) {
      return true; // No revision check requested
    }
    return getRevision(map) == expectedRevision;
  }
}
