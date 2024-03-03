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
      new Node(0, 0, 2, 0b11),
      new Node(0, 1, 3, 0b0100),
      new Node(0, 2, 5, 0b01010),
      new Node(0, 3, 7, 0b0011000),
      new Node(0, 4, 8, 0b00100010),
      new Node(0, 5, 9, 0b000100001),
      new Node(0, 6, 8, 0b00010101),
      new Node(0, 7, 9, 0b000001111),
      new Node(1, 0, 3, 0b101),
      new Node(1, 1, 3, 0b011),
      new Node(1, 2, 4, 0b0100),
      new Node(1, 3, 6, 0b001010),
      new Node(1, 4, 8, 0b00100000),
      new Node(1, 5, 8, 0b00010001),
      new Node(1, 6, 7, 0b0001011),
      new Node(1, 7, 8, 0b00001010),
      new Node(2, 0, 5, 0b01011),
      new Node(2, 1, 5, 0b00111),
      new Node(2, 2, 6, 0b001101),
      new Node(2, 3, 7, 0b0010010),
      new Node(2, 4, 8, 0b00011110),
      new Node(2, 5, 9, 0b000011111),
      new Node(2, 6, 8, 0b00010100),
      new Node(2, 7, 8, 0b00000101),
      new Node(3, 0, 7, 0b0011001),
      new Node(3, 1, 6, 0b001011),
      new Node(3, 2, 7, 0b0010011),
      new Node(3, 3, 9, 0b000111011),
      new Node(3, 4, 8, 0b00011011),
      new Node(3, 5, 10, 0b0000010010),
      new Node(3, 6, 8, 0b00001100),
      new Node(3, 7, 9, 0b000000101),
      new Node(4, 0, 8, 0b00100011),
      new Node(4, 1, 8, 0b00100001),
      new Node(4, 2, 8, 0b00011111),
      new Node(4, 3, 9, 0b000111010),
      new Node(4, 4, 9, 0b000011110),
      new Node(4, 5, 10, 0b0000010000),
      new Node(4, 6, 9, 0b000000111),
      new Node(4, 7, 10, 0b0000000101),
      new Node(5, 0, 8, 0b00011100),
      new Node(5, 1, 8, 0b00011010),
      new Node(5, 2, 9, 0b000100000),
      new Node(5, 3, 10, 0b0000010011),
      new Node(5, 4, 10, 0b0000010001),
      new Node(5, 5, 11, 0b00000001111),
      new Node(5, 6, 10, 0b0000001000),
      new Node(5, 7, 11, 0b00000001110),
      new Node(6, 0, 8, 0b00001110),
      new Node(6, 1, 7, 0b0001100),
      new Node(6, 2, 7, 0b0001001),
      new Node(6, 3, 8, 0b00001101),
      new Node(6, 4, 9, 0b000001110),
      new Node(6, 5, 10, 0b0000001001),
      new Node(6, 6, 10, 0b0000000100),
      new Node(6, 7, 10, 0b0000000001),
      new Node(7, 0, 8, 0b00001011),
      new Node(7, 1, 7, 0b0000100),
      new Node(7, 2, 8, 0b00000110),
      new Node(7, 3, 9, 0b000000110),
      new Node(7, 4, 10, 0b0000000110),
      new Node(7, 5, 10, 0b0000000011),
      new Node(7, 6, 10, 0b0000000010),
      new Node(7, 7, 10, 0b0000000000)
    );

    // Huffman code table 12
    tables[12] = new CodeTable(
      new Node(0, 0, 4, 0b1001),
      new Node(0, 1, 3, 0b110),
      new Node(0, 2, 5, 0b10000),
      new Node(0, 3, 7, 0b0100001),
      new Node(0, 4, 8, 0b00101001),
      new Node(0, 5, 9, 0b000100111),
      new Node(0, 6, 9, 0b000100110),
      new Node(0, 7, 9, 0b000011010),
      new Node(1, 0, 3, 0b111),
      new Node(1, 1, 3, 0b101),
      new Node(1, 2, 4, 0b0110),
      new Node(1, 3, 5, 0b01001),
      new Node(1, 4, 7, 0b0010111),
      new Node(1, 5, 7, 0b0010000),
      new Node(1, 6, 8, 0b00011010),
      new Node(1, 7, 8, 0b00001011),
      new Node(2, 0, 5, 0b10001),
      new Node(2, 1, 4, 0b0111),
      new Node(2, 2, 5, 0b01011),
      new Node(2, 3, 6, 0b001110),
      new Node(2, 4, 7, 0b0010101),
      new Node(2, 5, 8, 0b00011110),
      new Node(2, 6, 7, 0b0001010),
      new Node(2, 7, 8, 0b00000111),
      new Node(3, 0, 6, 0b010001),
      new Node(3, 1, 5, 0b01010),
      new Node(3, 2, 6, 0b001111),
      new Node(3, 3, 6, 0b001100),
      new Node(3, 4, 7, 0b0010010),
      new Node(3, 5, 8, 0b00011100),
      new Node(3, 6, 8, 0b00001110),
      new Node(3, 7, 8, 0b00000101),
      new Node(4, 0, 7, 0b0100000),
      new Node(4, 1, 6, 0b001101),
      new Node(4, 2, 7, 0b0010110),
      new Node(4, 3, 7, 0b0010011),
      new Node(4, 4, 8, 0b00010010),
      new Node(4, 5, 8, 0b00010000),
      new Node(4, 6, 8, 0b00001001),
      new Node(4, 7, 9, 0b000000101),
      new Node(5, 0, 8, 0b00101000),
      new Node(5, 1, 7, 0b0010001),
      new Node(5, 2, 8, 0b00011111),
      new Node(5, 3, 8, 0b00011101),
      new Node(5, 4, 8, 0b00010001),
      new Node(5, 5, 9, 0b000001101),
      new Node(5, 6, 8, 0b00000100),
      new Node(5, 7, 9, 0b000000010),
      new Node(6, 0, 8, 0b00011011),
      new Node(6, 1, 7, 0b0001100),
      new Node(6, 2, 7, 0b0001011),
      new Node(6, 3, 8, 0b00001111),
      new Node(6, 4, 8, 0b00001010),
      new Node(6, 5, 9, 0b000000111),
      new Node(6, 6, 9, 0b000000100),
      new Node(6, 7, 10, 0b0000000001),
      new Node(7, 0, 9, 0b000011011),
      new Node(7, 1, 8, 0b00001100),
      new Node(7, 2, 8, 0b00001000),
      new Node(7, 3, 9, 0b000001100),
      new Node(7, 4, 9, 0b000000110),
      new Node(7, 5, 9, 0b000000011),
      new Node(7, 6, 9, 0b000000001),
      new Node(7, 7, 10, 0b0000000000)
    );

    // Huffman code table 13
    tables[13] = new CodeTable(
      new Node(0, 0, 1, 0b1),
      new Node(0, 1, 4, 0b0101),
      new Node(0, 2, 6, 0b001110),
      new Node(0, 3, 7, 0b0010101),
      new Node(0, 4, 8, 0b00100010),
      new Node(0, 5, 9, 0b000110011),
      new Node(0, 6, 9, 0b000101110),
      new Node(0, 7, 10, 0b0001000111),
      new Node(0, 8, 9, 0b000101010),
      new Node(0, 9, 10, 0b0000110100),
      new Node(0, 10, 11, 0b00001000100),
      new Node(0, 11, 11, 0b00000110100),
      new Node(0, 12, 12, 0b000001000011),
      new Node(0, 13, 12, 0b000000101100),
      new Node(0, 14, 13, 0b0000000101011),
      new Node(0, 15, 13, 0b0000000010011),
      new Node(1, 0, 3, 0b011),
      new Node(1, 1, 4, 0b0100),
      new Node(1, 2, 6, 0b001100),
      new Node(1, 3, 7, 0b0010011),
      new Node(1, 4, 8, 0b00011111),
      new Node(1, 5, 8, 0b00011010),
      new Node(1, 6, 9, 0b000101100),
      new Node(1, 7, 9, 0b000100001),
      new Node(1, 8, 9, 0b000011111),
      new Node(1, 9, 9, 0b000011000),
      new Node(1, 10, 10, 0b0000100000),
      new Node(1, 11, 10, 0b0000011000),
      new Node(1, 12, 11, 0b00000011111),
      new Node(1, 13, 12, 0b000000100011),
      new Node(1, 14, 12, 0b000000010110),
      new Node(1, 15, 12, 0b000000001110),
      new Node(2, 0, 6, 0b001111),
      new Node(2, 1, 6, 0b001101),
      new Node(2, 2, 7, 0b0010111),
      new Node(2, 3, 8, 0b00100100),
      new Node(2, 4, 9, 0b000111011),
      new Node(2, 5, 9, 0b000110001),
      new Node(2, 6, 10, 0b0001001101),
      new Node(2, 7, 10, 0b0001000001),
      new Node(2, 8, 9, 0b000011101),
      new Node(2, 9, 10, 0b0000101000),
      new Node(2, 10, 10, 0b0000011110),
      new Node(2, 11, 11, 0b00000101000),
      new Node(2, 12, 11, 0b00000011011),
      new Node(2, 13, 12, 0b000000100001),
      new Node(2, 14, 13, 0b0000000101010),
      new Node(2, 15, 13, 0b0000000010000),
      new Node(3, 0, 7, 0b0010110),
      new Node(3, 1, 7, 0b0010100),
      new Node(3, 2, 8, 0b00100101),
      new Node(3, 3, 9, 0b000111101),
      new Node(3, 4, 9, 0b000111000),
      new Node(3, 5, 10, 0b0001001111),
      new Node(3, 6, 10, 0b0001001001),
      new Node(3, 7, 10, 0b0001000000),
      new Node(3, 8, 10, 0b0000101011),
      new Node(3, 9, 11, 0b00001001100),
      new Node(3, 10, 11, 0b00000111000),
      new Node(3, 11, 11, 0b00000100101),
      new Node(3, 12, 11, 0b00000011010),
      new Node(3, 13, 12, 0b000000011111),
      new Node(3, 14, 13, 0b0000000011001),
      new Node(3, 15, 13, 0b0000000001110),
      new Node(4, 0, 8, 0b00100011),
      new Node(4, 1, 7, 0b0010000),
      new Node(4, 2, 9, 0b000111100),
      new Node(4, 3, 9, 0b000111001),
      new Node(4, 4, 10, 0b0001100001),
      new Node(4, 5, 10, 0b0001001011),
      new Node(4, 6, 11, 0b00001110010),
      new Node(4, 7, 11, 0b00001011011),
      new Node(4, 8, 10, 0b0000110110),
      new Node(4, 9, 11, 0b00001001001),
      new Node(4, 10, 11, 0b00000110111),
      new Node(4, 11, 12, 0b000000101001),
      new Node(4, 12, 12, 0b000000110000),
      new Node(4, 13, 13, 0b0000000110101),
      new Node(4, 14, 13, 0b0000000010111),
      new Node(4, 15, 14, 0b00000000011000),
      new Node(5, 0, 9, 0b000111010),
      new Node(5, 1, 8, 0b00011011),
      new Node(5, 2, 9, 0b000110010),
      new Node(5, 3, 10, 0b0001100000),
      new Node(5, 4, 10, 0b0001001100),
      new Node(5, 5, 10, 0b0001000110),
      new Node(5, 6, 11, 0b00001011101),
      new Node(5, 7, 11, 0b00001010100),
      new Node(5, 8, 11, 0b00001001101),
      new Node(5, 9, 11, 0b00000111010),
      new Node(5, 10, 12, 0b000001001111),
      new Node(5, 11, 11, 0b00000011101),
      new Node(5, 12, 13, 0b0000001001010),
      new Node(5, 13, 13, 0b0000000110001),
      new Node(5, 14, 14, 0b00000000101001),
      new Node(5, 15, 14, 0b00000000010001),
      new Node(6, 0, 9, 0b000101111),
      new Node(6, 1, 9, 0b000101101),
      new Node(6, 2, 10, 0b0001001110),
      new Node(6, 3, 10, 0b0001001010),
      new Node(6, 4, 11, 0b00001110011),
      new Node(6, 5, 11, 0b00001011110),
      new Node(6, 6, 11, 0b00001011010),
      new Node(6, 7, 11, 0b00001001111),
      new Node(6, 8, 11, 0b00001000101),
      new Node(6, 9, 12, 0b000001010011),
      new Node(6, 10, 12, 0b000001000111),
      new Node(6, 11, 12, 0b000000110010),
      new Node(6, 12, 13, 0b0000000111011),
      new Node(6, 13, 13, 0b0000000100110),
      new Node(6, 14, 14, 0b00000000100100),
      new Node(6, 15, 14, 0b00000000001111),
      new Node(7, 0, 10, 0b0001001000),
      new Node(7, 1, 9, 0b000100010),
      new Node(7, 2, 10, 0b0000111000),
      new Node(7, 3, 11, 0b00001011111),
      new Node(7, 4, 11, 0b00001011100),
      new Node(7, 5, 11, 0b00001010101),
      new Node(7, 6, 12, 0b000001011011),
      new Node(7, 7, 12, 0b000001011010),
      new Node(7, 8, 12, 0b000001010110),
      new Node(7, 9, 12, 0b000001001001),
      new Node(7, 10, 13, 0b0000001001101),
      new Node(7, 11, 13, 0b0000001000001),
      new Node(7, 12, 13, 0b0000000110011),
      new Node(7, 13, 14, 0b00000000101100),
      new Node(7, 14, 16, 0b0000000000101011),
      new Node(7, 15, 16, 0b0000000000101010),
      new Node(8, 0, 9, 0b000101011),
      new Node(8, 1, 8, 0b00010100),
      new Node(8, 2, 9, 0b000011110),
      new Node(8, 3, 10, 0b0000101100),
      new Node(8, 4, 10, 0b0000110111),
      new Node(8, 5, 11, 0b00001001110),
      new Node(8, 6, 11, 0b00001001000),
      new Node(8, 7, 12, 0b000001010111),
      new Node(8, 8, 12, 0b000001001110),
      new Node(8, 9, 12, 0b000000111101),
      new Node(8, 10, 12, 0b000000101110),
      new Node(8, 11, 13, 0b0000000110110),
      new Node(8, 12, 13, 0b0000000100101),
      new Node(8, 13, 14, 0b00000000011110),
      new Node(8, 14, 15, 0b000000000010100),
      new Node(8, 15, 15, 0b000000000010000),
      new Node(9, 0, 10, 0b0000110101),
      new Node(9, 1, 9, 0b000011001),
      new Node(9, 2, 10, 0b0000101001),
      new Node(9, 3, 10, 0b0000100101),
      new Node(9, 4, 11, 0b00000101100),
      new Node(9, 5, 11, 0b00000111011),
      new Node(9, 6, 11, 0b00000110110),
      new Node(9, 7, 13, 0b0000001010001),
      new Node(9, 8, 12, 0b000001000010),
      new Node(9, 9, 13, 0b0000001001100),
      new Node(9, 10, 13, 0b0000000111001),
      new Node(9, 11, 14, 0b00000000110110),
      new Node(9, 12, 14, 0b00000000100101),
      new Node(9, 13, 14, 0b00000000010010),
      new Node(9, 14, 16, 0b0000000000100111),
      new Node(9, 15, 15, 0b000000000001011),
      new Node(10, 0, 10, 0b0000100011),
      new Node(10, 1, 10, 0b0000100001),
      new Node(10, 2, 10, 0b0000011111),
      new Node(10, 3, 11, 0b00000111001),
      new Node(10, 4, 11, 0b00000101010),
      new Node(10, 5, 12, 0b000001010010),
      new Node(10, 6, 12, 0b000001001000),
      new Node(10, 7, 13, 0b0000001010000),
      new Node(10, 8, 12, 0b000000101111),
      new Node(10, 9, 13, 0b0000000111010),
      new Node(10, 10, 14, 0b00000000110111),
      new Node(10, 11, 13, 0b0000000010101),
      new Node(10, 12, 14, 0b00000000010110),
      new Node(10, 13, 15, 0b000000000011010),
      new Node(10, 14, 16, 0b0000000000100110),
      new Node(10, 15, 17, 0b00000000000010110),
      new Node(11, 0, 11, 0b00000110101),
      new Node(11, 1, 10, 0b0000011001),
      new Node(11, 2, 10, 0b0000010111),
      new Node(11, 3, 11, 0b00000100110),
      new Node(11, 4, 12, 0b000001000110),
      new Node(11, 5, 12, 0b000000111100),
      new Node(11, 6, 12, 0b000000110011),
      new Node(11, 7, 12, 0b000000100100),
      new Node(11, 8, 13, 0b0000000110111),
      new Node(11, 9, 13, 0b0000000011010),
      new Node(11, 10, 13, 0b0000000100010),
      new Node(11, 11, 14, 0b00000000010111),
      new Node(11, 12, 15, 0b000000000011011),
      new Node(11, 13, 15, 0b000000000001110),
      new Node(11, 14, 15, 0b000000000001001),
      new Node(11, 15, 16, 0b0000000000000111),
      new Node(12, 0, 11, 0b00000100010),
      new Node(12, 1, 11, 0b00000100000),
      new Node(12, 2, 11, 0b00000011100),
      new Node(12, 3, 12, 0b000000100111),
      new Node(12, 4, 12, 0b000000110001),
      new Node(12, 5, 13, 0b0000001001011),
      new Node(12, 6, 12, 0b000000011110),
      new Node(12, 7, 13, 0b0000000110100),
      new Node(12, 8, 14, 0b00000000110000),
      new Node(12, 9, 14, 0b00000000101000),
      new Node(12, 10, 15, 0b000000000110100),
      new Node(12, 11, 15, 0b000000000011100),
      new Node(12, 12, 15, 0b000000000010010),
      new Node(12, 13, 16, 0b0000000000010001),
      new Node(12, 14, 16, 0b0000000000001001),
      new Node(12, 15, 16, 0b0000000000000101),
      new Node(13, 0, 12, 0b000000101101),
      new Node(13, 1, 11, 0b00000010101),
      new Node(13, 2, 12, 0b000000100010),
      new Node(13, 3, 13, 0b0000001000000),
      new Node(13, 4, 13, 0b0000000111000),
      new Node(13, 5, 13, 0b0000000110010),
      new Node(13, 6, 14, 0b00000000110001),
      new Node(13, 7, 14, 0b00000000101101),
      new Node(13, 8, 14, 0b00000000011111),
      new Node(13, 9, 14, 0b00000000010011),
      new Node(13, 10, 14, 0b00000000001100),
      new Node(13, 11, 15, 0b000000000001111),
      new Node(13, 12, 16, 0b0000000000001010),
      new Node(13, 13, 15, 0b000000000000111),
      new Node(13, 14, 16, 0b0000000000000110),
      new Node(13, 15, 16, 0b0000000000000011),
      new Node(14, 0, 13, 0b0000000110000),
      new Node(14, 1, 12, 0b000000010111),
      new Node(14, 2, 12, 0b000000010100),
      new Node(14, 3, 13, 0b0000000100111),
      new Node(14, 4, 13, 0b0000000100100),
      new Node(14, 5, 13, 0b0000000100011),
      new Node(14, 6, 15, 0b000000000110101),
      new Node(14, 7, 14, 0b00000000010101),
      new Node(14, 8, 14, 0b00000000010000),
      new Node(14, 9, 17, 0b00000000000010111),
      new Node(14, 10, 15, 0b000000000001101),
      new Node(14, 11, 15, 0b000000000001010),
      new Node(14, 12, 15, 0b000000000000110),
      new Node(14, 13, 17, 0b00000000000000001),
      new Node(14, 14, 16, 0b0000000000000100),
      new Node(14, 15, 16, 0b0000000000000010),
      new Node(15, 0, 12, 0b000000010000),
      new Node(15, 1, 12, 0b000000001111),
      new Node(15, 2, 13, 0b0000000010001),
      new Node(15, 3, 14, 0b00000000011011),
      new Node(15, 4, 14, 0b00000000011001),
      new Node(15, 5, 14, 0b00000000010100),
      new Node(15, 6, 15, 0b000000000011101),
      new Node(15, 7, 14, 0b00000000001011),
      new Node(15, 8, 15, 0b000000000010001),
      new Node(15, 9, 15, 0b000000000001100),
      new Node(15, 10, 16, 0b0000000000010000),
      new Node(15, 11, 16, 0b0000000000001000),
      new Node(15, 12, 19, 0b0000000000000000001),
      new Node(15, 13, 18, 0b000000000000000001),
      new Node(15, 14, 19, 0b0000000000000000000),
      new Node(15, 15, 16, 0b0000000000000001)
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
