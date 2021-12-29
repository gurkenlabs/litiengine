enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")
rootProject.name = "litiengine-sdk"

pluginManagement {
  plugins {
    fun idv(id: String, key: String = id) = id(id) version extra["$key.version"].toString()
    idv("org.sonarqube")
    idv("org.beryx.runtime")
    idv("com.github.vlsi.crlf", "com.github.vlsi.vlsi-release-plugins")
    idv("com.github.vlsi.gradle-extensions", "com.github.vlsi.vlsi-release-plugins")
    idv("com.github.vlsi.license-gather", "com.github.vlsi.vlsi-release-plugins")
    idv("com.diffplug.spotless")
  }
}

dependencyResolutionManagement {
  versionCatalogs {
    fun VersionCatalogBuilder.versionId(id: String) =
      version(id, extra["$id.version"].toString())
    create("libs") {
      versionId("darklaf")
      versionId("jinput")
      versionId("soundlibs.jorbis")
      versionId("soundlibs.tritonus")
      versionId("soundlibs.vorbisspi")
      versionId("soundlibs.mp3spi")
      versionId("javax.activation")
      versionId("jakarta.xml")

      alias("darklaf-core").to("com.github.weisj", "darklaf-core")
        .versionRef("darklaf")

      alias("jinput-core").to("net.java.jinput", "jinput")
        .versionRef("jinput")

      alias("soundlibs-jorbis").to("com.googlecode.soundlibs", "jorbis")
        .versionRef("soundlibs.jorbis")
      alias("soundlibs-tritonus").to("com.googlecode.soundlibs", "tritonus-share")
        .versionRef("soundlibs.tritonus")
      alias("soundlibs-vorbisspi").to("com.googlecode.soundlibs", "vorbisspi")
        .versionRef("soundlibs.vorbisspi")
      alias("soundlibs-mp3spi").to("com.googlecode.soundlibs", "mp3spi")
        .versionRef("soundlibs.mp3spi")
      bundle(
        "soundlibs",
        listOf(
          "soundlibs-jorbis",
          "soundlibs-tritonus",
          "soundlibs-vorbisspi",
          "soundlibs-mp3spi"
        )
      )

      alias("javax-activation").to("javax.activation", "javax.activation-api")
        .versionRef("javax.activation")
      alias("xml-api").to("jakarta.xml.bind", "jakarta.xml.bind-api")
        .versionRef("jakarta.xml")
      alias("xml-runtime-core").to("com.sun.xml.bind", "jaxb-core")
        .versionRef("jakarta.xml")
      alias("xml-runtime-impl").to("com.sun.xml.bind", "jaxb-impl")
        .versionRef("jakarta.xml")
      bundle(
        "xml-runtime",
        listOf("xml-runtime-core", "xml-runtime-impl")
      )
    }
    create("testLibs") {
      versionId("junit")
      versionId("mockito")

      alias("junit-api").to("org.junit.jupiter", "junit-jupiter-api")
        .versionRef("junit")
      alias("junit-params").to("org.junit.jupiter", "junit-jupiter-params")
        .versionRef("junit")
      alias("junit-engine").to("org.junit.jupiter", "junit-jupiter-engine")
        .versionRef("junit")

      alias("mockito-core").to("org.mockito", "mockito-core")
        .versionRef("mockito")
      alias("mockito-inline").to("org.mockito", "mockito-inline")
        .versionRef("mockito")
      bundle(
        "mockito",
        listOf("mockito-core", "mockito-inline")
      )
    }
  }
}

include(
  "core",
  "utiliti",
  "shared"
)

for (p in rootProject.children) {
  if (p.children.isEmpty()) {
    if (p.name == "core") {
      p.name = "litiengine"
    } else {
      p.name = "litiengine-${p.name}"
    }
  }
}

gradle.projectsLoaded {
  rootProject.allprojects {
    buildDir = File("../build/${project.name}")
  }
}
