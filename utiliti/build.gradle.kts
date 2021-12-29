import java.util.Calendar

plugins {
  `java-library`
  id("org.beryx.runtime")
}

description = """
    utiLITI is the official project / asset manager and map editor for the open source Java 2D Game Engine LITIENGINE.
""".trimIndent()

sourceSets {
  main {
    resources {
      srcDir("src/main/localization")
    }
  }
}

application {
  mainClass.set("de.gurkenlabs.utiliti.Program")
  applicationDistribution.into(executableDir) {
    from("src/dist")
  }
}

dependencies {
  implementation(projects.litiengine)
  implementation(libs.darklaf.core)
  testImplementation(projects.litiengineShared)
}

tasks {
  test {
    useJUnitPlatform()
  }
}

runtime {
  options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
  modules.set(listOf("java.desktop", "java.logging", "java.datatransfer", "java.management", "java.xml"))

  launcher {
    noConsole = false
  }

  jpackage {
    appVersion = project.version.toString().run {
      val invalidIndex = indexOfFirst { !it.isDigit() && it != '.' }
      if (invalidIndex >= 0) {
        substring(0, invalidIndex)
      } else this
    }
    skipInstaller = true
    imageOptions.addAll(
      listOf(
        "--icon", project.file("src/dist/pixel-icon-utiliti.ico").path,
        "--description", project.description,
        "--copyright", "2020-${Calendar.getInstance().get(Calendar.YEAR)} gurkenlabs.de",
        "--vendor", "gurkenlabs.de",
        "--java-options", "-Xms256m",
        "--java-options", "-Xmx2048m",
      )
    )
  }
}
