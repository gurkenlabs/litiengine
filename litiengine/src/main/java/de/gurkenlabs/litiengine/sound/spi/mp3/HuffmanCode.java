package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.sound.spi.BitReader;

import java.util.*;

/**
 * The code tables provided by this class are specified in the Appendix B.7 - Huffman codes for Layer III in ISO 11172-3.
 */
class HuffmanCode {
  /**
   * The deepest huffman code tree is 19 levels in the MP3 specification.
   */
  private static final int MAX_LEVEL = 19;
  private static CodeTable[] tables = new CodeTable[31];

  static {

    // Huffman code table 0
    tables[0] = new CodeTable();

    // Huffman code table 1
    tables[1] = new CodeTable(
      new Node(0, 0, 1, 0b1),
      new Node(0, 1, 3, 0b001),
      new Node(1, 0, 2, 0b01),
      new Node(1, 1, 3, 0b000)
    );

    // Huffman code table 2
    tables[2] = new CodeTable(
      new Node(0, 0, 1, 0b1),
      new Node(0, 1, 3, 0b010),
      new Node(0, 2, 6, 0b000001),
      new Node(1, 0, 3, 0b011),
      new Node(1, 1, 3, 0b001),
      new Node(1, 2, 5, 0b00001),
      new Node(2, 0, 5, 0b00011),
      new Node(2, 1, 5, 0b00010),
      new Node(2, 2, 6, 0b000000)
    );

    // Huffman code table 3
    tables[3] = new CodeTable(
      new Node(0, 0, 2, 0b11),
      new Node(0, 1, 2, 0b10),
      new Node(0, 2, 6, 0b000001),
      new Node(1, 0, 3, 0b001),
      new Node(1, 1, 2, 0b01),
      new Node(1, 2, 5, 0b00001),
      new Node(2, 0, 5, 0b00011),
      new Node(2, 1, 5, 0b00010),
      new Node(2, 2, 6, 0b000000)
    );

    // Huffman code table 4
    tables[4] = null; // this table index is not used

    // Huffman code table 5
    tables[5] = new CodeTable(
      new Node(0, 0, 1, 0b1),
      new Node(0, 1, 3, 0b010),
      new Node(0, 2, 6, 0b000110),
      new Node(0, 3, 7, 0b0000101),
      new Node(1, 0, 3, 0b011),
      new Node(1, 1, 3, 0b001),
      new Node(1, 2, 6, 0b000100),
      new Node(1, 3, 7, 0b0000100),
      new Node(2, 0, 6, 0b000111),
      new Node(2, 1, 6, 0b000101),
      new Node(2, 2, 7, 0b0000111),
      new Node(2, 3, 8, 0b00000001),
      new Node(3, 0, 7, 0b0000110),
      new Node(3, 1, 6, 0b000001),
      new Node(3, 2, 7, 0b0000001),
      new Node(3, 3, 8, 0b00000000)
    );

    // Huffman code table 6
    tables[6] = new CodeTable(
      new Node(0, 0, 3, 0b111),
      new Node(0, 1, 3, 0b011),
      new Node(0, 2, 5, 0b00101),
      new Node(0, 3, 7, 0b0000001),
      new Node(1, 0, 3, 0b0110),
      new Node(1, 1, 2, 0b10),
      new Node(1, 2, 4, 0b0011),
      new Node(1, 3, 5, 0b00010),
      new Node(2, 0, 4, 0b0101),
      new Node(2, 1, 4, 0b0100),
      new Node(2, 2, 5, 0b00100),
      new Node(2, 3, 6, 0b000001),
      new Node(3, 0, 6, 0b000011),
      new Node(3, 1, 5, 0b0011),
      new Node(3, 2, 6, 0b000010),
      new Node(3, 3, 7, 0b0000000)
    );

    // Huffman code table 7
    tables[7] = new CodeTable(
      new Node(0, 0, 1, 0b1),
      new Node(0, 1, 3, 0b010),
      new Node(0, 2, 6, 0b001010),
      new Node(0, 3, 8, 0b00010011),
      new Node(0, 4, 8, 0b00010000),
      new Node(0, 5, 9, 0b000001010),
      new Node(1, 0, 3, 0b011),
      new Node(1, 1, 4, 0b0011),
      new Node(1, 2, 6, 0b000111),
      new Node(1, 3, 7, 0b0001010),
      new Node(1, 4, 7, 0b0000101),
      new Node(1, 5, 8, 0b00000011),
      new Node(2, 0, 6, 0b001011),
      new Node(2, 1, 5, 0b00100),
      new Node(2, 2, 7, 0b0001101),
      new Node(2, 3, 8, 0b00010001),
      new Node(2, 4, 8, 0b00001000),
      new Node(2, 5, 9, 0b000000100),
      new Node(3, 0, 7, 0b0001100),
      new Node(3, 1, 7, 0b0001011),
      new Node(3, 2, 8, 0b00010010),
      new Node(3, 3, 9, 0b000001111),
      new Node(3, 4, 9, 0b000001011),
      new Node(3, 5, 9, 0b000000010),
      new Node(4, 0, 7, 0b0000111),
      new Node(4, 1, 7, 0b0000110),
      new Node(4, 2, 8, 0b00001001),
      new Node(4, 3, 9, 0b000001110),
      new Node(4, 4, 9, 0b000000011),
      new Node(4, 5, 10, 0b0000000001),
      new Node(5, 0, 8, 0b00000110),
      new Node(5, 1, 8, 0b00000100),
      new Node(5, 2, 9, 0b000000101),
      new Node(5, 3, 10, 0b0000000011),
      new Node(5, 4, 10, 0b0000000010),
      new Node(5, 5, 10, 0b0000000000)
    );

    // Huffman code table 8
    tables[8] = new CodeTable(
      new Node(0, 0, 2, 0b11),
      new Node(0, 1, 3, 0b100),
      new Node(0, 2, 6, 0b000110),
      new Node(0, 3, 8, 0b00010010),
      new Node(0, 4, 8, 0b00001100),
      new Node(0, 5, 9, 0b000000101),
      new Node(1, 0, 3, 0b101),
      new Node(1, 1, 2, 0b01),
      new Node(1, 2, 4, 0b0010),
      new Node(1, 3, 8, 0b00010000),
      new Node(1, 4, 8, 0b00001001),
      new Node(1, 5, 8, 0b00000011),
      new Node(2, 0, 6, 0b000111),
      new Node(2, 1, 4, 0b0011),
      new Node(2, 2, 6, 0b000101),
      new Node(2, 3, 8, 0b00001110),
      new Node(2, 4, 8, 0b00000111),
      new Node(2, 5, 9, 0b000000011),
      new Node(3, 0, 8, 0b00010011),
      new Node(3, 1, 8, 0b00010001),
      new Node(3, 2, 8, 0b00001111),
      new Node(3, 3, 9, 0b000001101),
      new Node(3, 4, 9, 0b000001010),
      new Node(3, 5, 10, 0b0000000100),
      new Node(4, 0, 8, 0b00001101),
      new Node(4, 1, 7, 0b0000101),
      new Node(4, 2, 8, 0b00001000),
      new Node(4, 3, 9, 0b000001011),
      new Node(4, 4, 10, 0b0000000101),
      new Node(4, 5, 10, 0b0000000001),
      new Node(5, 0, 9, 0b000001100),
      new Node(5, 1, 8, 0b00000100),
      new Node(5, 2, 9, 0b000000100),
      new Node(5, 3, 9, 0b000000001),
      new Node(5, 4, 11, 0b00000000001),
      new Node(5, 5, 11, 0b00000000000)
    );

    // Huffman code table 9
    tables[9] = new CodeTable(
      new Node(0, 0, 3, 0b111),
      new Node(0, 1, 3, 0b101),
      new Node(0, 2, 5, 0b01001),
      new Node(0, 3, 6, 0b001110),
      new Node(0, 4, 8, 0b00001111),
      new Node(0, 5, 9, 0b000000111),
      new Node(1, 0, 3, 0b110),
      new Node(1, 1, 3, 0b100),
      new Node(1, 2, 4, 0b0101),
      new Node(1, 3, 5, 0b00101),
      new Node(1, 4, 6, 0b000110),
      new Node(1, 5, 8, 0b00000111),
      new Node(2, 0, 4, 0b0111),
      new Node(2, 1, 4, 0b0110),
      new Node(2, 2, 5, 0b01000),
      new Node(2, 3, 6, 0b001000),
      new Node(2, 4, 7, 0b0001000),
      new Node(2, 5, 8, 0b00000101),
      new Node(3, 0, 6, 0b001111),
      new Node(3, 1, 5, 0b00110),
      new Node(3, 2, 6, 0b001001),
      new Node(3, 3, 7, 0b0001010),
      new Node(3, 4, 7, 0b0000101),
      new Node(3, 5, 8, 0b00000001),
      new Node(4, 0, 7, 0b0001011),
      new Node(4, 1, 6, 0b000111),
      new Node(4, 2, 7, 0b0001001),
      new Node(4, 3, 7, 0b0000110),
      new Node(4, 4, 8, 0b00000100),
      new Node(4, 5, 9, 0b000000001),
      new Node(5, 0, 9, 0b000001100),
      new Node(5, 1, 7, 0b0000100),
      new Node(5, 2, 8, 0b00000110),
      new Node(5, 3, 8, 0b00000010),
      new Node(5, 4, 9, 0b000000110),
      new Node(5, 5, 9, 0b000000000)
    );

    // Huffman code table 10
    tables[10] = new CodeTable(
      new Node(0, 0, 1, 0b1),
      new Node(0, 1, 3, 0b010),
      new Node(0, 2, 6, 0b001010),
      new Node(0, 3, 8, 0b00010111),
      new Node(0, 4, 9, 0b000100011),
      new Node(0, 5, 9, 0b000011110),
      new Node(0, 6, 9, 0b000001100),
      new Node(0, 7, 10, 0b0000010001),
      new Node(1, 0, 3, 0b011),
      new Node(1, 1, 4, 0b0011),
      new Node(1, 2, 6, 0b001000),
      new Node(1, 3, 7, 0b0001100),
      new Node(1, 4, 8, 0b00010010),
      new Node(1, 5, 9, 0b000010101),
      new Node(1, 6, 8, 0b00001100),
      new Node(1, 7, 8, 0b00000111),
      new Node(2, 0, 6, 0b001011),
      new Node(2, 1, 6, 0b001001),
      new Node(2, 2, 7, 0b0001111),
      new Node(2, 3, 8, 0b00010101),
      new Node(2, 4, 9, 0b000100000),
      new Node(2, 5, 10, 0b0000101000),
      new Node(2, 6, 9, 0b000010011),
      new Node(2, 7, 9, 0b000000110),
      new Node(3, 0, 7, 0b0001110),
      new Node(3, 1, 7, 0b0001101),
      new Node(3, 2, 8, 0b00010110),
      new Node(3, 3, 9, 0b000100010),
      new Node(3, 4, 10, 0b0000101110),
      new Node(3, 5, 10, 0b0000010111),
      new Node(3, 6, 9, 0b000010010),
      new Node(3, 7, 10, 0b0000000111),
      new Node(4, 0, 8, 0b00010100),
      new Node(4, 1, 8, 0b00010011),
      new Node(4, 2, 9, 0b000100001),
      new Node(4, 3, 10, 0b0000101111),
      new Node(4, 4, 10, 0b0000011011),
      new Node(4, 5, 10, 0b0000010110),
      new Node(4, 6, 10, 0b0000001001),
      new Node(4, 7, 10, 0b0000000011),
      new Node(5, 0, 9, 0b000011111),
      new Node(5, 1, 9, 0b000010110),
      new Node(5, 2, 10, 0b0000101001),
      new Node(5, 3, 10, 0b0000011010),
      new Node(5, 4, 11, 0b00000010101),
      new Node(5, 5, 11, 0b00000010100),
      new Node(5, 6, 10, 0b0000000101),
      new Node(5, 7, 11, 0b00000000011),
      new Node(6, 0, 8, 0b00001110),
      new Node(6, 1, 8, 0b00001101),
      new Node(6, 2, 9, 0b000001010),
      new Node(6, 3, 10, 0b0000001011),
      new Node(6, 4, 10, 0b0000010000),
      new Node(6, 5, 10, 0b0000000110),
      new Node(6, 6, 11, 0b00000000101),
      new Node(6, 7, 11, 0b00000000001),
      new Node(7, 0, 9, 0b000001001),
      new Node(7, 1, 8, 0b00001000),
      new Node(7, 2, 9, 0b000000111),
      new Node(7, 3, 10, 0b0000001000),
      new Node(7, 4, 10, 0b0000000100),
      new Node(7, 5, 11, 0b00000000100),
      new Node(7, 6, 11, 0b00000000010),
      new Node(7, 7, 11, 0b00000000000)
    );

    // Huffman code table 11
    tables[11] = new CodeTable(
    );
    // Huffman code table 14
    tables[14] = null; // this table index is not used
  }

