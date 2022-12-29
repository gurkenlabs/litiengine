import com.github.vlsi.gradle.properties.dsl.stringProperty
import com.github.vlsi.gradle.properties.dsl.toBool
import com.github.vlsi.gradle.publishing.dsl.simplifyXml
import com.github.vlsi.gradle.publishing.dsl.versionFromResolution
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-library`
  `maven-publish`
  jacoco
  signing
  alias(libs.plugins.sonarQube)
}

description = """
    LITIENGINE is a free and open source Java 2D Game Engine.
    It provides a comprehensive Java library and a dedicated map editor to create tile-based 2D games.
""".trimIndent()

val native: Configuration by configurations.creating
val isRelease = project.stringProperty("release").toBool()


dependencies {
  implementation(libs.jinput)
  native(libs.jinput) {
    artifact {
      classifier = "natives-all"
    }
  }

  implementation(libs.bundles.soundlibs)
  implementation(libs.javax.activation)
  implementation(libs.xml.api)
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
  repositories {
    mavenLocal()
    maven {
      val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
      val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
      name = "OSSRH"
      url = uri(if (isRelease) releasesRepoUrl else snapshotsRepoUrl)
      credentials {
        username = System.getenv("NEXUS_USERNAME")
        password = System.getenv("NEXUS_PASSWORD")
      }
    }
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/octocat/hello-world")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
  publications {
    create<MavenPublication>(project.name) {
      artifactId = project.name
      versionFromResolution()
      from(components["java"])

      pom {
        simplifyXml()
        description.set(project.description!!)
        name.set(
          (project.findProperty("artifact.name") as? String) ?: project.name.capitalize()
            .replace("-", " ")
        )
        url.set("https://litiengine.com")
        organization {
          name.set("Gurkenlabs")
          url.set("https://gurkenlabs.de/")
        }
        issueManagement {
          system.set("GitHub")
          url.set("https://github.com/gurkenlabs/litiengine/issues")
        }
        licenses {
          license {
            name.set("MIT")
            url.set("https://github.com/gurkenlabs/litiengine/blob/master/LICENSE")
            distribution.set("repo")
          }
        }
        scm {
          url.set("'https://github.com/gurkenlabs/litiengine/")
          connection.set("scm:git:git://github.com/gurkenlabs/litiengine.git")
          developerConnection.set("scm:git:git@github.com:gurkenlabs/litiengine.git")
        }
        developers {
          developer {
            id.set("steffen")
            name.set("Steffen Wilke")
            email.set("steffen@gurkenlabs.de")
          }
          developer {
            id.set("matthias")
            name.set("Matthias Wilke")
            email.set("matthias@gurkenlabs.de")
          }
        }
      }
    }
  }
}
signing {
  useInMemoryPgpKeys(
    System.getenv("GPG_SIGNING_KEY"),
    System.getenv("GPG_PASSPHRASE")
  )
}
