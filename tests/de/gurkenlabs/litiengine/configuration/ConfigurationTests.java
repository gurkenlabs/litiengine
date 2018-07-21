package de.gurkenlabs.litiengine.configuration;

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
      assertEquals(10, configGroup.getTestInt());
      assertEquals(10, configGroup.getTestByte());
      assertEquals(10, configGroup.getTestShort());
      assertEquals(10, configGroup.getTestLong());
      assertEquals(10.0, configGroup.getTestDouble(), 0.00001);
      assertEquals(10, configGroup.getTestFloat(), 0.00001f);
      assertEquals("test", configGroup.getTestString());
      assertEquals(true, configGroup.isTestBoolean());
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

    private boolean testBoolean;
    private byte testByte;
    private double testDouble;
    private TEST testEnum;
    private float testFloat;
    private int testInt;
    private long testLong;
    private short testShort;
    private String testString;
    private String[] testStringArray;

    private String testWithNoSetter;

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
      this.testBoolean = true;
    }

    public void setTestByte(final byte testByte) {
      this.testByte = 10;
    }

    public void setTestDouble(final double testDouble) {
      this.testDouble = 10.0;
    }

    public void setTestEnum(final TEST testEnum) {
      this.testEnum = TEST.TEST2;
    }

    public void setTestFloat(final float testFloat) {
      this.testFloat = 10.0f;
    }

    public void setTestInt(final int test1) {
      this.testInt = 10;
    }

    public void setTestLong(final long testLong) {
      this.testLong = 10;
    }

    public void setTestShort(final short testShort) {
      this.testShort = 10;
    }

    public void setTestString(final String testString) {
      this.testString = "test";
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