  static Node decode(CodeTable table, BitReader bitReader) {
    if (table.getLevels() == 0) {
      return Node.EMPTY;
    }

    var nextBit = bitReader.getNextBit();
    var hcod = 0;
    var level = 1;
    while (nextBit != BitReader.END_OF_DATA && level <= MAX_LEVEL) {
      hcod = hcod << 1 | nextBit;

      var node = table.getNode(level, hcod);
      if (node != null) {
        return node;
      }

      level++;
      nextBit = bitReader.getNextBit();
    }

    // the specified tree doesn't contain a matching node for the provided bit reader at its position
    return null;
  }

  static CodeTable getTable(int tableIndex) {
    return tables[tableIndex];
  }

  record Node(int x, int y, int hlen, int hcod) {
    /**
     * An empty node that provides x = 0 and y = 0 (usually only used for code tree 0).
     */
    static final Node EMPTY = new Node(0, 0, 0, 0b0);
  }

  static class CodeTable {

    private final Map<Integer, List<Node>> nodesByLevel = new HashMap<>();

    private final int levels;

    int linbits;

    CodeTable(Node... entries) {
      var maxLevel = 0;
      for (var value : entries) {
        var level = value.hlen;
        if (!this.nodesByLevel.containsKey(level)) {
          this.nodesByLevel.put(level, new ArrayList<>());
        }

        this.nodesByLevel.get(level).add(value);
        maxLevel = Math.max(maxLevel, value.hlen);
      }

      this.levels = maxLevel;
    }

    CodeTable(int linbits, Node... entries) {
      this(entries);
      this.linbits = linbits;
    }

    public int getLevels() {
      return this.levels;
    }

    public Node getNode(int level, int huffmanCode) {
      if (level < 0 || level > this.levels || !this.nodesByLevel.containsKey(level)) {
        return null;
      }

      var node = this.nodesByLevel.get(level).stream().filter(x -> x.hcod == huffmanCode).findFirst();
      return node.orElse(null);
    }
  }
}
