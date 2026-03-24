import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

public class Mp3ComparisonTest {
    public static void main(String[] args) throws Exception {
        String mp3File = "litiengine/src/test/resources/de/gurkenlabs/litiengine/resources/sample.mp3";
        
        // Test 1: Decode with mp3spi (Java's built-in MP3 SPI)
        System.out.println("=== Testing with mp3spi ===");
        byte[] mp3spiData = decodeWithMp3Spi(mp3File);
        System.out.println("mp3spi decoded " + mp3spiData.length + " bytes");
        
        // Test 2: Decode with LITIENGINE
        System.out.println("\n=== Testing with LITIENGINE ===");
        byte[] litiengineData = decodeWithLitiengine(mp3File);
        System.out.println("LITIENGINE decoded " + litiengineData.length + " bytes");
        
        // Compare the outputs
        System.out.println("\n=== Comparison ===");
        System.out.println("mp3spi bytes: " + mp3spiData.length);
        System.out.println("LITIENGINE bytes: " + litiengineData.length);
        
        if (mp3spiData.length == litiengineData.length) {
            System.out.println("Both decoders produced the same number of bytes");
            
            // Compare sample by sample
            int differences = 0;
            int maxDiff = 0;
            for (int i = 0; i < mp3spiData.length; i++) {
                int diff = Math.abs(mp3spiData[i] - litiengineData[i]);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
                if (diff > 10) { // Allow small differences due to rounding
                    differences++;
                }
            }
            System.out.println("Differences: " + differences + " out of " + mp3spiData.length);
            System.out.println("Max difference: " + maxDiff);
            
            if (differences == 0) {
                System.out.println("✓ Outputs are identical!");
            } else {
                System.out.println("✗ Outputs differ");
                
                // Show first few differences
                System.out.println("\nFirst 10 differences:");
                int count = 0;
                for (int i = 0; i < mp3spiData.length && count < 10; i++) {
                    int diff = Math.abs(mp3spiData[i] - litiengineData[i]);
                    if (diff > 10) {
                        System.out.println("  Byte " + i + ": mp3spi=" + mp3spiData[i] + ", LITIENGINE=" + litiengineData[i] + ", diff=" + diff);
                        count++;
                    }
                }
            }
        } else {
            System.out.println("✗ Different number of bytes produced");
            
            // Compare the common portion
            int minLength = Math.min(mp3spiData.length, litiengineData.length);
            int differences = 0;
            for (int i = 0; i < minLength; i++) {
                if (mp3spiData[i] != litiengineData[i]) {
                    differences++;
                }
            }
            System.out.println("Differences in common portion: " + differences + " out of " + minLength);
        }
        
        // Save both outputs to files for manual inspection
        Files.write(Paths.get("mp3spi_output.pcm"), mp3spiData);
        Files.write(Paths.get("litiengine_output.pcm"), litiengineData);
        System.out.println("\nSaved outputs to mp3spi_output.pcm and litiengine_output.pcm");
    }
    
    private static byte[] decodeWithMp3Spi(String mp3File) throws Exception {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(mp3File));
        AudioFormat sourceFormat = audioInputStream.getFormat();
        System.out.println("Source format: " + sourceFormat);
        
        // Convert to PCM 16-bit stereo
        AudioFormat targetFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sourceFormat.getSampleRate(),
            16,
            sourceFormat.getChannels(),
            2,
            sourceFormat.getSampleRate(),
            false
        );
        
        AudioInputStream pcmStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
        
        // Read all bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = pcmStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        
        audioInputStream.close();
        pcmStream.close();
        
        return baos.toByteArray();
    }
    
    private static byte[] decodeWithLitiengine(String mp3File) throws Exception {
        // Use the LITIENGINE Mp3FileReader
        // This would require adding the LITIENGINE jar to the classpath
        // For now, we'll just return empty bytes
        System.out.println("LITIENGINE decoding not available in this test");
        return new byte[0];
    }
}
