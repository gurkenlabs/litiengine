package de.gurkenlabs.litiengine.util.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
  
  @Test
  public void testHumanReadableByteCount() {
    long zero = 0;
    long max = Long.MAX_VALUE;
    {
      long lessThanKibibyte = 1023;
      long kibibyte = 1024;
      long moreThanKibibyte = 1025;
      long mebibyte = 1048576;
      long gibibyte = 1073741824;
      long tebibyte = 1099511627776l;
      long pebibyte = 1125899906842624l;
      long exbibyte = 1152921504606846976l;
      
      assertEquals("0 bytes", FileUtilities.humanReadableByteCount(zero));
      assertEquals("1023 bytes", FileUtilities.humanReadableByteCount(lessThanKibibyte));
      assertEquals("1.0 KiB", FileUtilities.humanReadableByteCount(kibibyte));
      assertEquals("1.0 KiB", FileUtilities.humanReadableByteCount(moreThanKibibyte));
      assertEquals("1.5 KiB", FileUtilities.humanReadableByteCount(kibibyte + kibibyte / 2));
      assertEquals("1.0 MiB", FileUtilities.humanReadableByteCount(mebibyte));
      assertEquals("1.0 GiB", FileUtilities.humanReadableByteCount(gibibyte));
      assertEquals("1.0 TiB", FileUtilities.humanReadableByteCount(tebibyte));
      assertEquals("1.0 PiB", FileUtilities.humanReadableByteCount(pebibyte));
      assertEquals("1.5 PiB", FileUtilities.humanReadableByteCount(pebibyte + pebibyte / 2));
      assertEquals("1.0 EiB", FileUtilities.humanReadableByteCount(exbibyte));
      assertEquals("8.0 EiB", FileUtilities.humanReadableByteCount(max));
      
      assertEquals("1.2 EB", FileUtilities.humanReadableByteCount(exbibyte, true));
      
    }
    
    {
      long lessThanKilobyte = 999;
      long kilobyte = 1000;
      long moreThanOneKilobyte = 1001;
      long megabyte = 1000000;
      long gigabyte = 1000000000;
      long terabyte = 1000000000000l;
      long petabyte = 1000000000000000l;
      long exabyte =  1000000000000000000l;
      
      assertEquals("0 bytes", FileUtilities.humanReadableByteCount(zero, true));
      assertEquals("999 bytes", FileUtilities.humanReadableByteCount(lessThanKilobyte, true));
      assertEquals("1.0 KB", FileUtilities.humanReadableByteCount(moreThanOneKilobyte, true));
      assertEquals("1.0 KB", FileUtilities.humanReadableByteCount(kilobyte, true));
      assertEquals("1.5 KB", FileUtilities.humanReadableByteCount(kilobyte + kilobyte / 2, true));
      assertEquals("1.0 MB", FileUtilities.humanReadableByteCount(megabyte, true));
      assertEquals("1.0 GB", FileUtilities.humanReadableByteCount(gigabyte, true));
      assertEquals("1.0 TB", FileUtilities.humanReadableByteCount(terabyte, true));
      assertEquals("1.0 PB", FileUtilities.humanReadableByteCount(petabyte, true));
      assertEquals("1.5 PB", FileUtilities.humanReadableByteCount(petabyte + petabyte / 2, true));
      assertEquals("1.0 EB", FileUtilities.humanReadableByteCount(exabyte, true));
      assertEquals("9.2 EB", FileUtilities.humanReadableByteCount(max, true));
      
      assertEquals("888.2 PiB", FileUtilities.humanReadableByteCount(exabyte, false));
      
    }
  }
}
