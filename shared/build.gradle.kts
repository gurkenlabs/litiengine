plugins {
  `java-library`
}

dependencies {
  api(testLibs.bundles.mockito)
  api(testLibs.junit.api)
  api(testLibs.junit.params)
  runtimeOnly(testLibs.junit.engine)
}
