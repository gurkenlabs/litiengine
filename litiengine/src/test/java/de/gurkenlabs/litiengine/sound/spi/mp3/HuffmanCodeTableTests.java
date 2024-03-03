package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.sound.spi.BitReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static junit.framework.Assert.*;

public class HuffmanCodeTableTests {

  @Test
  void testDecoding() {
    var table = HuffmanCode.getTable(5);
    var node = table.getNode(7, 0b0000100);

    assertEquals(7, node.hlen());
    assertEquals(0b0000100, node.hcod());
    assertEquals(1, node.x());
    assertEquals(3, node.y());
  }

  @Test
  void testDecodingWithBitReader() {
    // 1, 010, 000110, 001, 010
    var bitReader = new BitReader((byte) 0b10100001, (byte) 0b10001010);
    var tree = HuffmanCode.getTable(5);

    var nodes = new ArrayList<HuffmanCode.Node>();
    HuffmanCode.Node node;
    do {
      node = HuffmanCode.decode(tree, bitReader);

      if (node != null) {
        nodes.add(node);
      }
    } while (node != null);

    assertEquals(5, nodes.size());
  }

  @Test
  void ensureCodeTablesAreValid() {
    for (int i = 0; i < 31; i++) {
      var table = HuffmanCode.getTable(i);

      // table 4 and 14 are not used and therefore no code table is present
      if (i == 4 || i == 14) {
        assertNull(table);
        continue;
      } else {
        assertNotNull("table " + i + " should not be null", table);
      }

      for (var nodes : table.getNodes()) {
        var uniqueValues = new ArrayList<Integer>();
        for (var node : nodes) {
          var val = node.hcod() << node.hlen();
          assertFalse("table " + i  + "; node {" + node.x()  + ", " + node.y() + "}: value not unique in table", uniqueValues.contains(val));

          uniqueValues.add(val);
        }
      }
    }
  }
}