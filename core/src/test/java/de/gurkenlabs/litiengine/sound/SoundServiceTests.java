package de.gurkenlabs.litiengine.sound;

import javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import javazoom.spi.vorbis.sampled.convert.VorbisFormatConversionProvider;
import javazoom.spi.vorbis.sampled.file.VorbisAudioFileReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.FormatConversionProvider;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class SoundServiceTests {

    @Test
    public void testAudioFileReaderServicesPresent() {
        List<AudioFileReader> audioFileReaders = ServiceLoader
                .load(AudioFileReader.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        Assertions.assertTrue(containsOfType(audioFileReaders, MpegAudioFileReader.class));
        Assertions.assertTrue(containsOfType(audioFileReaders, VorbisAudioFileReader.class));
    }

    @Test
    public void testAudioFormatConversionProviderServicesPresent() {
        List<FormatConversionProvider> formatConversionProviders = ServiceLoader
                .load(FormatConversionProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        Assertions.assertTrue(containsOfType(formatConversionProviders, MpegFormatConversionProvider.class));
        Assertions.assertTrue(containsOfType(formatConversionProviders, VorbisFormatConversionProvider.class));
    }

    private <T, U extends T> boolean containsOfType(List<T> list, Class<U> type) {
        return list.stream().anyMatch(type::isInstance);
    }
}
