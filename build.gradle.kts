import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings
import com.github.vlsi.gradle.properties.dsl.props
import com.github.vlsi.gradle.properties.dsl.stringProperty
import com.github.vlsi.gradle.properties.dsl.toBool

plugins {
  java
  alias(libs.plugins.spotless)
  alias(libs.plugins.vlsi.crlf)
  alias(libs.plugins.vlsi.gradleExtensions)
  alias(libs.plugins.versions)
}

val skipSpotless by props(false)
val skipJavadoc by props()
val enableMavenLocal by props(false)
val enableGradleMetadata by props()
val isRelease = project.stringProperty("release").toBool()

val litiengineVersion = project.stringProperty("litiengine.version")
val buildNumber: String = if (System.getenv("GITHUB_RUN_NUMBER") != null) "-${System.getenv("GITHUB_RUN_NUMBER")}" else ""
val snapshotSuffix: String = if (isRelease) "-SNAPSHOT" else ""


//releaseParams {
//  tlp.set("litiengine")
//  organizationName.set("gurkenlabs")
//  componentName.set("litiengine")
//  prefixForProperties.set("gh")
//  svnDistEnabled.set(false)
//  sitePreviewEnabled.set(false)
//  release.set(isRelease)
//  if (!isRelease) {
//    rcTag.set("v$litiengineVersion$buildNumber$snapshotSuffix")
//  }
//  nexus {
//    credentials {
//      username.set(System.getenv("NEXUS_USERNAME"))
//      password.set(System.getenv("NEXUS_PASSWORD"))
//    }
//    val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
//    val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
//    prodUrl.set(uri(if (isRelease) releasesRepoUrl else snapshotsRepoUrl))
//  }
//
//  voteText.set {
//    """
//    ${it.componentName} v${it.version}-rc${it.rc} is ready for preview.
//    Git SHA: ${it.gitSha}
//    Staging repository: ${it.nexusRepositoryUri}
//    """.trimIndent()
//  }
//}

//tasks.closeRepository.configure { enabled = isRelease }

spotless {
  java {
    removeUnusedImports()
    eclipse().configFile("${project.rootDir}/config/gurkenlabs.eclipseformat.xml")
  }
}


val buildVersion = "$litiengineVersion$buildNumber$snapshotSuffix"
allprojects {
  group = "de.gurkenlabs"
  version = buildVersion

  configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
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
}
