package de.gurkenlabs.litiengine.util.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

public class FileUtilitiesTests {

  @Test
  public void testCombinePaths() {
    String winPath = "C:\\test\\test2\\";
    String linuxPath = "somepath/123/456/";

    String winPath2 = "test\\test2\\";

    String linuxPath3 = "../somepath/123/456/";
    String winPath3 = "..\\test\\test2\\";
    String somefile = "..\\file.txt";
    String someOtherFile = "file.txt";

    String combined = FileUtilities.combine(winPath, linuxPath);
    String combined2 = FileUtilities.combine(linuxPath, winPath2);
    String combined3 = FileUtilities.combine(linuxPath3, winPath2);
    String combined4 = FileUtilities.combine(winPath3, winPath2);
    String combined5 = FileUtilities.combine(winPath3, winPath2, linuxPath);
    String combined6 = FileUtilities.combine(linuxPath, somefile);
    String combined7 = FileUtilities.combine(linuxPath, someOtherFile);

    assertEquals(new File("C:\\test\\test2\\somepath\\123\\456").toPath().toString(), combined);
    assertEquals(new File("somepath/123/456/test/test2").toPath().toString(), combined2);
    assertEquals(new File("../somepath/123/456/test/test2").toPath().toString(), combined3);
    assertEquals(new File("..\\test\\test2\\test\\test2").toPath().toString(), combined4);
    assertEquals(new File("..\\test\\test2\\test\\test2\\somepath\\123\\456").toPath().toString(), combined5);
    assertEquals(new File("somepath/123/file.txt").toPath().toString(), combined6);
    assertEquals(new File("somepath/123/456/file.txt").toPath().toString(), combined7);
  }

  @Test
  public void testGetFileName() {
    String file1 = "C:\\test\\test2\\test.txt";
    String file2 = "/somepath/123/456/test.txt";
    String file3 = "/somepath/123/456/test";
    String file4 = "/somepath/123/456/";
    String file5 = "/somep.ath/1.23/45.6/test.txt";
    String file6 = "/somep.ath/1.23/45.6/";

    String filename = FileUtilities.getFileName(file1);
    String filename2 = FileUtilities.getFileName(file2);
    String filename3 = FileUtilities.getFileName(file3);
    String filename4 = FileUtilities.getFileName(file4);
    String filename5 = FileUtilities.getFileName(file5);
    String filename6 = FileUtilities.getFileName(file6);

    assertEquals("test", filename);
    assertEquals("test", filename2);
    assertEquals("test", filename3);
    assertEquals("", filename4);
    assertEquals("test", filename5);
    assertEquals("", filename6);
  }

  @Test
  public void testGetExtension() {
    String file1 = "C:\\test\\test2\\test.txt";
    String file2 = "/somepath/123/456/test.123456789";
    String file3 = "/somepath/123/456/test";
    String file4 = "/somepath/123/456/";
    String file5 = "/somepath/1.23/4.56/";

    String extension = FileUtilities.getExtension(file1);
    String extension2 = FileUtilities.getExtension(file2);
    String extension3 = FileUtilities.getExtension(file3);
    String extension4 = FileUtilities.getExtension(file4);
    String extension5 = FileUtilities.getExtension(file5);

    assertEquals("txt", extension);
    assertEquals("123456789", extension2);
    assertEquals("", extension3);
    assertEquals("", extension4);
    assertEquals("", extension5);
  }
}
