import java.util.Calendar

plugins {
    `java-library`
    application
    id("org.beryx.runtime")
}

sourceSets {
    main {
        java {
            resources.srcDir("src/main/localization")
        }
    }
}

application {
    mainClass.set("de.gurkenlabs.utiliti.Program")
    applicationDistribution.into(executableDir) {
        from("dist")
    }
}

dependencies {
    implementation(project(":litiengine"))
    implementation(libs.darklaf.core)

    testImplementation(project(":test-common"))
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
                "--icon", project.file("dist/pixel-icon-utiliti.ico").path,
                "--copyright", "2020-${Calendar.getInstance().get(Calendar.YEAR)} gurkenlabs.de",
                "--vendor", "gurkenlabs.de",
                "--java-options", "-Xms256m",
                "--java-options", "-Xmx1024m",
            )
        )
    }
}
