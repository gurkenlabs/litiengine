import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-library`
  jacoco
  signing
  `maven-publish`
  id("org.sonarqube")
}

description = """
    LITIENGINE is a free and open source Java 2D Game Engine.
    It provides a comprehensive Java library and a dedicated map editor to create tile-based 2D games.
""".trimIndent()

val native: Configuration by configurations.creating

dependencies {
  implementation(libs.jinput.core)
  native(libs.jinput.core) {
    artifact {
      classifier = "natives-all"
    }
  }

  implementation(libs.bundles.soundlibs)
  implementation(libs.javax.activation)
  // This needs to be api to make the annotations on the class visible to the compiler.
  api(libs.xml.api)
  runtimeOnly(libs.bundles.xml.runtime)
  testImplementation(projects.litiengineShared)
}

java.sourceSets["main"].resources {
  srcDir(File(buildDir, "natives"))
}

tasks.register<Copy>("natives") {
  for (dep in configurations.runtimeClasspath.get().files + native.files) {
    from(zipTree(dep).files)
    include("**/*.dll", "**/*.so", "**/*.jnilib", "**/*.dylib")
    into(File(buildDir, "natives"))
  }
}

tasks.named("processResources") {
  dependsOn("natives")
}

tasks.named("sourcesJar") {
  dependsOn("natives")
}

tasks {
  test {
    workingDir = buildDir.resolve("test")
    doFirst {
      workingDir.mkdirs()
    }
    useJUnitPlatform()
    testLogging {
      events(TestLogEvent.FAILED)
      exceptionFormat = TestExceptionFormat.SHORT

      // set options for log level DEBUG
      debug {
        events(TestLogEvent.STARTED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
      }

      // remove standard output/error logging from --info builds
      // by assigning only 'failed' and 'skipped' events
      info.events(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
  }

  jacocoTestReport {
    reports {
      xml.required.set(true)
      html.required.set(true)
    }
  }
}

publishing {
  publications {
    create<MavenPublication>(project.name) {
      artifactId = project.name
      version = project.version.toString()
      from(components["java"])
    }
  }
}
