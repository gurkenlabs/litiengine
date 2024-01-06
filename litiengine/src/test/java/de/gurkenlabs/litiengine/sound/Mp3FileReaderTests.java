package de.gurkenlabs.litiengine.sound;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.spi.mp3.Mp3FileReader;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Mp3FileReaderTests {

  @Test
  void invalidFilesAreRecognized() {
    var fileReader = new Mp3FileReader();
    assertThrows(UnsupportedAudioFileException.class, () -> fileReader.getAudioFileFormat(Resources.getLocation("de/gurkenlabs/litiengine/resources/bop.wav").openStream()));
  }

  @Test
  void testReadFile() throws IOException, UnsupportedAudioFileException, InterruptedException {
    var fileReader = new Mp3FileReader();
    var fileFormat = fileReader.getAudioFileFormat(Resources.getLocation("de/gurkenlabs/litiengine/resources/sample.mp3").openStream());

    assertNotNull(fileFormat);

    var mpegFileReader = new MpegAudioFileReader();
    var mpegFileFormat = mpegFileReader.getAudioFileFormat(Resources.getLocation("de/gurkenlabs/litiengine/resources/sample.mp3"));

    assertNotNull(mpegFileFormat);

    // just to play around
    var sound = Resources.sounds().get("de/gurkenlabs/litiengine/resources/sample.mp3");
    var playback = Game.audio().playSound(sound);
    Thread.sleep(1000);
  }
}
