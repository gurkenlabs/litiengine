package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class Mp3OutputTest {

    @Test
    void testOutputToFile() throws Exception {
        var sound = Resources.sounds().get("de/gurkenlabs/litiengine/resources/sample.mp3");
        assertNotNull(sound);
        
        byte[] data = sound.getStreamData();
        
        // Quick stats
        int samples = data.length / 2;
        int nonZero = 0;
        long sum = 0;
        int firstNonZero = -1;
        
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < samples; i++) {
            short s = buf.getShort();
            if (s != 0) {
                if (firstNonZero == -1) firstNonZero = i;
                nonZero++;
                sum += s;
            }
        }
        
        double avg = nonZero > 0 ? (double) sum / nonZero : 0;
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== MP3 Decoder Test Results ===\n");
        sb.append("Total samples: ").append(samples).append("\n");
        sb.append("Non-zero samples: ").append(nonZero).append(" (").append(String.format("%.1f", 100.0*nonZero/samples)).append("%)\n");
        sb.append("First non-zero at: ").append(firstNonZero).append("\n");
        sb.append("Average (non-zero): ").append(String.format("%.2f", avg)).append("\n");
        
        // Compare with reference
        byte[] refData = decodeWithMp3Spi();
        int refSamples = refData.length / 2;
        int refNonZero = 0;
        ByteBuffer refBuf = ByteBuffer.wrap(refData).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < refSamples; i++) {
            if (refBuf.getShort() != 0) refNonZero++;
        }
        
        sb.append("\n=== Reference (mp3spi) ===\n");
        sb.append("Total samples: ").append(refSamples).append("\n");
        sb.append("Non-zero samples: ").append(refNonZero).append(" (").append(String.format("%.1f", 100.0*refNonZero/refSamples)).append("%)\n");
        
        // Full comparison
        int minSamples = Math.min(samples, refSamples);
        buf.rewind();
        refBuf.rewind();
        int matchCount = 0;
        for (int i = 0; i < minSamples; i++) {
            if (buf.getShort() == refBuf.getShort()) {
                matchCount++;
            }
        }
        
        sb.append("\n=== Full comparison ===\n");
        sb.append("Total samples compared: ").append(minSamples).append("\n");
        sb.append("Matching: ").append(matchCount).append(" (").append(String.format("%.2f", 100.0*matchCount/minSamples)).append("%)\n");
        
        // First 20 samples details
        sb.append("\n=== First 20 samples ===\n");
        buf.rewind();
        refBuf.rewind();
        for (int i = 0; i < 20; i++) {
            sb.append("Sample ").append(i).append(": ref=").append(refBuf.getShort()).append(" our=").append(buf.getShort()).append("\n");
        }
        
        // Show around first non-zero in reference
        sb.append("\n=== Reference first non-zero ===\n");
        refBuf.rewind();
        int refFirstNonZero = -1;
        for (int i = 0; i < refSamples; i++) {
            if (refBuf.getShort() != 0) {
                refFirstNonZero = i;
                break;
            }
        }
        sb.append("Reference first non-zero at: ").append(refFirstNonZero).append("\n");
        
        // Show around both first non-zero
        sb.append("\n=== Around first reference non-zero ===\n");
        buf.rewind(); refBuf.rewind();
        int start = Math.max(0, refFirstNonZero - 5);
        buf.position(start * 2);
        refBuf.position(start * 2);
        for (int i = start; i < Math.min(refSamples, refFirstNonZero + 15); i++) {
            sb.append("Sample ").append(i).append(": ref=").append(refBuf.getShort()).append(" our=").append(buf.getShort()).append("\n");
        }
        
        // Show more samples from reference to see the actual waveform
        sb.append("\n=== More reference samples 0-50 ===\n");
        refBuf.rewind();
        for (int i = 0; i < 50; i++) {
            short val = refBuf.getShort();
            if (val != 0) {
                sb.append("Sample ").append(i).append(": ").append(val).append("\n");
            }
        }
        
        // Show our samples at the same positions
        sb.append("\n=== More our samples 0-50 ===\n");
        buf.rewind();
        for (int i = 0; i < 50; i++) {
            short val = buf.getShort();
            if (val != 0) {
                sb.append("Sample ").append(i).append(": ").append(val).append("\n");
            }
        }
        
        // Show position 2712 in hex
        sb.append("\n=== Sample 2712 in hex ===\n");
        buf.rewind(); refBuf.rewind();
        buf.position(2712 * 2);
        refBuf.position(2712 * 2);
        short ourVal = buf.getShort();
        short refVal = refBuf.getShort();
        sb.append("Our: ").append(ourVal).append(" (0x").append(Integer.toHexString(ourVal & 0xFFFF)).append(")\n");
        sb.append("Ref: ").append(refVal).append(" (0x").append(Integer.toHexString(refVal & 0xFFFF)).append(")\n");
        
        Path resultsPath = Path.of("build", "mp3-test-results.txt");
        Files.createDirectories(resultsPath.getParent());
        Files.writeString(resultsPath, sb.toString());
        System.out.println("Results written to " + resultsPath.toAbsolutePath());
    }
    
    private byte[] decodeWithMp3Spi() throws Exception {
        var mp3Stream = Resources.getLocation("de/gurkenlabs/litiengine/resources/sample.mp3").openStream();
        byte[] mp3Data = mp3Stream.readAllBytes();
        mp3Stream.close();
        
        var bais = new java.io.ByteArrayInputStream(mp3Data);
        var mp3Stream2 = javax.sound.sampled.AudioSystem.getAudioInputStream(bais);
        var baseFormat = mp3Stream2.getFormat();
        var decodedFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
            baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
        var pcmStream = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, mp3Stream2);
        byte[] pcmData = pcmStream.readAllBytes();
        pcmStream.close();
        mp3Stream2.close();
        return pcmData;
    }
}