package de.gurkenlabs.litiengine.sound.spi.mp3;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.nio.ByteBuffer;
import java.util.Map;

final class Mpeg {
  public static final String VERSION_1_0 = "1.0";
  public static final String VERSION_2_0 = "2.0";
  public static final String VERSION_2_5 = "2.5";
  public static final String LAYER_1 = "I";
  public static final String LAYER_2 = "II";
  public static final String LAYER_3 = "III";
  public static final String CHANNEL_MODE_MONO = "Mono";
  public static final String CHANNEL_MODE_DUAL_MONO = "Dual mono";
  public static final String CHANNEL_MODE_JOINT_STEREO = "Joint stereo";
  public static final String CHANNEL_MODE_STEREO = "Stereo";
  public static final String MODE_EXTENSION_BANDS_4_31 = "Bands 4-31";
  public static final String MODE_EXTENSION_BANDS_8_31 = "Bands 8-31";
  public static final String MODE_EXTENSION_BANDS_12_31 = "Bands 12-31";
  public static final String MODE_EXTENSION_BANDS_16_31 = "Bands 16-31";
  public static final String MODE_EXTENSION_NONE = "None";
  public static final String MODE_EXTENSION_INTENSITY_STEREO = "Intensity stereo";
  public static final String MODE_EXTENSION_M_S_STEREO = "M/S stereo";
  public static final String MODE_EXTENSION_INTENSITY_M_S_STEREO = "Intensity & M/S stereo";
  public static final String MODE_EXTENSION_NA = "n/a";

  public static final Map<String, String[]> MODE_EXTENSIONS = Map.of(
    LAYER_1, new String[]{MODE_EXTENSION_BANDS_4_31, MODE_EXTENSION_BANDS_8_31, MODE_EXTENSION_BANDS_12_31, MODE_EXTENSION_BANDS_16_31},
    LAYER_2, new String[]{MODE_EXTENSION_BANDS_4_31, MODE_EXTENSION_BANDS_8_31, MODE_EXTENSION_BANDS_12_31, MODE_EXTENSION_BANDS_16_31},
    LAYER_3, new String[]{MODE_EXTENSION_NONE, MODE_EXTENSION_INTENSITY_STEREO, MODE_EXTENSION_M_S_STEREO, MODE_EXTENSION_INTENSITY_M_S_STEREO});

  public static final String EMPHASIS_NONE = "None";
  public static final String EMPHASIS__50_15_MS = "50/15 ms";
  public static final String EMPHASIS_CCITT_J_17 = "CCITT J.17";
  public static final String[] EMPHASIS = new String[]{EMPHASIS_NONE, EMPHASIS__50_15_MS, EMPHASIS_CCITT_J_17};

  public static final Map<String, Integer[]> SAMPLERATES = Map.of(
    VERSION_1_0, new Integer[]{44100, 48000, 32000},
    VERSION_2_0, new Integer[]{22050, 24000, 16000},
    VERSION_2_5, new Integer[]{11025, 12000, 8000});

  public static final Map<String, Integer[]> BITRATES_VERSION_1_0 = Map.of(
    LAYER_1, new Integer[]{32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448},
    LAYER_2, new Integer[]{32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384},
    LAYER_3, new Integer[]{32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320});

  public static final Map<String, Integer[]> BITRATES_VERSION_2_X = Map.of(
    LAYER_1, new Integer[]{32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256},
    LAYER_2, new Integer[]{8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160},
    LAYER_3, new Integer[]{8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160});

  public static final Map<String, Float> TIME_PER_FRAME = Map.of(
    LAYER_1, 384f,
    LAYER_2, 1152f,
    LAYER_3, 1152f);

  static final String ID3V2_TAG = "ID3";

  private static final int ID3V2_TAG_HEADER_LENGTH = 10;

  static final int ID3V2_TAG_DATA_OFFSET_OFFSET = 6;

  private Mpeg() {
  }

