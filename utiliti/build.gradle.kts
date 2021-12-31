import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.util.Calendar

plugins {
  `java-library`
  id("org.beryx.runtime")
}

val applicationName = "utiLITI"
description = """
    $applicationName is the official project / asset manager and map editor for the open source Java 2D Game Engine LITIENGINE.
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
    val currentOs: OperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()
    val iconFileType = when {
      currentOs.isWindows -> "ico"
      currentOs.isMacOsX -> "icns"
      else -> "png"
    }
    imageOptions.addAll(
      listOf(
        "--icon", project.file("src/dist/pixel-icon-utiliti.$iconFileType").path,
        "--description", project.description,
        "--copyright", "2020-${Calendar.getInstance().get(Calendar.YEAR)} gurkenlabs.de",
        "--vendor", "gurkenlabs.de",
        "--java-options", "-Xms256m",
        "--java-options", "-Xmx2048m",
      )
    )
    when {
      currentOs.isLinux -> imageOptions.addAll(
        listOf(
          "--linux-package-name", applicationName
        )
      )
      currentOs.isMacOsX -> imageOptions.addAll(
        listOf(
          "--mac-package-identifier", "de.gurkenlabs.litiengine.utiliti",
          "--mac-package-name", applicationName, // Name appearing in Menu Bar.
        )
      )
    }
  }
}
