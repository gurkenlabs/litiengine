package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangSet;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangTile;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TilesetEntry;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Wang terrain constraints using the same propagation model as Tiled's WangFiller. */
final class TerrainResolver {
  private static final int[] OFFSET_X = {0, 1, 1, 1, 0, -1, -1, -1};
  private static final int[] OFFSET_Y = {-1, -1, 0, 1, 1, 1, 0, -1};
  private static final int[] ALL_POSITIONS = {0, 1, 2, 3, 4, 5, 6, 7};
  private static final int[] CORNER_POSITIONS = {1, 3, 5, 7};
  private static final int[] EDGE_POSITIONS = {0, 2, 4, 6};

  private final ITileLayer layer;
  private final ITileset tileset;
  private final WangSet terrainSet;
  private final List<WangTile> candidates;
  private final int[] positions;
  private final int[][] distances;
  private final Map<Point, CellInfo> cells = new HashMap<>();
  private final Map<Point, Integer> resolved = new LinkedHashMap<>();
  private final ArrayDeque<Point> pending = new ArrayDeque<>();
  private final Set<Point> queued = new HashSet<>();
  private final Set<Point> initial = new HashSet<>();
  private final int correctionRadius;
  private Point center;

  private TerrainResolver(ITileLayer layer, ITileset tileset, WangSet terrainSet) {
    this.layer = layer;
    this.tileset = tileset;
    this.terrainSet = terrainSet;
    this.candidates = terrainSet.getWangTiles();
    this.positions = positions(terrainSet.getType());
    this.distances = colorDistances(terrainSet);
    this.correctionRadius = maximumDistance(this.distances) + 1;
  }

  static Result resolve(ITileLayer layer, ITileset tileset, WangSet terrainSet, int terrain, Point center) {
    TerrainResolver resolver = new TerrainResolver(layer, tileset, terrainSet);
    return resolver.resolve(terrain, center);
  }

  private Result resolve(int terrain, Point center) {
    this.center = new Point(center);
    setFullTile(center, terrain);

    queue(center);
    for (int index = 0; index < 8; index++) {
      Point adjacent = adjacent(center, index);
      if (this.initial.contains(adjacent)) {
        queue(adjacent);
      }
    }

    int invalid = 0;
    while (!this.pending.isEmpty()) {
      Point point = this.pending.removeFirst();
      this.queued.remove(point);
      if (this.resolved.containsKey(point) || this.layer.getTile(point.x, point.y) == null) {
        continue;
      }

      CellInfo info = cell(point);
      WangTile match = bestMatch(info);
      if (match == null) {
        invalid++;
        continue;
      }

      this.resolved.put(new Point(point), match.getTileId());
      propagate(point, match.getWangId());
    }

    Map<Point, Integer> changes = new LinkedHashMap<>();
    for (Map.Entry<Point, Integer> entry : this.resolved.entrySet()) {
      Point point = entry.getKey();
      int gid = this.tileset.getFirstGridId() + entry.getValue();
      ITile tile = this.layer.getTile(point.x, point.y);
      if (tile != null && tile.getGridId() != gid) {
        changes.put(point, gid);
      }
    }
    return new Result(changes, invalid);
  }

  private void setFullTile(Point point, int terrain) {
    CellInfo centerInfo = cell(point);
    for (int position : this.positions) {
      constrain(centerInfo, position, terrain);
    }
    this.initial.add(new Point(point));

    for (int index = 0; index < 8; index++) {
      boolean corner = isCorner(index);
      if (this.terrainSet.getType() == TerrainType.EDGE && corner) {
        continue;
      }

      Point adjacent = adjacent(point, index);
      if (this.layer.getTile(adjacent.x, adjacent.y) == null) {
        continue;
      }
      CellInfo info = cell(adjacent);
      this.initial.add(adjacent);

      if (corner || this.terrainSet.getType() != TerrainType.CORNER) {
        constrain(info, opposite(index), terrain);
      }
      if (!corner && this.terrainSet.getType() != TerrainType.EDGE) {
        constrain(info, (index + 3) % 8, terrain);
        constrain(info, (index + 5) % 8, terrain);
      }
    }
  }

  private void propagate(Point point, int[] wangId) {
    for (int index = 0; index < 8; index++) {
      Point adjacent = adjacent(point, index);
      if (this.layer.getTile(adjacent.x, adjacent.y) == null || this.resolved.containsKey(adjacent)) {
        continue;
      }

      CellInfo info = cell(adjacent);
      updateAdjacent(info, wangId, index);

      if (this.initial.contains(adjacent)) {
        queue(adjacent);
        continue;
      }

      if (!isCorner(index) && inCorrectionBounds(adjacent) && !matchesCurrent(adjacent, info)) {
        queue(adjacent);
      }
    }
  }

  private static void updateAdjacent(CellInfo adjacent, int[] wangId, int adjacentIndex) {
    int index = opposite(adjacentIndex);
    constrain(adjacent, index, wangId[adjacentIndex]);

    if (!isCorner(index)) {
      constrain(adjacent, (index + 1) % 8, wangId[(adjacentIndex + 7) % 8]);
      constrain(adjacent, (index + 7) % 8, wangId[(adjacentIndex + 1) % 8]);
    }
  }

