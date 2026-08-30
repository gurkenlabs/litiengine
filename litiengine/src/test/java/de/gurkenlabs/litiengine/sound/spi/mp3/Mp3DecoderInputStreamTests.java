package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Mp3DecoderInputStreamTests {

  @ParameterizedTest
  @CsvSource({
    "Xing, false, false, 36",
    "Info, true,  false, 21",
    "Xing, false, true,  38",
    "Info, true,  true,  23"
  })
  void detectsXingHeadersAtTheMpeg1Layer3Offset(String marker, boolean mono,
    boolean protectedByCrc, int markerOffset) {
    byte[] frame = new byte[42];
    frame[0] = (byte) 0xff;
    frame[1] = (byte) (protectedByCrc ? 0xfa : 0xfb);
    frame[2] = (byte) 0x90;
    frame[3] = mono ? (byte) 0xc0 : 0;
    System.arraycopy(marker.getBytes(StandardCharsets.ISO_8859_1), 0, frame, markerOffset, 4);

    assertTrue(Mp3DecoderInputStream.hasXingHeader(frame));
  }

  @ParameterizedTest
  @CsvSource({"Xing, 21", "Info, 21", "VBRI, 36"})
  void recognizesMetadataFrames(String marker, int markerOffset) {
    byte[] frame = new byte[42];
    frame[0] = (byte) 0xff;
    frame[1] = (byte) 0xfb;
    frame[2] = (byte) 0x90;
    frame[3] = (byte) 0xc0;
    System.arraycopy(marker.getBytes(StandardCharsets.ISO_8859_1), 0, frame, markerOffset, 4);

    assertTrue(Mp3DecoderInputStream.isMetadataFrame(frame));
  }
}
