package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Mp3DecoderComparisonTest {
  private static final String MP3_RESOURCE = "de/gurkenlabs/litiengine/resources/sample.mp3";

  @Test
  void decodedPcmMatchesGoldenOutput() throws Exception {
    byte[] pcm = decodeWithLitiengine(readSample(), false);

    assertEquals(963072, pcm.length);
    assertEquals("4360f659134fd1a9641449da6982527726e32ed2cd641a399eb4c51a78323c8a",
      HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(pcm)));
  }

  @Test
  void decodedPcmMatchesIndependentReferenceCheckpoints() throws Exception {
    // Generated from the committed MP3 with JLayer 1.0.1.4, which can differ by one due to rounding.
    // Offsets exclude the initial 1152-sample Xing metadata frame.
    int[][] referenceSamples = {
      {15232, -8051}, {31616, 32}, {48000, -7018}, {64384, 284},
      {80768, 1002}, {97152, -140}, {113536, -684}, {129920, -1977}, {146304, -435},
      {162688, 6499}, {179072, 1001}, {195456, 33}, {211840, -1518}, {228224, -4100},
      {244608, 9}, {260992, -128}, {277376, 1}, {293760, 10394}, {310144, -603},
      {326528, -4345}, {342912, 26}, {359296, -1205}, {375680, 52}, {392064, -217},
      {408448, -403}, {424832, -6170}, {441216, -1591}, {457600, -9}, {473984, 623}
    };
    byte[] pcm = decodeWithLitiengine(readSample(), false);

    for (int[] reference : referenceSamples) {
      int byteOffset = reference[0] * 2;
      short actual = (short) ((pcm[byteOffset] & 0xff) | (pcm[byteOffset + 1] << 8));
      assertEquals(reference[1], actual, 1, "PCM sample " + reference[0]);
    }
  }

  @Test
  void bigEndianOutputContainsTheSameSamples() throws Exception {
    byte[] littleEndian = decodeWithLitiengine(readSample(), false);
    byte[] bigEndian = decodeWithLitiengine(readSample(), true);

    assertEquals(littleEndian.length, bigEndian.length);
    for (int i = 0; i < littleEndian.length; i += 2) {
      assertEquals(littleEndian[i], bigEndian[i + 1]);
      assertEquals(littleEndian[i + 1], bigEndian[i]);
    }
  }

  @Test
  void largeCallerBuffersProduceTheSamePcm() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false), decodeWithLitiengine(mp3, false, 600_000));
  }

  @Test
  void creatingAConversionDoesNotEagerlyConsumeTheEncodedStream() throws Exception {
    var encoded = new CountingInputStream(new ByteArrayInputStream(readSample()));
    try (var source = new Mp3FileReader().getAudioInputStream(encoded)) {
      int readsAfterFormatDetection = encoded.bytesRead;
      try (var decoded = new Mp3FormatConversionProvider()
        .getAudioInputStream(targetFormat(source.getFormat(), false), source)) {
        assertEquals(readsAfterFormatDetection, encoded.bytesRead);

        assertEquals(2, decoded.read(new byte[2]));
        assertTrue(encoded.bytesRead > readsAfterFormatDetection);
      }
    }
  }

  @Test
  void decodedStreamEnforcesPcmFrameSemantics() throws Exception {
    try (var source = new Mp3FileReader().getAudioInputStream(new ByteArrayInputStream(readSample()));
      var decoded = new Mp3FormatConversionProvider().getAudioInputStream(targetFormat(source.getFormat(), false), source)) {
      byte[] buffer = new byte[5];

      assertEquals(2, decoded.getFormat().getFrameSize());
      assertEquals(0, decoded.read(buffer, 0, 1));
      assertEquals(2, decoded.read(buffer, 0, 3));
      assertEquals(4, decoded.read(buffer, 0, 5));
      assertFalse(decoded.markSupported());
      assertThrows(IOException.class, decoded::reset);
    }
  }

  @Test
  void skipAdvancesByCompletePcmFrames() throws Exception {
    byte[] expected = decodeWithLitiengine(readSample(), false);
    try (var source = new Mp3FileReader().getAudioInputStream(new ByteArrayInputStream(readSample()));
      var decoded = new Mp3FormatConversionProvider().getAudioInputStream(targetFormat(source.getFormat(), false), source)) {
      assertEquals(2, decoded.skip(3));
      assertArrayEquals(Arrays.copyOfRange(expected, 2, expected.length), decoded.readAllBytes());
    }
  }

  @Test
  void id3v24FooterDoesNotChangeDecodedPcm() throws Exception {
    byte[] mp3 = readSample();
    byte[] untagged = Arrays.copyOfRange(mp3, Mpeg.getId3TagLength(mp3), mp3.length);

    assertArrayEquals(decodeWithLitiengine(untagged, false),
      decodeWithLitiengine(withId3v24Footer(untagged), false));
  }

  @Test
  void trailingApev2HeaderAndFooterDoNotPreventCompleteDecoding() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false),
      decodeWithLitiengine(withTrailingMetadata(mp3, apev2HeaderAndFooter()), false));
  }

  @Test
  void trailingReadOnlyApev2HeaderAndFooterDoNotPreventCompleteDecoding() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false),
      decodeWithLitiengine(withTrailingMetadata(mp3, apev2HeaderAndFooter(1)), false));
  }

  @Test
  void trailingApev2ItemsAndFooterDoNotPreventCompleteDecoding() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false),
      decodeWithLitiengine(withTrailingMetadata(mp3, apev2ItemsAndFooter()), false));
  }

  @Test
  void trailingReadOnlyApev2FooterDoesNotPreventCompleteDecoding() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false),
      decodeWithLitiengine(withTrailingMetadata(mp3, apev2Descriptor(32, 0, 1)), false));
  }

  @Test
  void trailingApev2FooterBeforeId3v1DoesNotPreventCompleteDecoding() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false),
      decodeWithLitiengine(withTrailingMetadata(mp3, apev2FooterAndId3v1()), false));
  }

  @Test
  void appendedId3v24TagDoesNotPreventCompleteDecoding() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false),
      decodeWithLitiengine(withTrailingMetadata(mp3, appendedId3v24()), false));
  }

  @Test
  void appendedId3v24BeforeId3v1DoesNotPreventCompleteDecoding() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false),
      decodeWithLitiengine(withTrailingMetadata(mp3, appendedId3v24(), id3v1()), false));
  }

  @Test
  void appendedId3v24BeforeApev2AndId3v1DoesNotPreventCompleteDecoding() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false),
      decodeWithLitiengine(withTrailingMetadata(mp3, appendedId3v24(), apev2Descriptor(32, 0, 0), id3v1()), false));
  }

  @Test
  void trailingId3v1TagDoesNotPreventCompleteDecoding() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false),
      decodeWithLitiengine(withTrailingMetadata(mp3, id3v1()), false));
  }

  @Test
  void malformedTrailingId3IsRejected() throws Exception {
    byte[] mp3 = readSample();
    byte[] malformedId3 = {'I', 'D', '3', 'X'};

    assertThrows(IOException.class,
      () -> decodeWithLitiengine(withTrailingMetadata(mp3, malformedId3), false));
  }

  @Test
  void truncatedId3v1IsRejected() throws Exception {
    byte[] mp3 = readSample();
    byte[] truncatedId3v1 = {'T', 'A', 'G', 'X'};

    assertThrows(IOException.class,
      () -> decodeWithLitiengine(withTrailingMetadata(mp3, truncatedId3v1), false));
  }

  @Test
  void truncatedId3v2PayloadIsRejected() throws Exception {
    byte[] mp3 = readSample();
    byte[] truncatedId3v24 = {'I', 'D', '3', 4, 0, 0, 0, 0, 0, 4};

    assertThrows(IOException.class,
      () -> decodeWithLitiengine(withTrailingMetadata(mp3, truncatedId3v24), false));
  }

  @Test
  void unrecognizedTrailingDataIsRejected() throws Exception {
    byte[] mp3 = readSample();
    byte[] trailingData = {'N', 'O', 'P', 'E'};

    assertThrows(IOException.class,
      () -> decodeWithLitiengine(withTrailingMetadata(mp3, trailingData), false));
  }

  @Test
  void apev2FooterSizeMustCoverTheEntireTrailingTag() throws Exception {
    byte[] mp3 = readSample();
    var malformedTag = new ByteArrayOutputStream();
    malformedTag.write("NOPE".getBytes(StandardCharsets.US_ASCII));
    malformedTag.write(apev2Descriptor(32, 0, 0));

    assertThrows(IOException.class,
      () -> decodeWithLitiengine(withTrailingMetadata(mp3, malformedTag.toByteArray()), false));
  }

  @Test
  void vbriMetadataFrameDoesNotProducePcm() throws Exception {
    byte[] mp3 = readSample();
    byte[] withVbri = Arrays.copyOf(mp3, mp3.length);
    int firstFrameOffset = Mpeg.getId3TagLength(withVbri);
    Arrays.fill(withVbri, firstFrameOffset + 21, firstFrameOffset + 25, (byte) 0);
    System.arraycopy("VBRI".getBytes(StandardCharsets.ISO_8859_1), 0, withVbri, firstFrameOffset + 36, 4);

    assertArrayEquals(decodeWithLitiengine(mp3, false), decodeWithLitiengine(withVbri, false));
  }

  @Test
  void malformedLaterFramesFailTheDecodedStream() throws Exception {
    byte[] corrupted = readSample();
    int firstFrameOffset = Mpeg.getId3TagLength(corrupted);
    int nextFrameOffset = firstFrameOffset + frameLength(corrupted, firstFrameOffset);
    corrupted[nextFrameOffset] = 0;

    try (var source = new Mp3FileReader().getAudioInputStream(new ByteArrayInputStream(corrupted));
      var decoded = new Mp3FormatConversionProvider().getAudioInputStream(targetFormat(source.getFormat(), false), source)) {
      assertThrows(IOException.class, decoded::readAllBytes);
    }
  }

  @Test
  void sampleRateChangesBetweenFramesAreRejected() throws Exception {
    byte[] changed = readSample();
    int nextFrameOffset = nextFrameOffset(changed);
    changed[nextFrameOffset + 2] = (byte) ((changed[nextFrameOffset + 2] & ~0x0c) | 0x04);

    assertFormatChangeRejected(changed, nextFrameOffset);
  }

  @Test
  void channelChangesBetweenFramesAreRejected() throws Exception {
    byte[] changed = readSample();
    int nextFrameOffset = nextFrameOffset(changed);
    changed[nextFrameOffset + 3] &= 0x3f;

    assertFormatChangeRejected(changed, nextFrameOffset);
  }

  @Test
  void conversionPreservesRateAndChannels() throws Exception {
    try (var source = new Mp3FileReader().getAudioInputStream(new ByteArrayInputStream(readSample()))) {
      var provider = new Mp3FormatConversionProvider();
      var formats = provider.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, source.getFormat());

      assertEquals(2, formats.length);
      assertEquals(source.getFormat().getSampleRate(), formats[0].getSampleRate());
      assertEquals(source.getFormat().getChannels(), formats[0].getChannels());

      var stereo = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, source.getFormat().getSampleRate(),
        16, 2, 4, source.getFormat().getSampleRate(), false);
      assertThrows(IllegalArgumentException.class, () -> provider.getAudioInputStream(stereo, source));
    }
  }

  private static byte[] decodeWithLitiengine(byte[] mp3Data, boolean bigEndian) throws Exception {
    try (AudioInputStream source = new Mp3FileReader()
      .getAudioInputStream(new ByteArrayInputStream(mp3Data))) {
      var target = targetFormat(source.getFormat(), bigEndian);
      try (var decoded = new Mp3FormatConversionProvider().getAudioInputStream(target, source)) {
        return decoded.readAllBytes();
      }
    }
  }

  private static byte[] decodeWithLitiengine(byte[] mp3Data, boolean bigEndian, int bufferSize) throws Exception {
    try (AudioInputStream source = new Mp3FileReader()
      .getAudioInputStream(new ByteArrayInputStream(mp3Data))) {
      var target = targetFormat(source.getFormat(), bigEndian);
      try (var decoded = new Mp3FormatConversionProvider().getAudioInputStream(target, source);
        var output = new ByteArrayOutputStream()) {
        byte[] buffer = new byte[bufferSize];
        int read;
        while ((read = decoded.read(buffer)) != -1) output.write(buffer, 0, read);
        return output.toByteArray();
      }
    }
  }

  private static AudioFormat targetFormat(AudioFormat source, boolean bigEndian) {
    return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, source.getSampleRate(), 16,
      source.getChannels(), source.getChannels() * 2, source.getSampleRate(), bigEndian);
  }

  private static void assertFormatChangeRejected(byte[] mp3, int frameOffset) throws Exception {
    try (var source = new Mp3FileReader().getAudioInputStream(new ByteArrayInputStream(mp3));
      var decoded = new Mp3FormatConversionProvider().getAudioInputStream(targetFormat(source.getFormat(), false), source)) {
      var exception = assertThrows(IOException.class, decoded::readAllBytes);
      assertEquals("MPEG stream format changed at byte " + frameOffset, exception.getMessage());
    }
  }

  private static int nextFrameOffset(byte[] mp3) throws UnsupportedAudioFileException {
    int firstFrameOffset = Mpeg.getId3TagLength(mp3);
    return firstFrameOffset + frameLength(mp3, firstFrameOffset);
  }

  private static int frameLength(byte[] mp3, int offset) {
    int header = ((mp3[offset] & 0xff) << 24) | ((mp3[offset + 1] & 0xff) << 16)
      | ((mp3[offset + 2] & 0xff) << 8) | (mp3[offset + 3] & 0xff);
    int bitrate = Mpeg.BITRATES_VERSION_1_0_LAYER_3[((header >>> 12) & 0xf) - 1];
    int sampleRate = Mpeg.SAMPLERATES_VERSION_1_0[(header >>> 10) & 0x3];
    return 144000 * bitrate / sampleRate + ((header >>> 9) & 1);
  }

  private static byte[] withId3v24Footer(byte[] mp3) throws IOException {
    var output = new ByteArrayOutputStream();
    output.write(new byte[]{'I', 'D', '3', 4, 0, 0x10, 0, 0, 0, 0});
    output.write(new byte[]{'3', 'D', 'I', 4, 0, 0x10, 0, 0, 0, 0});
    output.write(mp3);
    return output.toByteArray();
  }

  private static byte[] withTrailingMetadata(byte[] mp3, byte[]... metadata) throws IOException {
    var output = new ByteArrayOutputStream();
    output.write(mp3);
    for (byte[] tag : metadata) output.write(tag);
    return output.toByteArray();
  }

  private static byte[] appendedId3v24() throws IOException {
    var output = new ByteArrayOutputStream();
    output.write(new byte[]{'I', 'D', '3', 4, 0, 0x10, 0, 0, 0, 4});
    output.write(new byte[4]);
    output.write(new byte[]{'3', 'D', 'I', 4, 0, 0x10, 0, 0, 0, 4});
    return output.toByteArray();
  }

  private static byte[] id3v1() {
    byte[] tag = new byte[128];
    System.arraycopy("TAG".getBytes(StandardCharsets.US_ASCII), 0, tag, 0, 3);
    return tag;
  }

  private static byte[] apev2HeaderAndFooter() throws IOException {
    return apev2HeaderAndFooter(0);
  }

  private static byte[] apev2HeaderAndFooter(int flags) throws IOException {
    var output = new ByteArrayOutputStream();
    output.write(apev2Descriptor(32, 0, 0xa0000000 | flags));
    output.write(apev2Descriptor(32, 0, 0x80000000 | flags));
    return output.toByteArray();
  }

  private static byte[] apev2ItemsAndFooter() throws IOException {
    var item = new ByteArrayOutputStream();
    item.write(new byte[]{4, 0, 0, 0, 0, 0, 0, 0});
    item.write("Title\0Test".getBytes(StandardCharsets.UTF_8));

    var output = new ByteArrayOutputStream();
    output.write(item.toByteArray());
    output.write(apev2Descriptor(32 + item.size(), 1, 0));
    return output.toByteArray();
  }

  private static byte[] apev2FooterAndId3v1() throws IOException {
    var output = new ByteArrayOutputStream();
    output.write(apev2Descriptor(32, 0, 0));
    output.write(id3v1());
    return output.toByteArray();
  }

  private static byte[] apev2Descriptor(int tagSize, int itemCount, int flags) {
    byte[] descriptor = new byte[32];
    System.arraycopy("APETAGEX".getBytes(StandardCharsets.US_ASCII), 0, descriptor, 0, 8);
    writeLittleEndianInt(descriptor, 8, 2000);
    writeLittleEndianInt(descriptor, 12, tagSize);
    writeLittleEndianInt(descriptor, 16, itemCount);
    writeLittleEndianInt(descriptor, 20, flags);
    return descriptor;
  }

  private static void writeLittleEndianInt(byte[] data, int offset, int value) {
    data[offset] = (byte) value;
    data[offset + 1] = (byte) (value >>> 8);
    data[offset + 2] = (byte) (value >>> 16);
    data[offset + 3] = (byte) (value >>> 24);
  }

  private static byte[] readSample() throws IOException {
    try (var stream = Resources.getLocation(MP3_RESOURCE).openStream()) {
      return stream.readAllBytes();
    }
  }

  private static final class CountingInputStream extends FilterInputStream {
    private int bytesRead;

    private CountingInputStream(InputStream input) {
      super(input);
    }

    @Override
    public int read() throws IOException {
      int value = super.read();
      if (value != -1) bytesRead++;
      return value;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
      int read = super.read(buffer, offset, length);
      if (read > 0) bytesRead += read;
      return read;
    }
  }
}
