import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings
import com.github.vlsi.gradle.properties.dsl.props
import com.github.vlsi.gradle.properties.dsl.stringProperty
import com.github.vlsi.gradle.properties.dsl.toBool
import com.github.vlsi.gradle.publishing.dsl.simplifyXml
import com.github.vlsi.gradle.publishing.dsl.versionFromResolution

plugins {
    id("com.github.vlsi.crlf")
    id("com.github.vlsi.gradle-extensions")
}

val skipJavadoc by props()
val enableMavenLocal by props(false)
val enableGradleMetadata by props()
val isRelease = project.stringProperty("release").toBool()

val String.v: String get() = rootProject.extra["$this.version"] as String
val buildVersion = "litiengine".v + if (isRelease) "" else "-SNAPSHOT"

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
        maven { url = uri("https://plugins.gradle.org/m2/") }
        mavenCentral()
        gradlePluginPortal()
    }

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
                languageVersion.set(JavaLanguageVersion.of(16))
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
                    links("https://docs.oracle.com/javase/9/docs/api/")
                }
            }
        }
    }

    extensions.findByType(PublishingExtension::class)?.apply {
        if (project.path == ":") {
            // Skip the root project
            return@apply
        }

        configure<SigningExtension> {
            sign(publications.findByName("mavenJava"))
        }

        repositories {
            maven {
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (isRelease) releasesRepoUrl else snapshotRepoUrl
                credentials {
                    username = project.stringProperty("ossrhUsername") ?: ""
                    password = project.stringProperty("ossrhPassword") ?: ""
                }
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
