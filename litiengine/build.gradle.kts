import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    jacoco
    id("com.stehno.natives")
    id("org.sonarqube")
    signing
    `maven-publish`
}

description = """
    LITIENGINE is a free and open source Java 2D Game Engine.
    It provides a comprehensive Java library and a dedicated map editor to create tile-based 2D games.
""".trimIndent()

sourceSets {
    main {
        java {
            srcDir(rootDir.resolve("src"))
        }
        resources {
            srcDir(rootDir.resolve("resources"))
        }
    }
    test {
        java {
            srcDir(rootDir.resolve("tests"))
        }
        resources {
            srcDir(rootDir.resolve("testsResources"))
        }
    }
}

dependencies {
    implementation(libs.bundles.jinput)
    implementation(libs.bundles.soundlibs)
    implementation(libs.steamworks)
    implementation(libs.javax.activation)
    // This needs to be api to make the annotations on the class visible to the compiler.
    api(libs.xml.api)
    runtimeOnly(libs.bundles.xml.runtime)

    testImplementation(project(":test-common"))
}

natives {
    configurations = listOf("runtimeClasspath")
    outputDir = "libs"
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
