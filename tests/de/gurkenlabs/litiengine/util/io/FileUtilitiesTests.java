package de.gurkenlabs.litiengine.util.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FileUtilitiesTests {

  @Test
  public void testCombinePaths() {
    String winPath = "C:\\test\\test2\\";
    String linuxPath = "/somepath/123/456/";
    
    String winPath2 = "\\test\\test2\\";
    
    String linuxPath3 = "../somepath/123/456/";
    String winPath3 = "..\\test\\test2\\";

    String combined = FileUtilities.combinePaths(winPath, linuxPath);
    String combined2 = FileUtilities.combinePaths(linuxPath, winPath2);
    String combined3 = FileUtilities.combinePaths(linuxPath3, winPath2);
    String combined4 = FileUtilities.combinePaths(winPath3, winPath2);
    String combined5 = FileUtilities.combinePaths(winPath3, winPath2, linuxPath);

    assertEquals("C:\\test\\test2\\somepath\\123\\456\\", combined);
    assertEquals("/somepath/123/456/test/test2/", combined2);
    assertEquals("../somepath/123/456/test/test2/", combined3);
    assertEquals("..\\test\\test2\\test\\test2\\", combined4);
    assertEquals("..\\test\\test2\\test\\test2\\somepath\\123\\456\\", combined5);
  }
}
