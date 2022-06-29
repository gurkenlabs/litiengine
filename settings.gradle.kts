enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "litiengine-sdk"

pluginManagement {
  plugins {
    fun idv(id: String, key: String = id) = id(id) version extra["$key.version"].toString()
    idv("org.sonarqube")
    idv("org.beryx.runtime")
    idv("com.github.vlsi.crlf", "com.github.vlsi.vlsi-release-plugins")
    idv("com.github.vlsi.gradle-extensions", "com.github.vlsi.vlsi-release-plugins")
    idv("com.github.vlsi.license-gather", "com.github.vlsi.vlsi-release-plugins")
    idv("com.github.vlsi.stage-vote-release", "com.github.vlsi.vlsi-release-plugins")
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

      library("darklaf-core", "com.github.weisj", "darklaf-core").versionRef("darklaf")
      library("jinput-core", "net.java.jinput", "jinput").versionRef("jinput")
      library("soundlibs-jorbis", "com.googlecode.soundlibs", "jorbis").versionRef("soundlibs.jorbis")
      library("soundlibs-tritonus", "com.googlecode.soundlibs", "tritonus-share").versionRef("soundlibs.tritonus")
      library("soundlibs-vorbisspi", "com.googlecode.soundlibs", "vorbisspi").versionRef("soundlibs.vorbisspi")
      library("soundlibs-mp3spi", "com.googlecode.soundlibs", "mp3spi").versionRef("soundlibs.mp3spi")
      bundle(
        "soundlibs",
        listOf(
          "soundlibs-jorbis",
          "soundlibs-tritonus",
          "soundlibs-vorbisspi",
          "soundlibs-mp3spi"
        )
      )

      library("javax-activation", "javax.activation", "javax.activation-api").versionRef("javax.activation")
      library("xml-api", "jakarta.xml.bind", "jakarta.xml.bind-api").versionRef("jakarta.xml")
      library("xml-runtime-core", "com.sun.xml.bind", "jaxb-core").versionRef("jakarta.xml")
      library("xml-runtime-impl", "com.sun.xml.bind", "jaxb-impl").versionRef("jakarta.xml")
      bundle(
        "xml-runtime",
        listOf("xml-runtime-core", "xml-runtime-impl")
      )
    }
    create("testLibs") {
      versionId("junit")
      versionId("mockito")

      library("junit-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
      library("junit-params", "org.junit.jupiter", "junit-jupiter-params").versionRef("junit")
      library("junit-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")

      library("mockito-core", "org.mockito", "mockito-core").versionRef("mockito")
      library("mockito-inline", "org.mockito", "mockito-inline").versionRef("mockito")
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
