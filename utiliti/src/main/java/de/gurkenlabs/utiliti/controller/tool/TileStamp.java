package de.gurkenlabs.utiliti.controller.tool;

import java.util.List;

/** Immutable row-major tile pattern used by the stamp brush. A GID of zero is transparent. */
public record TileStamp(int width, int height, List<Integer> gids) {
  private static final TileStamp EMPTY = new TileStamp(0, 0, List.of());

  public TileStamp {
    gids = List.copyOf(gids);
    if (width < 0 || height < 0 || (width == 0) != (height == 0)) {
      throw new IllegalArgumentException("Tile stamp dimensions must both be zero or positive.");
    }
    if ((long) width * height != gids.size()) {
      throw new IllegalArgumentException("Tile stamp dimensions must match its GID count.");
    }
  }

  public static TileStamp empty() {
    return EMPTY;
  }

  public static TileStamp single(int gid) {
    return gid == 0 ? EMPTY : new TileStamp(1, 1, List.of(gid));
  }

  public boolean isEmpty() {
    return gids.isEmpty();
  }

  public int gidAt(int x, int y) {
    return gids.get(x + y * width);
  }
}
