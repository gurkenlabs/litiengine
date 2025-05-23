package de.gurkenlabs.litiengine.util.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class FileUtilitiesTests {

  @TempDir
  Path tempDir;

  @TempDir
  Path tempPath;

  @Test
  void testDeleteNoneDir() {
    Path dir = Paths.get("/test/test2/");
    assertFalse(FileUtilities.deleteDir(dir));
  }

  @Test
  void testDeleteExistingDirZeroChildren() {
    assertTrue(FileUtilities.deleteDir(tempDir));
  }

  @Test
  void testDeleteExistingDir() throws IOException {
    Path file1 = tempDir.resolve("file1.txt");
    Files.createFile(file1);
    assertTrue(FileUtilities.deleteDir(tempDir));
  }

  @Test
  void testDeleteExistingDirNewFiles() throws IOException {
    // arrange
    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.txt");

    // act
    Files.createFile(file1);
    Files.createFile(file2);

    // assert
    assertTrue(FileUtilities.deleteDir(tempDir));
  }

  @Test
  void testDeleteExistingDirNoChildren() {
    assertFalse(FileUtilities.deleteDir(new File("")));
  }

  @Test
  void testFindFilesByExtensionDirectory() throws IOException {
    // arrange
    Files.createFile(tempPath.resolve("notToBeFound.pdf"));
    Path testFile = Files.createFile(tempPath.resolve("test.txt"));

    List<String> fileNames = new LinkedList<>(Collections.emptyList());
    List<String> expectedFiles = new LinkedList<>(Collections.singletonList(testFile.toAbsolutePath().toString()));

    // act
    List<String> actualFiles = FileUtilities.findFilesByExtension(fileNames, tempPath, "txt");

    // assert
    assertEquals(expectedFiles, actualFiles);
  }

  @Test
  void testFindFilesByExtensionSubDirectory() throws IOException {
    // arrange
    Path subDirectory = Files.createDirectories(tempPath.resolve("test"));
    Path textFileSub = Files.createFile(subDirectory.resolve("test.txt"));

    List<String> fileNames = new LinkedList<>(Collections.emptyList());
    List<String> expectedFiles = new LinkedList<>(Collections.singletonList(textFileSub.toAbsolutePath().toString()));

    // act
    List<String> actualFiles = FileUtilities.findFilesByExtension(fileNames, tempPath, "txt");

    // assert
    assertEquals(expectedFiles, actualFiles);
  }

  @Test
  void testFindFilesByExtensionBlackListedDirectory() throws IOException {
    // arrange
    Path blackListedDir = Files.createDirectories(tempPath.resolve("\\bin"));
    Path testFile = blackListedDir.resolve("test.txt");
    if (Files.exists(testFile)) {
      Files.delete(testFile);
    }
    Files.createFile(testFile);

    List<String> fileNames = new LinkedList<>(Collections.emptyList());

    // act
    List<String> actualFiles = FileUtilities.findFilesByExtension(fileNames, tempPath, "txt");

    // assert
    assertEquals(0, actualFiles.size());
  }

  @ParameterizedTest(name = "testCombinePaths path1={0}, path2={1}, expectedValue={2}")
  @MethodSource("getCombinedPaths")
  void testCombinePaths(String path1, String path2, String expectedValue) {
    String combined = FileUtilities.combine(path1, path2);
    assertEquals(expectedValue, combined);
  }

  @ParameterizedTest(name = "testCombinePaths path1={0}, path2={1}, expectedValue={2}")
  @MethodSource("getCombinedThreePaths")
  void testCombineThreePaths(String path1, String path2, String path3, String expectedValue) {
    String combined = FileUtilities.combine(path1, path2, path3);
    assertEquals(expectedValue, combined);
  }

  @ParameterizedTest(name = "testCombinePaths path1={0}, path2={1}, expectedValue={2}")
  @MethodSource("getCombinePathsWin")
  void testCombineThreePathsWin(String path1, String path2, String expectedValue) {
    String combined = FileUtilities.combine(path1, path2);
    assertEquals(expectedValue, combined);
  }

  @ParameterizedTest(name = "testCombinePaths path1={0}, path2={1}, expectedValue={2}")
  @MethodSource("getCombineThreePathsWin")
  void testCombineThreePathsWin(String path1, String path2, String path3, String expectedValue) {
    String combined = FileUtilities.combine(path1, path2, path3);
    assertEquals(expectedValue, combined);
  }

  @Test
  void testCombinePathsWithSpace() {
    // arrange
    String path = "\\test\\test2  sadasd sadsad\\";
    String path2 = "test222\\sadasd sadsad\\";

    // act
    String combined = FileUtilities.combine(path, path2);

    // assert
    assertEquals("/test/test2  sadasd sadsad/test222/sadasd sadsad/", combined);
  }

  @ParameterizedTest(name = "testGetFileName file={0}, expectedValue={1}")
  @MethodSource("getFileNames")
  void testGetFileName(String file, String expectedValue) {
    String filename = FileUtilities.getFileName(file);
    assertEquals(expectedValue, filename);
  }

  @ParameterizedTest(name = "testGetFileName file={0}, expectedValue={1}")
  @MethodSource("getGetExtension")
  void testGetExtension(String file, String expectedValue) {
    String extension = FileUtilities.getExtension(file);
    assertEquals(expectedValue, extension);
  }

  @ParameterizedTest(name = "testHumanReadableByteCount bytes={0}, expectedValue={1}")
  @MethodSource("getGetHumanReadableByteCount")
  void testHumanReadableByteCount(long bytes, String expectedValue) {
    assertEquals(expectedValue, FileUtilities.humanReadableByteCount(bytes));
  }

  @ParameterizedTest(name = "testHumanReadableByteCountDecimal bytes={0}, isDecimal={1}, expectedValue={2}")
  @MethodSource("getGetHumanReadableByteCountDecimal")
  void testHumanReadableByteCountDecimal(long bytes, boolean isDecimal, String expectedValue) {
    assertEquals(expectedValue, FileUtilities.humanReadableByteCount(bytes, isDecimal));
  }

  private static Stream<Arguments> getCombinedPaths() {
    return Stream.of(Arguments.of("/test/test2/", "somepath/123/456/", "/test/test2/somepath/123/456/"),
        Arguments.of("somepath/123/456/", "./test/test2", "somepath/123/456/test/test2"),
        Arguments.of("../somepath/123/456/", "./test/test2", "../somepath/123/456/test/test2"),
        Arguments.of("../test/test2/", "./test/test2", "../test/test2/test/test2"),
        Arguments.of("somepath/123/456/", "../file.txt", "somepath/123/file.txt"),
        Arguments.of("somepath/123/456/", "file.txt", "somepath/123/456/file.txt"));
  }

  private static Stream<Arguments> getCombinedThreePaths() {
    return Stream.of(Arguments.of("../test/test2/", "./test/test2", "../somepath/123/456/", "../test/test2/somepath/123/456/"));
  }

  private static Stream<Arguments> getCombinePathsWin() {
    return Stream.of(Arguments.of("\\test\\test2\\", "somepath/123/456/", "/test/test2/somepath/123/456/"),
        Arguments.of("somepath/123/456/", "test\\test2\\", "somepath/123/456/test/test2/"),
        Arguments.of("../somepath/123/456/", "test\\test2\\", "../somepath/123/456/test/test2/"),
        Arguments.of("..\\test\\test2\\", "test\\test2\\", "../test/test2/test/test2/"),
        Arguments.of("somepath/123/456/", "..\\file.txt", "somepath/123/file.txt"),
        Arguments.of("somepath/123/456/", "file.txt", "somepath/123/456/file.txt"));
  }

  private static Stream<Arguments> getCombineThreePathsWin() {
    return Stream.of(Arguments.of("..\\test\\test2\\", "test\\test2\\", "somepath/123/456/", "../test/test2/test/test2/somepath/123/456/"));
  }

  private static Stream<Arguments> getFileNames() {
    return Stream.of(Arguments.of("C:\\test\\test2\\test.txt", "test"), Arguments.of("/somepath/123/456/test.txt", "test"),
        Arguments.of("/somepath/123/456/test", "test"), Arguments.of("/somepath/123/456/", ""), Arguments.of("/somep.ath/1.23/45.6/test.txt", "test"),
        Arguments.of("/somep.ath/1.23/45.6/", ""));
  }

  private static Stream<Arguments> getGetExtension() {
    return Stream.of(Arguments.of("C:\\test\\test2\\test.txt", "txt"), Arguments.of("/somepath/123/456/test.123456789", "123456789"),
        Arguments.of("/somepath/123/456/test", ""), Arguments.of("/somepath/123/456/", ""), Arguments.of("/somepath/1.23/4.56/", ""));
  }

  private static Stream<Arguments> getGetHumanReadableByteCount() {
    return Stream.of(Arguments.of(0, "0 bytes"), Arguments.of(1023, "1023 bytes"), Arguments.of(1024, "1.0 KiB"), Arguments.of(1025, "1.0 KiB"),
        Arguments.of(1024 + 1024 / 2, "1.5 KiB"), Arguments.of(1048576, "1.0 MiB"), Arguments.of(1073741824, "1.0 GiB"),
        Arguments.of(1099511627776L, "1.0 TiB"), Arguments.of(1125899906842624L, "1.0 PiB"),
        Arguments.of(1125899906842624L + 1125899906842624L / 2, "1.5 PiB"), Arguments.of(1152921504606846976L, "1.0 EiB"),
        Arguments.of(Long.MAX_VALUE, "8.0 EiB"));
  }

  private static Stream<Arguments> getGetHumanReadableByteCountDecimal() {
    return Stream.of(Arguments.of(1152921504606846976L, true, "1.2 EB"), Arguments.of(0, true, "0 bytes"), Arguments.of(999, true, "999 bytes"),
        Arguments.of(1001, true, "1.0 KB"), Arguments.of(1000, true, "1.0 KB"), Arguments.of(1000 + 1000 / 2, true, "1.5 KB"),
        Arguments.of(1000000, true, "1.0 MB"), Arguments.of(1000000000, true, "1.0 GB"), Arguments.of(1000000000000L, true, "1.0 TB"),
        Arguments.of(1000000000000000L, true, "1.0 PB"), Arguments.of(1000000000000000L + 1000000000000000L / 2, true, "1.5 PB"),
        Arguments.of(1000000000000000000L, true, "1.0 EB"), Arguments.of(Long.MAX_VALUE, true, "9.2 EB"),
        Arguments.of(1000000000000000000L, false, "888.2 PiB"));
  }
}
