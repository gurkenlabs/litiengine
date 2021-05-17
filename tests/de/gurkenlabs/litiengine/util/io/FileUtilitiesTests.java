package de.gurkenlabs.litiengine.util.io;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;


public class FileUtilitiesTests {

  @TempDir
  File tempDir;

  @Test
  public void testDeleteNoneDir(){
    File dir = new File("/test/test2/");
    assertFalse(FileUtilities.deleteDir(dir));
  }

  @Test
  public void testDeleteExistingDirZeroChildren() throws IOException {
    assertTrue(FileUtilities.deleteDir(tempDir));
  }

  @Test
  public void testDeleteExistingDir() throws IOException {
    File file1 = new File(tempDir, "file1.txt");
    file1.createNewFile();
    assertTrue(FileUtilities.deleteDir(tempDir));
  }

  @Test
  public void testDeleteExistingDirNewFiles() throws IOException {
    File file1 = new File(tempDir, "file1.txt");
    file1.createNewFile();
    File file2 = new File(tempDir, "file2.txt");
    file2.createNewFile();
    assertTrue(FileUtilities.deleteDir(tempDir));
  }

  @Test
  public void testDeleteExistingDirNoChildren() {
    assertFalse(FileUtilities.deleteDir(new File("")));
  }

  @Test
  void testFindFilesByExtension(@TempDir Path tempDir) throws IOException {

    Path directory1 = Files.createDirectories(tempDir.resolve("test"));
    Path directory2 = Files.createDirectories(tempDir.resolve("\\bin"));

    Path file1 = Files.createFile(tempDir.resolve("file1.txt"));
    Path file2 = Files.createFile(tempDir.resolve("file2.pdf"));
    Path file3 = Files.createFile(directory1.resolve("file3.pdf"));
    Path file4 = Files.createFile(directory2.resolve("file4.pdf"));

    List<String> fileNames = new LinkedList<>(Arrays.asList());
    List<String> expectedFile = new LinkedList<>(Arrays.asList(file3.toAbsolutePath().toString(), file2.toAbsolutePath().toString()));

    assertEquals(expectedFile, FileUtilities.findFilesByExtension(fileNames, tempDir, "pdf"));
  }

  @ParameterizedTest(name = "testCombinePaths path1={0}, path2={1}, expectedValue={2}")
  @MethodSource("getCombinedPaths")
  public void testCombinePaths(String path1, String path2, String expectedValue) {

    String combined = FileUtilities.combine(path1, path2);
    assertEquals(expectedValue, combined);

  }

  private static Stream<Arguments> getCombinedPaths() {
    return Stream.of(
            Arguments.of("/test/test2/", "somepath/123/456/", "/test/test2/somepath/123/456/"),
            Arguments.of("somepath/123/456/", "./test/test2", "somepath/123/456/test/test2"),
            Arguments.of("../somepath/123/456/", "./test/test2", "../somepath/123/456/test/test2"),
            Arguments.of("../test/test2/", "./test/test2", "../test/test2/test/test2"),
            Arguments.of("somepath/123/456/", "../file.txt", "somepath/123/file.txt"),
            Arguments.of("somepath/123/456/", "file.txt", "somepath/123/456/file.txt"));
  }

  @ParameterizedTest(name = "testCombinePaths path1={0}, path2={1}, expectedValue={2}")
  @MethodSource("getCombinedThreePaths")
  public void testCombineThreePaths(String path1, String path2, String path3, String expectedValue) {
    String combined = FileUtilities.combine(path1, path2, path3);
    assertEquals(expectedValue, combined);
  }

  private static Stream<Arguments> getCombinedThreePaths() {
    return Stream.of(
            Arguments.of("../test/test2/", "./test/test2", "../somepath/123/456/", "../test/test2/somepath/123/456/"));
  }

  @ParameterizedTest(name = "testCombinePaths path1={0}, path2={1}, expectedValue={2}")
  @MethodSource("getCombinePathsWin")
  public void testCombineThreePathsWin(String path1, String path2, String expectedValue) {
    String combined = FileUtilities.combine(path1, path2);
    assertEquals(expectedValue, combined);
  }

  private static Stream<Arguments> getCombinePathsWin() {
    return Stream.of(
            Arguments.of("\\test\\test2\\", "somepath/123/456/", "/test/test2/somepath/123/456/"),
            Arguments.of("somepath/123/456/", "test\\test2\\", "somepath/123/456/test/test2/"),
            Arguments.of("../somepath/123/456/", "test\\test2\\", "../somepath/123/456/test/test2/"),
            Arguments.of("..\\test\\test2\\", "test\\test2\\", "../test/test2/test/test2/"),
            Arguments.of("somepath/123/456/", "..\\file.txt", "somepath/123/file.txt"),
            Arguments.of("somepath/123/456/", "file.txt", "somepath/123/456/file.txt")
    );
  }