  static String getVersion(int versionRaw) throws UnsupportedAudioFileException {
    return switch (versionRaw) {
      case 3 -> VERSION_1_0;
      case 2 -> VERSION_2_0;
      case 0 -> VERSION_2_5;
      default -> throw new UnsupportedAudioFileException("Invalid mpeg audio version in frame header");
    };
  }

  static String getLayer(int layerRaw) throws UnsupportedAudioFileException {
    return switch (layerRaw) {
      case 1 -> LAYER_3;
      case 2 -> LAYER_2;
      case 3 -> LAYER_1;
      default -> throw new UnsupportedAudioFileException("Invalid mpeg layer description in frame header");
    };
  }

  static AudioFormat.Encoding getEncoding(String version, String layer) {
    return new AudioFormat.Encoding("MPEG" + version + "L" + layer);
  }

  static int getBitRate(int bitRateRaw, String version, String layer) throws UnsupportedAudioFileException {
    if (bitRateRaw < 0 || bitRateRaw > BITRATES_VERSION_1_0.get(LAYER_1).length) {
      throw new UnsupportedAudioFileException("Invalid bitrate in frame header");
    }

    var bitRateIndex = bitRateRaw - 1;
    if (version.equals(VERSION_1_0)) {
      return BITRATES_VERSION_1_0.get(layer)[bitRateIndex];
    }

    return BITRATES_VERSION_2_X.get(layer)[bitRateIndex];
  }

  static int getSampleRate(int sampleRateRaw, String version) throws UnsupportedAudioFileException {
    var sampleRates = SAMPLERATES.getOrDefault(version, null);
    if (sampleRates == null || sampleRateRaw < 0 || sampleRateRaw > sampleRates.length - 1) {
      throw new UnsupportedAudioFileException("Invalid sample rate in frame header");
    }

    return sampleRates[sampleRateRaw];
  }

  static float getFrameRate(String version, String layer, int sampleRate) {
    var tpf = TIME_PER_FRAME.get(layer) / sampleRate;
    if ((version.equals(VERSION_2_0) || (version.equals(VERSION_2_5)))) {
      tpf /= 2;
    }

    return 1.0f / tpf;
  }

  static String getChannelMode(int channelModeRaw) throws UnsupportedAudioFileException {

    return switch (channelModeRaw) {
      case 0 -> CHANNEL_MODE_STEREO;
      case 1 -> CHANNEL_MODE_JOINT_STEREO;
      case 2 -> CHANNEL_MODE_DUAL_MONO;
      case 3 -> CHANNEL_MODE_MONO;
      default -> throw new UnsupportedAudioFileException("Invalid channel mode in frame header");
    };
  }

  static String getModeExtension(int modeExtensionRaw, String layer, String channelMode) throws UnsupportedAudioFileException {
    if (!CHANNEL_MODE_JOINT_STEREO.equals(channelMode)) {
      return MODE_EXTENSION_NA;
    }

    var extension = MODE_EXTENSIONS.getOrDefault(layer, null);
    if (extension == null || modeExtensionRaw < 0 || modeExtensionRaw > extension.length - 1) {
      throw new UnsupportedAudioFileException("Invalid mode extension in frame header");
    }

    return extension[modeExtensionRaw];
  }

  static String getEmphasis(int emphasisRaw) throws UnsupportedAudioFileException {
    if (emphasisRaw < 0 || emphasisRaw > EMPHASIS.length - 1) {
      throw new UnsupportedAudioFileException("Invalid emphasis in frame header");
    }

    return EMPHASIS[emphasisRaw];
  }


  static int getDataOffset(ByteBuffer byteBuffer) {
    return ID3V2_TAG_HEADER_LENGTH + byteBuffer.getInt(ID3V2_TAG_DATA_OFFSET_OFFSET);
  }

  static boolean isStart(byte b1, byte b2) {
    // check for FRAME_SYNC bytes (11111111 11100000)
    return b1 == (byte) 0b11111111 && (b2 & (byte) 0xE0) == (byte) 0xE0;
  }
}
