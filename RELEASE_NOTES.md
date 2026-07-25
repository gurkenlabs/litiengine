# Release Notes

## Next Release

* Java 25 is now the minimum supported runtime for LITIENGINE and utiLITI. Java 22, 23, and 24 are no longer supported.
* Published artifacts target Java 25.
* Applications using LITIENGINE's gamepad support should pass `--enable-native-access=ALL-UNNAMED` to the JVM. Java 25 otherwise warns by default,
  while `--illegal-native-access=deny` and future Java releases reject unauthorized native access. The packaged utiLITI launchers include the required option automatically.
