package de.gurkenlabs.litiengine.configuration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.util.io.FileUtilities;

public class ConfigurationTests {

  private static void deleteTempConfigFile(final Configuration config) {
    if (config != null) {
      final File configFile = new File(config.getFileName());
      if (configFile.exists()) {
        configFile.delete();
      }
    }
  }

  @BeforeEach
  public void setup() {
    Logger.getLogger(FileUtilities.class.getName()).setUseParentHandlers(false);
    Logger.getLogger(Configuration.class.getName()).setUseParentHandlers(false);
  }

  @Test
  public void testConfigurationGroupInitialization() {
    Configuration config = null;
    final TestConfigurationGroup group = new TestConfigurationGroup();

    try {
      config = new Configuration(group);
      config.load();
      assertTrue(config.getConfigurationGroup(group.getClass()).equals(group));
      assertEquals("test-prefix", group.getPrefix());
      assertTrue(config.getConfigurationGroup("test-prefix").equals(group));

    } finally {
      deleteTempConfigFile(config);
    }
  }

  @Test
  public void testDefaultFileCreation() {
    Configuration config = null;
    try {
      config = new Configuration();
      config.load();
      assertTrue(new File(config.getFileName()).exists());
    } finally {
      deleteTempConfigFile(config);
    }
  }

  @Test
  public void testFieldInitialization() {
    Configuration config = null;
    final TestConfigurationGroup group = new TestConfigurationGroup();

    try {
      deleteTempConfigFile(config);
      config = new Configuration(group);
      config.load();
      assertTrue(new File(config.getFileName()).exists());

      config.load();
      final TestConfigurationGroup configGroup = config.getConfigurationGroup(TestConfigurationGroup.class);
      assertEquals(100, configGroup.getTestInt());
      assertEquals(101, configGroup.getTestByte());
      assertEquals(102, configGroup.getTestShort());
      assertEquals(103, configGroup.getTestLong());
      assertEquals(104.0d, configGroup.getTestDouble(), 0.00001);
      assertEquals(105.0f, configGroup.getTestFloat(), 0.00001f);
      assertEquals("test", configGroup.getTestString());
      assertEquals(true, configGroup.isTestBoolean());
      assertEquals(TEST.TEST1, configGroup.getTestEnum());
      assertEquals("", configGroup.getTestWithNoSetter());
      assertArrayEquals(new String[] { "test", "testicle" }, configGroup.getTestStringArray());
    } finally {

      deleteTempConfigFile(config);
    }
  }

  @Test
  public void testFileName() {
    final String testFileName = UUID.randomUUID().toString() + ".properties";
    Configuration config = null;
    try {
      config = new Configuration(testFileName);
      config.load();
      assertTrue(new File(testFileName).exists());
    } finally {
      deleteTempConfigFile(config);
    }
  }

  private enum TEST {
    TEST1, TEST2;
  }

  @ConfigurationGroupInfo(prefix = "test-prefix")
  @SuppressWarnings("unused")
  private class TestConfigurationGroup extends ConfigurationGroup {
    private int testInt = 100;
    private byte testByte = 101;
    private short testShort = 102;
    private long testLong = 103;
    private double testDouble = 104.0d;
    private float testFloat = 105.0f;
    private String testString = "test";
    private boolean testBoolean = true;
    private TEST testEnum = TEST.TEST1;
    private String[] testStringArray = new String[] { "test", "testicle" };
    private String testWithNoSetter = "";

    public byte getTestByte() {
      return this.testByte;
    }

    public double getTestDouble() {
      return this.testDouble;
    }

    public TEST getTestEnum() {
      return this.testEnum;
    }

    public float getTestFloat() {
      return this.testFloat;
    }

    public int getTestInt() {
      return this.testInt;
    }

    public long getTestLong() {
      return this.testLong;
    }

    public short getTestShort() {
      return this.testShort;
    }

    public String getTestString() {
      return this.testString;
    }

    public boolean isTestBoolean() {
      return this.testBoolean;
    }

    public void setTestBoolean(final boolean testBoolean) {
      this.testBoolean = testBoolean;
    }

    public void setTestByte(final byte testByte) {
      this.testByte = testByte;
    }

    public void setTestDouble(final double testDouble) {
      this.testDouble = testDouble;
    }

    public void setTestEnum(final TEST testEnum) {
      this.testEnum = testEnum;
    }

    public void setTestFloat(final float testFloat) {
      this.testFloat = testFloat;
    }

    public void setTestInt(final int test1) {
      this.testInt = test1;
    }

    public void setTestLong(final long testLong) {
      this.testLong = testLong;
    }

    public void setTestShort(final short testShort) {
      this.testShort = testShort;
    }

    public void setTestString(final String testString) {
      this.testString = testString;
    }

    public String[] getTestStringArray() {
      return testStringArray;
    }

    public void setTestStringArray(String[] testStringArray) {
      this.testStringArray = testStringArray;
    }

    public String getTestWithNoSetter() {
      return testWithNoSetter;
    }
  }
}
