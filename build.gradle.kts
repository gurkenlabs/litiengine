import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings
import com.github.vlsi.gradle.properties.dsl.props
import com.github.vlsi.gradle.properties.dsl.stringProperty
import com.github.vlsi.gradle.properties.dsl.toBool
import com.github.vlsi.gradle.publishing.dsl.simplifyXml
import com.github.vlsi.gradle.publishing.dsl.versionFromResolution

plugins {
  alias(libs.plugins.spotless)
  alias(libs.plugins.vlsi.crlf)
  alias(libs.plugins.vlsi.gradleExtensions)
  alias(libs.plugins.vlsi.stageVoteRelease)
  alias(libs.plugins.versions)
}

val skipSpotless by props(false)
val skipJavadoc by props()
val enableMavenLocal by props(false)
val enableGradleMetadata by props()
val isRelease = project.stringProperty("release").toBool()

val String.v: String get() = rootProject.extra["$this.version"] as String
val projectVersion = "litiengine".v

releaseParams {
  tlp.set("litiengine")
  organizationName.set("gurkenlabs")
  componentName.set("litiengine")
  prefixForProperties.set("gh")
  svnDistEnabled.set(false)
  sitePreviewEnabled.set(false)
  release.set(isRelease)
  if (!isRelease) {
    rcTag.set("v$projectVersion$snapshotSuffix")
  }
  nexus {
    mavenCentral()
  }
  voteText.set {
    """
    ${it.componentName} v${it.version}-rc${it.rc} is ready for preview.
    Git SHA: ${it.gitSha}
    Staging repository: ${it.nexusRepositoryUri}
    """.trimIndent()
  }
}

tasks.closeRepository.configure { enabled = isRelease }

val buildVersion = "$projectVersion${releaseParams.snapshotSuffix}"

allprojects {
  group = "de.gurkenlabs"
  version = buildVersion

  repositories {
    if (enableMavenLocal) {
      mavenLocal()
    }
    if (!isRelease) {
      maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    }
    mavenCentral()
  }

  configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
  }

  if (!skipSpotless) {
    apply(plugin = "com.diffplug.spotless")
    spotless {
      plugins.withType<JavaPlugin>().configureEach {
        java {
          removeUnusedImports()
          eclipse().configFile("${project.rootDir}/config/gurkenlabs.eclipseformat.xml")
        }
      }
    }
  }

  tasks.withType<AbstractArchiveTask>().configureEach {
    // Ensure builds are reproducible
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    dirMode = "775".toInt(8)
    fileMode = "664".toInt(8)
  }

  if (!enableGradleMetadata) {
    tasks.withType<GenerateModuleMetadata> {
      enabled = false
    }
  }

  plugins.withType<JavaPlugin> {
    apply<IdeaPlugin>()
    apply<EclipsePlugin>()

    configure<JavaPluginExtension> {
      toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
      }
      withSourcesJar()
      if (!skipJavadoc && isRelease) {
        withJavadocJar()
      }
    }

    tasks {
      withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
      }

      withType<Jar>().configureEach {
        manifest {
          attributes["Bundle-License"] = "MIT"
          attributes["Implementation-Title"] = project.name
          attributes["Implementation-Version"] = project.version
          attributes["Specification-Vendor"] = "Gurkenlabs"
          attributes["Specification-Version"] = project.version
          attributes["Specification-Title"] = "LITIENGINE"
          attributes["Implementation-Vendor"] = "Gurkenlabs"
          attributes["Implementation-Vendor-Id"] = project.group
        }

        CrLfSpec(LineEndings.LF).run {
          into("META-INF") {
            filteringCharset = "UTF-8"
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            // This includes either project-specific license or a default one
            if (file("$projectDir/LICENSE").exists()) {
              textFrom("$projectDir/LICENSE")
            } else {
              textFrom("$rootDir/LICENSE")
            }
          }
        }
      }

      withType<Javadoc>().configureEach {
        (options as StandardJavadocDocletOptions).apply {
          quiet()
          locale = "en"
          docEncoding = "UTF-8"
          charSet = "UTF-8"
          encoding = "UTF-8"
          docTitle = "${project.name.capitalize()} API"
          windowTitle = "${project.name.capitalize()} API"
          header = "<b>${project.name.capitalize()}</b>"
          addBooleanOption("Xdoclint:none", true)
          addBooleanOption("html5", true)
          links("https://docs.oracle.com/en/java/javase/17/docs/api/")
        }
      }
    }
  }

  plugins.withType<MavenPublishPlugin>().configureEach {
    configure<PublishingExtension> {
      if (project.path == ":") {
        // Skip the root project
        return@configure
      }

      val useInMemoryKey by props()
      if (useInMemoryKey) {
        apply(plugin = "signing")

        configure<SigningExtension> {
          useInMemoryPgpKeys(
            project.stringProperty("signing.inMemoryKey"),
            project.stringProperty("signing.password")
          )
        }
      }

      publications {
        withType<MavenPublication> {
          // Use the resolved versions in pom.xml
          // Gradle might have different resolution rules, so we set the versions
          // that were used in Gradle build/test.
          versionFromResolution()
          pom {
            simplifyXml()
            description.set(project.description!!)
            name.set(
              (project.findProperty("artifact.name") as? String)
                ?: project.name.capitalize().replace("-", " ")
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
  }
}
