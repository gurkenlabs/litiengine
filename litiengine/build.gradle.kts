import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    jacoco
    id("com.stehno.natives")
}

dependencies {
    implementation("net.java.jinput:jinput:2.0.9")
    implementation("net.java.jinput:jinput:2.0.9:natives-all")
    implementation("com.googlecode.soundlibs:jorbis:0.0.17.4")
    implementation("com.googlecode.soundlibs:tritonus-share:0.3.7.4")
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4") {
        exclude(group = "junit")
    }

    implementation("com.code-disaster.steamworks4j:steamworks4j:1.8.0")

    // JAXB modules for JDK 9 or higher
    implementation("javax.activation:javax.activation-api:1.2.0")
    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
    implementation("com.sun.xml.bind:jaxb-core:3.0.1")
    implementation("com.sun.xml.bind:jaxb-impl:3.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("org.mockito:mockito-core:3.10.0")
    testImplementation("org.mockito:mockito-inline:3.10.0")
}

natives {
    configurations = listOf("runtimeClasspath")
    outputDir = "libs"
}

tasks {
    test {
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
