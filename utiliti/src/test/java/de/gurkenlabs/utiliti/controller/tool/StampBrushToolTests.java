package de.gurkenlabs.utiliti.controller.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.util.List;
import org.junit.jupiter.api.Test;

class StampBrushToolTests {

  @Test
  void stampIsCenteredOnCursorAndSkipsTransparentCells() {
    TileStamp stamp = new TileStamp(3, 2, List.of(1, 0, 3, 4, 5, 0));

    List<StampBrushTool.StampCell> cells =
        StampBrushTool.stampCells(new Point(10, 10), stamp);

    assertEquals(
        List.of(
            new StampBrushTool.StampCell(new Point(9, 9), 1),
            new StampBrushTool.StampCell(new Point(11, 9), 3),
            new StampBrushTool.StampCell(new Point(9, 10), 4),
            new StampBrushTool.StampCell(new Point(10, 10), 5)),
        cells);
  }
}
