import javax.sound.sampled.*;
import java.io.*;
import java.nio.*;

public class CompareDebug {
    public static void main(String[] args) throws Exception {
        String mp3File = "litiengine/src/test/resources/de/gurkenlabs/litiengine/resources/sample.mp3";
        
        // Decode with reference (mp3spi)
        System.out.println("=== Reference (mp3spi) ===");
        byte[] refData = decodeWithMp3Spi(mp3File);
        System.out.println("Reference: " + refData.length + " bytes (" + (refData.length/2) + " samples)");
        
        // Find first non-zero in reference
        ByteBuffer refBuf = ByteBuffer.wrap(refData).order(ByteOrder.LITTLE_ENDIAN);
        int refFirstNonZero = -1;
        for (int i = 0; i < refData.length / 2; i++) {
            if (refBuf.getShort() != 0) {
                refFirstNonZero = i;
                break;
            }
        }
        System.out.println("Reference first non-zero: " + refFirstNonZero);
        
        // Show reference samples around first non-zero
        if (refFirstNonZero >= 0) {
            refBuf.position(refFirstNonZero * 2);
            System.out.println("Reference samples " + refFirstNonZero + " to " + (refFirstNonZero + 20) + ":");
            for (int i = 0; i < 20; i++) {
                System.out.println("  " + (refFirstNonZero + i) + ": " + refBuf.getShort());
            }
        }
        
        // Decode with LITIENGINE
        System.out.println("\n=== LITIENGINE ===");
        byte[] litiData = decodeWithLitiengine(mp3File);
        System.out.println("LITIENGINE: " + litiData.length + " bytes (" + (litiData.length/2) + " samples)");
        
        // Find first non-zero in LITIENGINE
        ByteBuffer litiBuf = ByteBuffer.wrap(litiData).order(ByteOrder.LITTLE_ENDIAN);
        int litiFirstNonZero = -1;
        for (int i = 0; i < litiData.length / 2; i++) {
            if (litiBuf.getShort() != 0) {
                litiFirstNonZero = i;
                break;
            }
        }
        System.out.println("LITIENGINE first non-zero: " + litiFirstNonZero);
        
        // Show LITIENGINE samples around first non-zero
        if (litiFirstNonZero >= 0) {
            litiBuf.position(litiFirstNonZero * 2);
            System.out.println("LITIENGINE samples " + litiFirstNonZero + " to " + (litiFirstNonZero + 20) + ":");
            for (int i = 0; i < 20; i++) {
                System.out.println("  " + (litiFirstNonZero + i) + ": " + litiBuf.getShort());
            }
        }
        
        // Compare outputs
        System.out.println("\n=== Comparison ===");
        System.out.println("Sample rate: 32000 Hz");
        System.out.println("Reference first non-zero at: " + refFirstNonZero + " (" + (refFirstNonZero / 32000.0) + " seconds)");
        System.out.println("LITIENGINE first non-zero at: " + litiFirstNonZero + " (" + (litiFirstNonZero / 32000.0) + " seconds)");
        System.out.println("Difference: " + (litiFirstNonZero - refFirstNonZero) + " samples (" + ((litiFirstNonZero - refFirstNonZero) / 32000.0) + " seconds)");
    }
    
    static byte[] decodeWithMp3Spi(String mp3File) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(mp3File));
        AudioFormat base = ais.getFormat();
        AudioFormat decoded = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            base.getSampleRate(),
            16,
            base.getChannels(),
            base.getChannels() * 2,
            base.getSampleRate(),
            false
        );
        AudioInputStream pcm = AudioSystem.getAudioInputStream(decoded, ais);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = pcm.read(buf)) > 0) baos.write(buf, 0, r);
        ais.close();
        pcm.close();
        return baos.toByteArray();
    }
    
    static byte[] decodeWithLitiengine(String mp3File) throws Exception {
        // Use Mp3FileReader directly
        de.gurkenlabs.litiengine.sound.spi.mp3.Mp3FileReader reader = 
            new de.gurkenlabs.litiengine.sound.spi.mp3.Mp3FileReader();
        FileInputStream fis = new FileInputStream(mp3File);
        AudioInputStream ais = reader.getAudioInputStream(fis);
        AudioFormat base = ais.getFormat();
        AudioFormat decoded = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            base.getSampleRate(),
            16,
            base.getChannels(),
            base.getChannels() * 2,
            base.getSampleRate(),
            false
        );
        AudioInputStream pcm = AudioSystem.getAudioInputStream(decoded, ais);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = pcm.read(buf)) > 0) baos.write(buf, 0, r);
        fis.close();
        ais.close();
        pcm.close();
        return baos.toByteArray();
    }
}