  private WangTile bestMatch(CellInfo info) {
    List<WangTile> best = new ArrayList<>();
    int lowestPenalty = Integer.MAX_VALUE;
    for (WangTile candidate : this.candidates) {
      int[] wangId = candidate.getWangId();
      if (!validWangId(wangId)) {
        continue;
      }
      if (!matchesConstraints(wangId, info, this.positions)) {
        continue;
      }

      int penalty = 0;
      boolean reachable = true;
      for (int position : this.positions) {
        if (info.mask[position] || info.desired[position] == wangId[position]) {
          continue;
        }
        int distance = this.distances[info.desired[position]][wangId[position]];
        if (distance < 0) {
          reachable = false;
          break;
        }
        penalty += distance;
      }
      if (reachable && penalty < lowestPenalty) {
        best.clear();
        best.add(candidate);
        lowestPenalty = penalty;
      } else if (reachable && penalty == lowestPenalty) {
        best.add(candidate);
      }
    }
    return weightedCandidate(best);
  }

  private boolean validWangId(int[] wangId) {
    return Arrays.stream(wangId).allMatch(id -> id >= 0 && id < this.distances.length);
  }

  private WangTile weightedCandidate(List<WangTile> matches) {
    double total = 0;
    for (WangTile match : matches) {
      total += candidateWeight(match);
    }
    if (total <= 0) {
      return null;
    }
    double selected = ThreadLocalRandom.current().nextDouble(total);
    for (WangTile match : matches) {
      selected -= candidateWeight(match);
      if (selected < 0) {
        return match;
      }
    }
    return matches.getLast();
  }

  private double candidateWeight(WangTile candidate) {
    double probability = this.tileset.getTile(candidate.getTileId()) instanceof TilesetEntry entry ? entry.getProbability() : 1.0;
    return Double.isFinite(probability) && probability >= 0 ? probability : 0;
  }

  private boolean matchesCurrent(Point point, CellInfo info) {
    ITile tile = this.layer.getTile(point.x, point.y);
    int[] wangId = tile != null && this.tileset.containsTile(tile.getGridId())
      ? this.terrainSet.getWangId(tile.getGridId() - this.tileset.getFirstGridId())
      : new int[8];
    return matchesConstraints(wangId, info, this.positions);
  }

  private boolean inCorrectionBounds(Point point) {
    return Math.abs(point.x - this.center.x) <= this.correctionRadius
      && Math.abs(point.y - this.center.y) <= this.correctionRadius;
  }

  private CellInfo cell(Point point) {
    return this.cells.computeIfAbsent(new Point(point), key -> {
      ITile tile = this.layer.getTile(key.x, key.y);
      int[] desired = tile != null && this.tileset.containsTile(tile.getGridId())
        ? this.terrainSet.getWangId(tile.getGridId() - this.tileset.getFirstGridId())
        : new int[8];
      return new CellInfo(desired);
    });
  }

  private void queue(Point point) {
    Point key = new Point(point);
    if (this.queued.add(key)) {
      this.pending.addLast(key);
    }
  }

  private static boolean matchesConstraints(int[] wangId, CellInfo info, int[] positions) {
    for (int index : positions) {
      if (info.mask[index] && wangId[index] != info.desired[index]) {
        return false;
      }
    }
    return true;
  }

  private static void constrain(CellInfo info, int index, int terrain) {
    info.desired[index] = terrain;
    info.mask[index] = true;
  }

  private static Point adjacent(Point point, int index) {
    return new Point(point.x + OFFSET_X[index], point.y + OFFSET_Y[index]);
  }

  private static int opposite(int index) {
    return (index + 4) % 8;
  }

  private static boolean isCorner(int index) {
    return index % 2 == 1;
  }

  private static int[] positions(TerrainType type) {
    return switch (type) {
      case CORNER -> CORNER_POSITIONS;
      case EDGE -> EDGE_POSITIONS;
      case MIXED -> ALL_POSITIONS;
    };
  }

  private static int[][] colorDistances(WangSet terrainSet) {
    int count = terrainSet.getTerrains().size() + 1;
    int[][] distances = new int[count][count];
    for (int[] row : distances) {
      Arrays.fill(row, -1);
    }
    for (int color = 0; color < count; color++) {
      distances[color][color] = 0;
    }

    for (WangTile tile : terrainSet.getWangTiles()) {
      int[] wangId = tile.getWangId();
      if (terrainSet.getType() != TerrainType.EDGE) {
        connectColors(distances, wangId, CORNER_POSITIONS, count);
      }
      if (terrainSet.getType() != TerrainType.CORNER) {
        connectColors(distances, wangId, EDGE_POSITIONS, count);
      }
    }

    for (int via = 0; via < count; via++) {
      for (int from = 0; from < count; from++) {
        for (int to = 0; to < count; to++) {
          if (distances[from][via] >= 0 && distances[via][to] >= 0) {
            int distance = distances[from][via] + distances[via][to];
            if (distances[from][to] < 0 || distance < distances[from][to]) {
              distances[from][to] = distance;
            }
          }
        }
      }
    }
    return distances;
  }

  private static void connectColors(int[][] distances, int[] wangId, int[] positions, int colorCount) {
    for (int first : positions) {
      for (int second : positions) {
        int a = wangId[first];
        int b = wangId[second];
        if (a >= 0 && b >= 0 && a < colorCount && b < colorCount && (distances[a][b] < 0 || distances[a][b] > 1)) {
          distances[a][b] = distances[b][a] = a == b ? 0 : 1;
        }
      }
    }
  }

  private static int maximumDistance(int[][] distances) {
    int maximum = 1;
    for (int[] row : distances) {
      for (int distance : row) {
        maximum = Math.max(maximum, distance);
      }
    }
    return maximum;
  }

  record Result(Map<Point, Integer> changes, int invalidCells) {
  }

  private static final class CellInfo {
    private final int[] desired;
    private final boolean[] mask = new boolean[8];

    private CellInfo(int[] desired) {
      this.desired = desired.clone();
    }
  }
}