  @ParameterizedTest(name = "testCombinePaths path1={0}, path2={1}, expectedValue={2}")
  @MethodSource("getCombineThreePathsWin")
  public void testCombineThreePathsWin(String path1, String path2, String path3, String expectedValue) {
    String combined = FileUtilities.combine(path1, path2, path3);
    assertEquals(expectedValue, combined);
  }

  private static Stream<Arguments> getCombineThreePathsWin() {
    return Stream.of(
            Arguments.of("..\\test\\test2\\", "test\\test2\\", "somepath/123/456/", "../test/test2/test/test2/somepath/123/456/"));
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
  public void testGetFileName(String file, String expectedValue) {
    String filename = FileUtilities.getFileName(file);
    assertEquals(expectedValue, filename);
  }

  private static Stream<Arguments> getFileNames() {
    return Stream.of(
            Arguments.of("C:\\test\\test2\\test.txt", "test"),
            Arguments.of("/somepath/123/456/test.txt", "test"),
            Arguments.of("/somepath/123/456/test", "test"),
            Arguments.of("/somepath/123/456/", ""),
            Arguments.of("/somep.ath/1.23/45.6/test.txt", "test"),
            Arguments.of("/somep.ath/1.23/45.6/", ""));
  }

  @ParameterizedTest(name = "testGetFileName file={0}, expectedValue={1}")
  @MethodSource("getGetExtension")
  public void testGetExtension(String file, String expectedValue) {
    String extension = FileUtilities.getExtension(file);
    assertEquals(expectedValue, extension);
  }

  private static Stream<Arguments> getGetExtension() {
    return Stream.of(
            Arguments.of("C:\\test\\test2\\test.txt", "txt"),
            Arguments.of("/somepath/123/456/test.123456789", "123456789"),
            Arguments.of("/somepath/123/456/test", ""),
            Arguments.of("/somepath/123/456/", ""),
            Arguments.of("/somepath/1.23/4.56/", ""));
  }

  @ParameterizedTest(name = "testHumanReadableByteCount bytes={0}, expectedValue={1}")
  @MethodSource("getGetHumanReadableByteCount")
  public void testHumanReadableByteCount(long bytes, String expectedValue) {
      assertEquals(expectedValue, FileUtilities.humanReadableByteCount(bytes));
    }


  private static Stream<Arguments> getGetHumanReadableByteCount() {
    return Stream.of(
            Arguments.of(0, "0 bytes"),
            Arguments.of(1023, "1023 bytes"),
            Arguments.of(1024, "1.0 KiB"),
            Arguments.of(1025, "1.0 KiB"),
            Arguments.of(1024 + 1024 / 2, "1.5 KiB"),
            Arguments.of(1048576, "1.0 MiB"),
            Arguments.of(1073741824, "1.0 GiB"),
            Arguments.of(1099511627776l, "1.0 TiB"),
            Arguments.of(1125899906842624l, "1.0 PiB"),
            Arguments.of(1125899906842624l + 1125899906842624l / 2, "1.5 PiB"),
            Arguments.of(1152921504606846976l, "1.0 EiB"),
            Arguments.of(Long.MAX_VALUE, "8.0 EiB")
    );
  }

  @ParameterizedTest(name = "testHumanReadableByteCountDecimal bytes={0}, isDecimal={1}, expectedValue={2}")
  @MethodSource("getGetHumanReadableByteCountDecimal")
  public void testHumanReadableByteCountDecimal(long bytes, boolean isDecimal, String expectedValue) {
    assertEquals(expectedValue, FileUtilities.humanReadableByteCount(bytes, isDecimal));
  }

  private static Stream<Arguments> getGetHumanReadableByteCountDecimal() {
    return Stream.of(
            Arguments.of(1152921504606846976l, true, "1.2 EB"),
            Arguments.of(0, true, "0 bytes"),
            Arguments.of(999, true, "999 bytes"),
            Arguments.of(1001, true, "1.0 KB"),
            Arguments.of(1000, true, "1.0 KB"),
            Arguments.of(1000+1000/2, true, "1.5 KB"),
            Arguments.of(1000000, true, "1.0 MB"),
            Arguments.of(1000000000, true, "1.0 GB"),
            Arguments.of(1000000000000l, true, "1.0 TB"),
            Arguments.of(1000000000000000l, true, "1.0 PB"),
            Arguments.of(1000000000000000l+1000000000000000l/2, true, "1.5 PB"),
            Arguments.of(1000000000000000000l, true, "1.0 EB"),
            Arguments.of(Long.MAX_VALUE, true, "9.2 EB"),
            Arguments.of(1000000000000000000l, false, "888.2 PiB")
    );
  }


}
