package de.gurkenlabs.litiengine.util.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.entities.Rotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class FileUtilitiesTests {

  @Test
  public void testCombinePaths() {
    String path1 = "/test/test2/";
    String relativePath = "./test/test2";
    String relativePath2 = "../test/test2/";
    String relativePath3 = "../somepath/123/456/";
    
    String otherPath = "somepath/123/456/";

    String somefile = "../file.txt";
    String someOtherFile = "file.txt";

    String combined = FileUtilities.combine(path1, otherPath);
    String combined2 = FileUtilities.combine(otherPath, relativePath);
    String combined3 = FileUtilities.combine(relativePath3, relativePath);
    String combined4 = FileUtilities.combine(relativePath2, relativePath);
    String combined5 = FileUtilities.combine(relativePath2, relativePath, relativePath3);
    String combined6 = FileUtilities.combine(otherPath, somefile);
    String combined7 = FileUtilities.combine(otherPath, someOtherFile);

    assertEquals("/test/test2/somepath/123/456/", combined);
    assertEquals("somepath/123/456/test/test2", combined2);
    assertEquals("../somepath/123/456/test/test2", combined3);
    assertEquals("../test/test2/test/test2", combined4);
    assertEquals("../test/test2/somepath/123/456/", combined5);
    assertEquals("somepath/123/file.txt", combined6);
    assertEquals("somepath/123/456/file.txt", combined7);
  }

  @Test
  public void testCombinePathsWin() {
    String winPath = "\\test\\test2\\";
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

    assertEquals("/test/test2/somepath/123/456/", combined);
    assertEquals("somepath/123/456/test/test2/", combined2);
    assertEquals("../somepath/123/456/test/test2/", combined3);
    assertEquals("../test/test2/test/test2/", combined4);
    assertEquals("../test/test2/test/test2/somepath/123/456/", combined5);
    assertEquals("somepath/123/file.txt", combined6);
    assertEquals("somepath/123/456/file.txt", combined7);
  }
  
  @Test
  public void testCombinePathsWithSpace() {
    String path = "\\test\\test2  sadasd sadsad\\";
    
    String path2 = "test222\\sadasd sadsad\\";
    
    String combined = FileUtilities.combine(path, path2);
    
    assertEquals("/test/test2  sadasd sadsad/test222/sadasd sadsad/", combined);
  }

  @ParameterizedTest(name = "testGetFileName file={0}, expectedValue={1}")
  @MethodSource("getFileNames")
  public void testGetFileName( String file, String expectedValue) {
    String filename = FileUtilities.getFileName(file);
    assertEquals(expectedValue, filename);
  }

  private static Stream<Arguments> getFileNames(){
    return Stream.of(
            Arguments.of("C:\\test\\test2\\test.txt", "test"),
            Arguments.of("/somepath/123/456/test.txt", "test"),
            Arguments.of("/somepath/123/456/test", "test"),
            Arguments.of("/somepath/123/456/", ""),
            Arguments.of("/somep.ath/1.23/45.6/test.txt", "test"),
            Arguments.of("/somep.ath/1.23/45.6/", "")
    );
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
