plugins {
  `java-library`
}

dependencies {
  api(libs.bundles.mockito)
  api(libs.junit.api)
  api(libs.junit.params)
  runtimeOnly(libs.junit.engine)
}
